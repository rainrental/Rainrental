package org.rainrental.rainrentalrfid.chainway.data


import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient.Mqtt3SubscribeAndCallbackBuilder.Call.Ex
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.IUHF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import org.rainrental.rainrentalrfid.audio.AudioService
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.RfidTagInfo
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.continuousScanning.data.ContinuousScanningState
import org.rainrental.rainrentalrfid.continuousScanning.data.TagEvent
import org.rainrental.rainrentalrfid.result.InputError
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.chainway.data.TagWithOrientation

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

enum class RfidHardwareState{
    Init,
    Configuring,
    Ready,
    Scanning,
    Writing,
    Error,
    ShuttingDown,
    Sleeping
}

interface RfidManager : LifecycleEventObserver {
    fun initializeHardware()
    fun shutdownHardware()
    suspend fun readSingleTag(withPower: Int? = null): Result<RfidTagInfo,InputError>
    suspend fun writeTagEpc(tid:String,epc:String): Result<Boolean,InputError>

    suspend fun startTagHunt(epc:String) : Boolean
    suspend fun stopTagHunt() : Boolean

    suspend fun configureEpcFilter(epc:String):Boolean
    suspend fun configureTidFilter(tid:String):Boolean
    suspend fun clearEpcFilter():Boolean
    val hardwareState: StateFlow<RfidHardwareState>
    var scannedTags: StateFlow<List<UHFTAGInfo>>
    var huntResults: StateFlow<List<TagWithOrientation>>
    var hunting: StateFlow<Boolean>
    val inventory: StateFlow<ConcurrentHashMap<String,UHFTAGInfo>>
    val inventoryCount: StateFlow<Int>
    val continuousScanningState: StateFlow<ContinuousScanningState>
    val tagEvents: StateFlow<ConcurrentHashMap<String, TagEvent>>
    val tagEventsFlow: SharedFlow<TagEvent>
    val uniqueTagEventsFlow: SharedFlow<TagEvent>
    suspend fun startInventoryScan(epc:String) : Boolean
    suspend fun stopInventoryScan() : Boolean

    fun startContinuousScanning()
    fun stopContinuousScanning()
    fun getConnectionStatus(): StateFlow<Boolean>
    fun setRainCompanyId(companyId: Int)
    fun getCurrentEpcFilter(): String
    fun getRainCompanyId(): Int
    fun setEpcFilterEnabled(enabled: Boolean)
}

object ChainwayRfidManager: RfidManager, Logger {
    // CRITICAL: Keep single thread context for native hardware callbacks
    // Chainway RFID hardware uses native callbacks that come from hardware threads
    // These callbacks must be handled on a dedicated thread to avoid race conditions
    // and ensure thread safety with the hardware library
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val scope = CoroutineScope(newSingleThreadContext("RfidThread"))
    private var rfid : RFIDWithUHFUART? = null
    private var connectionStatus = MutableStateFlow(false) // false means disconnected

    // Add cleanup flag to prevent operations after cleanup
    private var isCleanedUp = false
    
    // RainRental company ID for EPC filtering
    private var rainCompanyId: Int = 12 // Default value
    private var epcFilterEnabled: Boolean = true // Default to enabled
    
    // Configuration - using hardcoded values since this is a singleton object
    private object Config {
        const val DEFAULT_POWER = 24
        const val WRITE_POWER = 2
        const val HUNT_POWER = 30
        const val FREQUENCY_MODE = 4
    }

    private val _hardwareState: MutableStateFlow<RfidHardwareState> = MutableStateFlow(RfidHardwareState.Init)
    override val hardwareState: StateFlow<RfidHardwareState> = _hardwareState.asStateFlow()

    private val _scannedTags: MutableStateFlow<List<UHFTAGInfo>> = MutableStateFlow(emptyList())
    override var scannedTags: StateFlow<List<UHFTAGInfo>> = _scannedTags.asStateFlow()

    private var _hunting: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override var hunting: StateFlow<Boolean> = _hunting.asStateFlow()

    private var _inventory: MutableStateFlow<ConcurrentHashMap<String,UHFTAGInfo>> = MutableStateFlow(
        ConcurrentHashMap()
    )
    override val inventory: StateFlow<ConcurrentHashMap<String, UHFTAGInfo>> = _inventory.asStateFlow()
    private val _inventoryCount : MutableStateFlow<Int> = MutableStateFlow(0)
    override val inventoryCount: StateFlow<Int> = _inventoryCount.asStateFlow()

    private val _huntResults: MutableStateFlow<List<TagWithOrientation>> = MutableStateFlow(emptyList())
    override var huntResults: StateFlow<List<TagWithOrientation>> = _huntResults.asStateFlow()

    private val _continuousScanningState = MutableStateFlow(ContinuousScanningState())
    override val continuousScanningState: StateFlow<ContinuousScanningState> = _continuousScanningState.asStateFlow()

    private val _tagEvents = MutableStateFlow(ConcurrentHashMap<String, TagEvent>())
    override val tagEvents: StateFlow<ConcurrentHashMap<String, TagEvent>> = _tagEvents.asStateFlow()

    private val _tagEventsFlow = MutableSharedFlow<TagEvent>()
    override val tagEventsFlow: SharedFlow<TagEvent> = _tagEventsFlow.asSharedFlow()

    private val _uniqueTagEventsFlow = MutableSharedFlow<TagEvent>()
    override val uniqueTagEventsFlow: SharedFlow<TagEvent> = _uniqueTagEventsFlow.asSharedFlow()

    private val orientationManager = OrientationManager

    private fun updateHardwareState(newState: RfidHardwareState) {
        _hardwareState.update { newState }
        logd("Hardware state updated to: $newState")
    }

    // Add cleanup method for proper resource management
    fun cleanup() {
        if (isCleanedUp) return
        isCleanedUp = true
        
        scope.launch {
            try {
                stopContinuousScanning()
                stopTagHunt()
                stopInventoryScan()
                shutdownHardware()
            } catch (e: Exception) {
                loge("Error during RfidManager cleanup: $e")
            } finally {
                scope.cancel()
                // Note: ExecutorsCoroutineDispatcher.close() is not available in this Kotlin version
                // The dispatcher will be cleaned up automatically when the scope is cancelled
            }
        }
    }

    private fun checkCleanupState(): Boolean {
        if (isCleanedUp) {
            return false
        }
        return true
    }

    override fun setRainCompanyId(companyId: Int) {
        rainCompanyId = companyId
        logd("Set RainRental company ID to: $companyId")
    }
    
    /**
     * Gets the current EPC filter string for RainRental company tags
     */
    override fun getCurrentEpcFilter(): String {
        return createRainRentalEpcFilter(rainCompanyId)
    }
    
    /**
     * Gets the current RainRental company ID
     */
    override fun getRainCompanyId(): Int {
        return rainCompanyId
    }
    
    override fun setEpcFilterEnabled(enabled: Boolean) {
        epcFilterEnabled = enabled
        logd("EPC filter enabled set to: $enabled")
    }

    override fun startContinuousScanning() {
        if (!checkCleanupState()) return
        
        scope.launch {
            ensureActive()
            resetContinuousScanningStats()
            
            // Ensure hardware is ready before any operations
            if (!validateConnection()) {
                loge("Hardware not ready for continuous scanning")
                return@launch
            }
            
            rfid?.let { rf ->
                // Set mode first to ensure hardware is in correct state
                val didSetMode = rf.setEPCAndTIDMode()
                if (isHardwareError(didSetMode)) {
                    loge("setEPCAndTIDMode failed - reconnecting hardware")
                    reconnectHardware()
                    return@launch
                }
                
                // Set power to HUNT_POWER (30dB) for continuous scanning
                val didSetPower = rf.setPower(Config.HUNT_POWER)
                if (isHardwareError(didSetPower)) {
                    loge("setPower failed - reconnecting hardware")
                    reconnectHardware()
                    return@launch
                }
                logd("Set RFID power to ${Config.HUNT_POWER}dB for continuous scanning")
                
                // Now handle EPC filter operations after hardware is properly configured
                if (epcFilterEnabled) {
                    // Clear any existing EPC filter first
                    val didClear = clearEpcFilter()
                    if (!didClear) {
                        loge("Failed to clear EPC filter - reconnecting hardware")
                        reconnectHardware()
                        return@launch
                    }
                    logd("Cleared existing EPC filter")
                    
                    // Apply EPC filter for RainRental company tags
                    val epcFilter = createRainRentalEpcFilter(rainCompanyId)
                    val didConfigure = configureEpcFilter(epcFilter)
                    if (!didConfigure) {
                        loge("Failed to configure EPC filter - reconnecting hardware")
                        reconnectHardware()
                        return@launch
                    }
                    logd("Starting continuous scanning with EPC filter: $epcFilter")
                } else {
                    // Clear EPC filter when disabled
                    val didClear = clearEpcFilter()
                    if (!didClear) {
                        loge("Failed to clear EPC filter - reconnecting hardware")
                        reconnectHardware()
                        return@launch
                    }
                    logd("Starting continuous scanning WITHOUT EPC filter (filter disabled)")
                }

                rf.setInventoryCallback { tagData ->
                    scope.launch { onTagEvent(tagData) }
                }
                
                val didStartInventory = rf.startInventoryTag()
                if (isHardwareError(didStartInventory)) {
                    loge("startInventoryTag failed - reconnecting hardware")
                    reconnectHardware()
                    return@launch
                }
                
                updateHardwareState(RfidHardwareState.Scanning)
            }
        }
    }

    override fun stopContinuousScanning() {
        if (!checkCleanupState()) return
        
        scope.launch {
            ensureActive()
            
            // Only stop if we're actually inventorying
            if (rfid?.isInventorying != true) {
                logd("No active inventory to stop")
                return@launch
            }
            
            updateHardwareState(RfidHardwareState.Configuring)
            
            // Stop inventory and wait for it to complete
            val didStopInventory = rfid?.stopInventory()
            if (didStopInventory == true) {
                logd("Successfully stopped continuous scanning")
                updateHardwareState(RfidHardwareState.Ready)
            } else {
                loge("Failed to stop inventory properly")
                updateHardwareState(RfidHardwareState.Error)
            }
        }
    }

    private fun resetContinuousScanningStats() {
        _tagEvents.update { ConcurrentHashMap<String, TagEvent>() }
        _continuousScanningState.update {
            it.copy(
                tagCount = 0,
                scanningRoundId = it.scanningRoundId + 1,
                tagIds = emptyList(),
                uniqueCount = 0,
                tagId = null,
                cacheSize = 0,
                lastTagEvent = null,
                lastRssi = Double.NEGATIVE_INFINITY
            )
        }
    }

    private fun containsTid(tid: String): Boolean {
        return _tagEvents.value.any { it.key == tid }
    }



    private suspend fun onTagEvent(tagData: UHFTAGInfo) {
        if (tagData.tid == null) {
            return
        }
        
        // Log the detected tag EPC for debugging
        logd("Detected tag - EPC: ${tagData.epc}, TID: ${tagData.tid}, RSSI: ${tagData.rssi}")
        
        try {
            val tagEvent = TagEvent(
                epc = tagData.epc,
                tid = tagData.tid,
                roundId = 0,
                rssi = tagData.rssi.toDoubleOrNull() ?: 0.0,
                seen = 1,
                frequency = tagData.frequencyPoint,
                power = 30
            )
            
            // Use tryEmit to avoid blocking if the flow is full
            _tagEventsFlow.tryEmit(tagEvent)
            
            if (containsTid(tagData.tid)) {
                _tagEvents.update { events ->
                    events[tagData.tid]?.let { it.seen += 1 }
                    events
                }
            } else {
                _tagEvents.value.putIfAbsent(tagData.tid, tagEvent)
//                _uniqueTagEventsFlow.tryEmit(tagEvent)
                // Can't use tryEmit here because it never gets through to the user
                _uniqueTagEventsFlow.emit(tagEvent)
            }
            
            _continuousScanningState.update {
                it.copy(
                    uniqueCount = _tagEvents.value.size,
                    lastTagEvent = tagEvent,
                    lastRssi = tagEvent.rssi,
                    tagCount = it.tagCount + 1
                )
            }

        } catch (e: Exception) {
            loge("Error processing tag event: $e")
        }
    }



    override suspend fun startInventoryScan(epc:String): Boolean {
        val deferred = scope.async {
            if (!connectionStatus.value) return@async false
            _inventory.value.clear()
            _inventoryCount.update { 0 }
            rfid?.let{
                try {
                    updateHardwareState(RfidHardwareState.Configuring)
                    it.setEPCAndTIDMode()
                    it.power = Config.HUNT_POWER
                    configureEpcFilter(epc.slice(0..9))
                    it.setInventoryCallback {tag->
                        if (tag != null){
                            if (!tag.epc.isNullOrBlank() || !tag.tid.isNullOrBlank()){
                                val epcFirst96 = tag.epc.slice(0..23)
                                if (!_inventory.value.contains(key = epcFirst96)){
                                    if (tag.epc.slice(0..10) == epc.slice(0..10)){
                                        _inventory.value.putIfAbsent(epcFirst96,tag)
                                    }
                                }
                                _inventory.value = _inventory.value
                                _inventoryCount.update { _inventory.value.size }
                            }
                        }
                    }
                    updateHardwareState(RfidHardwareState.Scanning)
                    it.startInventoryTag()
                    true

                }catch (e:Exception){
                    loge("Error starting inventory scan: $e")
                    false
                }
            }
            false
        }
        return deferred.await()
    }

    override suspend fun stopInventoryScan(): Boolean {
        val deferred = scope.async {
            if (rfid?.isInventorying == true){
                val didStop = rfid?.stopInventory()
                if (didStop == true){
                    updateHardwareState(RfidHardwareState.Ready)
                }
                return@async didStop?:false
            }else{
                return@async true
            }
        }
        return deferred.await()
    }

    override fun initializeHardware() {
        if (!checkCleanupState()) return
        scope.launch {
            try {
                updateHardwareState(RfidHardwareState.Init)
                
                initializeRfidInstance()
                if (rfid == null) {
                    loge("Failed to get RFID instance")
                    connectionStatus.update { false }
                    updateHardwareState(RfidHardwareState.Error)
                    return@launch
                }
                
                rfid?.let { rfid ->
                    val didOpen = safeHardwareOperation(
                        operation = { rfid.init() },
                        errorMessage = "Failed to initialize RFID hardware",
                        fallbackState = RfidHardwareState.Error
                    )
                    
                    if (didOpen) {
                        updateHardwareState(RfidHardwareState.Configuring)
                        
                        val configSuccess = safeHardwareOperation(
                            operation = {
                                                    rfid.setEPCAndTIDMode()
                                rfid.frequencyMode = Config.FREQUENCY_MODE
                                rfid.power = Config.DEFAULT_POWER
                                true
                            },
                            errorMessage = "Failed to configure RFID hardware",
                            fallbackState = RfidHardwareState.Error
                        )
                        
                        if (configSuccess) {
                            updateHardwareState(RfidHardwareState.Ready)
                            connectionStatus.update { true }
                        }
                    }
                } ?: run {
                    connectionStatus.update { false }
                    updateHardwareState(RfidHardwareState.Error)
                }
            } catch (e: Exception) {
                loge("Unexpected error during RFID initialization: $e")
                connectionStatus.update { false }
                updateHardwareState(RfidHardwareState.Error)
            }
        }
    }

    override fun shutdownHardware() {
        scope.launch {
            try {
                updateHardwareState(RfidHardwareState.ShuttingDown)
                rfid?.free()
                rfid = null
                updateHardwareState(RfidHardwareState.Sleeping)
                connectionStatus.value = false
            } catch (e: Exception) {
                loge("Error during RFID shutdown: $e")
                updateHardwareState(RfidHardwareState.Error)
            }
        }
    }

    override fun getConnectionStatus(): StateFlow<Boolean> = connectionStatus

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event){
            Lifecycle.Event.ON_RESUME -> initializeHardware()
            Lifecycle.Event.ON_PAUSE -> shutdownHardware()
            else -> {}
        }
    }

    override suspend fun readSingleTag(withPower: Int?): Result<RfidTagInfo,InputError> {
        val deferred = scope.async {
            try {
                if (!validateConnection()) {
                    return@async Result.Error(InputError.HardwareError)
                }
                
                _scannedTags.value = emptyList()
                
                // For single tag operations, always use 2dB (close-range scanning)
                val singleTagPower = withPower ?: Config.WRITE_POWER
                updateHardwareState(RfidHardwareState.Configuring)

                clearEpcFilter()

                val didSetMode = rfid?.setEPCAndTIDMode()
                if (isHardwareError(didSetMode)) {
                    loge("setEPCAndTIDMode failed during single tag read - reconnecting hardware")
                    reconnectHardware()
                    return@async Result.Error(InputError.HardwareError)
                }
                
                val didSetPower = rfid?.setPower(singleTagPower)
                if (isHardwareError(didSetPower)) {
                    loge("setPower failed during single tag read - reconnecting hardware")
                    reconnectHardware()
                    return@async Result.Error(InputError.HardwareError)
                }
                logd("Set RFID power to ${singleTagPower}dB for single tag read")
                
                updateHardwareState(RfidHardwareState.Scanning)
                
                val tag = rfid?.inventorySingleTag()
                updateHardwareState(RfidHardwareState.Ready)
                
                // After single tag read, always restore to DEFAULT_POWER (24dB)
                // This ensures we're ready for the next operation
                val didRestorePower = rfid?.setPower(Config.DEFAULT_POWER)
                if (isHardwareError(didRestorePower)) {
                    loge("Failed to restore power after single tag read - reconnecting hardware")
                    reconnectHardware()
                    return@async Result.Error(InputError.HardwareError)
                }
                logd("Restored RFID power to ${Config.DEFAULT_POWER}dB after single tag read")
                
                if (tag == null) {
                    return@async Result.Error(InputError.NoRfidTag)
                }
                
                if (tag.tid.isNullOrBlank() || tag.epc.isNullOrBlank()) {
                    return@async Result.Error(InputError.NoRfidTag)
                }
                
                _scannedTags.value += tag
                val r = RfidTagInfo(tid = tag.tid, epc = tag.epc)
                return@async Result.Success(r)
                
            } catch (e: Exception) {
                loge("Error during tag reading: $e")
                updateHardwareState(RfidHardwareState.Error)
                return@async Result.Error(InputError.HardwareError)
            }
        }
        return deferred.await()
    }

    /*

     */
    override suspend fun writeTagEpc(tid: String, epc: String): Result<Boolean, InputError> {
        val deferred = scope.async {
            updateHardwareState(RfidHardwareState.Writing)
            if (configureTidFilter(tid)){
                logd("This IS reachable")
                return@async rfid?.let{ rf->
                    logd("attempting to use $rf")
                    rf.power = Config.WRITE_POWER
                    logd("Set RFID power to ${Config.WRITE_POWER}dB for tag writing")
                    val didWrite = rf.writeDataToEpc("00000000",epc)
                    if (didWrite){
                        configureTidFilter("")
                        // Restore power to DEFAULT_POWER after writing
                        rf.power = Config.DEFAULT_POWER
                        logd("Restored RFID power to ${Config.DEFAULT_POWER}dB after tag writing")
                        //return@async
                        Result.Success(true)
                    }else{
                        configureTidFilter("")
                        // Restore power to DEFAULT_POWER even on failure
                        rf.power = Config.DEFAULT_POWER
                        logd("Restored RFID power to ${Config.DEFAULT_POWER}dB after failed tag writing")
                        //return@async
                        Result.Error(InputError.WriteEpcError)
                    }
                } ?: Result.Error(InputError.WriteEpcError)
            }else{
                return@async Result.Error(InputError.WriteEpcError)
            }
        }
        return deferred.await()
    }

    override suspend fun stopTagHunt(): Boolean {
        val deferred = scope.async {
            return@async if (_hunting.value){
                orientationManager.stopListening()
                rfid?.let{rf->
                    if (rf.stopInventory()) {
                        updateHardwareState(RfidHardwareState.Ready)
                        _hunting.update { false }
                        return@async true
                    }
                }
                false
            }else false
        }
        return deferred.await()
    }

    override suspend fun startTagHunt(epc: String) : Boolean{
        val deferred = scope.async {
            return@async if (_hunting.value){
                orientationManager.stopListening()
                rfid?.let{rf->
                    if (rf.stopInventory()) {
                        updateHardwareState(RfidHardwareState.Ready)
                        _hunting.update { false }
                        return@async false
                    }
                }
                false

            }else{
                _huntResults.update { emptyList() }
                orientationManager.startListening()
                rfid?.let{rf->
                    updateHardwareState(RfidHardwareState.Configuring)
                    rf.setPower(Config.HUNT_POWER)
                    rf.setEPCAndTIDMode()
                    configureEpcFilter(epc)
                    rf.setInventoryCallback { tagInfo->
                        val orientation = orientationManager.getDeviceOrientation()
                        val newTagWithOrientation = TagWithOrientation(tagInfo,System.currentTimeMillis(),orientation)
                        _huntResults.value += newTagWithOrientation
                    }
                    if (rf.startInventoryTag()) {
                        updateHardwareState(RfidHardwareState.Scanning)
                        _hunting.update { true }
                        return@async true
                    }
                }
                false
            }
        }
        return deferred.await()
    }

    /**
     * Creates an EPC filter for RainRental company tags
     * Based on the EPC structure: "1111" + companyIdBits + remainingBits
     * For company ID 198: "1111" + "00000000000011000110" (24 bits total)
     */
    private fun createRainRentalEpcFilter(companyId: Int): String {
        // Convert company ID to 16-bit binary string with proper padding
        // Then convert to hex representation of that binary string
        // If the hex string has an odd number of characters, add a trailing zero
        val companyIdBits = Integer.toBinaryString(companyId).padStart(16, '0')

        // RainRental standard prefix is "1111" (4 bits)
        // Company ID is 16 bits (padded to 20 bits for hardware compatibility)
        // Total filter length is 24 bits
        val filterValue = "1111$companyIdBits"

        logd("Created RainRental EPC filter: $filterValue for company ID: $companyId")
        logd("Filter breakdown: RainRental prefix='1111', Company ID bits='$companyIdBits'")

        val hexValue = Integer.toHexString(filterValue.toInt(2)).uppercase()
        logd("Hex value of filter: $hexValue")
        logd("Length of hex value: ${hexValue.length}")
        if (hexValue.length % 2 == 1) logd("Adding trailing zero to hex value to make it even: ${hexValue}0")
        if (hexValue.length % 2 == 1) return hexValue + "0"
        return hexValue
    }

    override suspend fun configureEpcFilter(epc: String,):Boolean {
        val deferred = scope.async {
            // Check if hardware is ready before attempting filter operations
            if (!validateConnection()) {
                loge("Hardware not ready for EPC filter configuration")
                return@async false
            }
            
            rfid?.let{rf->
                updateHardwareState(RfidHardwareState.Configuring)

                val didSetFastId = rf.setFastID(true)
                if (isHardwareError(didSetFastId)) {
                    loge("setFastID failed - reconnecting hardware")
                    reconnectHardware()
                    return@async false
                }

                val startBit = 32 // After CRC (0..15) and PC (16..31)
                val numBits = 20 //epc.length (Rain rental 4 bits (F) plus 16 bits companyId
                val didSetEpc = rf.setFilter(IUHF.Bank_EPC,startBit,numBits,epc)
//                val didSetEpc = rf.setFilter(IUHF.Bank_EPC,32,20,"F000C0")
                logd("Start bit: $startBit, Num bits: $numBits, EPC: $epc")
                
                if (isHardwareError(didSetEpc)) {
                    loge("EPC filter configuration failed - reconnecting hardware")
                    reconnectHardware()
                    return@async false
                }
                
                logd("EPC filter configured successfully with EPC: $epc")
                

                
                updateHardwareState(RfidHardwareState.Ready)
                return@async true
            }

            loge("Failed to configure EPC filter - RFID not available")
            return@async false
        }
        return deferred.await()
    }

    override suspend fun clearEpcFilter():Boolean {
        val deferred = scope.async {
            // Check if hardware is ready before attempting filter operations
            if (!validateConnection()) {
                loge("Hardware not ready for EPC filter clear")
                return@async false
            }
            
            rfid?.let{rf->
                updateHardwareState(RfidHardwareState.Configuring)
                
                // Clear the EPC filter by setting it with 0 bits
                val didClearEpc = rf.setFilter(IUHF.Bank_EPC, 0, 0, "")
                
                if (isHardwareError(didClearEpc)) {
                    loge("EPC filter clear failed - reconnecting hardware")
                    reconnectHardware()
                    return@async false
                }
                
                val didSetFastId = rf.setFastID(true)
                if (isHardwareError(didSetFastId)) {
                    loge("setFastID failed during EPC filter clear - reconnecting hardware")
                    reconnectHardware()
                    return@async false
                }
                
                updateHardwareState(RfidHardwareState.Ready)
                logd("EPC filter cleared successfully")
                return@async true
            }

            loge("Failed to clear EPC filter - RFID not available")
            return@async false
        }
        return deferred.await()
    }

    override suspend fun configureTidFilter(tid: String):Boolean {
        val deferred = scope.async {
            updateHardwareState(RfidHardwareState.Configuring)
            rfid?.let { rf ->
                val startBit = 0
                val numBits = tid.length * 4
                val didSetTid = rf.setFilter(IUHF.Bank_TID, startBit, numBits, tid)
                logd("TID filter configured successfully ($didSetTid) with TID: $tid")
                updateHardwareState(RfidHardwareState.Ready)
                return@async true
            }
            updateHardwareState(RfidHardwareState.Ready)
            return@async false
        }
        return deferred.await()
    }

    private fun initializeRfidInstance(){
        if (rfid != null) return
        rfid = RFIDWithUHFUART.getInstance()
    }

    // Add error handling helper
    private suspend fun safeHardwareOperation(
        operation: suspend () -> Boolean,
        errorMessage: String,
        fallbackState: RfidHardwareState = RfidHardwareState.Error
    ): Boolean {
        return try {
            val result = operation()
            if (!result) {
                loge("Hardware operation failed: $errorMessage")
                updateHardwareState(fallbackState)
            }
            result
        } catch (e: Exception) {
            loge("Hardware operation exception: $errorMessage - $e")
            updateHardwareState(fallbackState)
            false
        }
    }

    // Add connection validation
    private fun isConnected(): Boolean {
        return rfid != null && connectionStatus.value && _hardwareState.value != RfidHardwareState.Error
    }

    private fun validateConnection(): Boolean {
        if (!isConnected()) {
            loge("RFID not connected or in error state")
            return false
        }
        return true
    }
    
    /**
     * Checks if a hardware operation result indicates a communication error
     * Returns true if the operation failed with a -1 error (hardware communication issue)
     */
    private fun isHardwareError(result: Boolean?): Boolean {
        return result == false || result == null
    }
    
    /**
     * Performs a simple hardware reconnection when communication errors occur
     * This is a lightweight recovery that just reinitializes the connection
     */
    private suspend fun reconnectHardware() {
        loge("Hardware communication error - reconnecting")
        updateHardwareState(RfidHardwareState.Error)
        
        // Clean shutdown
        try {
            rfid?.stopInventory()
            rfid?.free()
        } catch (e: Exception) {
            loge("Error during hardware cleanup: $e")
        }
        
        rfid = null
        connectionStatus.update { false }
        
        // Brief pause for hardware reset
        kotlinx.coroutines.delay(500)
        
        // Reinitialize
        initializeHardware()
        
        // Wait for reconnection (with timeout)
        var attempts = 0
        while (attempts < 5 && !connectionStatus.value) {
            kotlinx.coroutines.delay(200)
            attempts++
        }
        
        if (connectionStatus.value) {
            logd("Hardware reconnected successfully")
        } else {
            loge("Hardware reconnection failed")
        }
    }

    // Add timeout handling
    private suspend fun <T> withTimeout(
        timeoutMs: Long = 5000L,
        operation: suspend () -> T
    ): T? {
        return try {
            withTimeout(timeoutMs) {
                operation()
            }
        } catch (e: TimeoutCancellationException) {
            loge("Operation timed out after ${timeoutMs}ms")
            null
        } catch (e: Exception) {
            loge("Operation failed with exception: $e")
            null
        }
    }

}