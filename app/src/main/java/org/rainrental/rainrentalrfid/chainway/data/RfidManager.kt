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

    // Add state validation helper
    private fun canTransitionTo(newState: RfidHardwareState): Boolean {
        return when (newState) {
            RfidHardwareState.Init -> true
            RfidHardwareState.Configuring -> _hardwareState.value != RfidHardwareState.Sleeping
            RfidHardwareState.Ready -> _hardwareState.value in listOf(RfidHardwareState.Init, RfidHardwareState.Configuring, RfidHardwareState.Scanning, RfidHardwareState.Writing)
            RfidHardwareState.Scanning -> _hardwareState.value in listOf(RfidHardwareState.Ready, RfidHardwareState.Configuring)
            RfidHardwareState.Writing -> _hardwareState.value == RfidHardwareState.Ready
            RfidHardwareState.Error -> true
            RfidHardwareState.ShuttingDown -> _hardwareState.value != RfidHardwareState.Sleeping
            RfidHardwareState.Sleeping -> _hardwareState.value == RfidHardwareState.ShuttingDown
        }
    }

    private fun updateHardwareState(newState: RfidHardwareState) {
        if (canTransitionTo(newState)) {
            _hardwareState.update { newState }
        } else {
            loge("Invalid state transition from ${_hardwareState.value} to $newState")
        }
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

    override fun startContinuousScanning() {
        if (!checkCleanupState()) return
        
        scope.launch {
            ensureActive()
            resetContinuousScanningStats()
            
            // Clear any existing EPC filter before starting continuous scanning
            clearEpcFilter()
            logd("Starting continuous scanning with cleared EPC filter")
            
            rfid?.let { rf ->
                val currentMode = rf.epcAndTIDUserMode
                if (currentMode != null && currentMode.mode == 0) {
                    rf.setEPCAndTIDMode()
                }

                rf.setInventoryCallback { tagData ->
                    scope.launch { onTagEvent(tagData) }
                }
                if (rf.startInventoryTag()) updateHardwareState(RfidHardwareState.Scanning)
            }
        }
    }

    override fun stopContinuousScanning() {
        if (!checkCleanupState()) return
        
        scope.launch {
            ensureActive()
            if (rfid?.isInventorying == false) return@launch
            if (!isActive) updateHardwareState(RfidHardwareState.Sleeping)
            updateHardwareState(RfidHardwareState.Configuring)
            val didStopInventory = rfid?.stopInventory()
            if (didStopInventory == true || rfid?.isInventorying == false) updateHardwareState(RfidHardwareState.Ready)
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
                    it.setPower(Config.HUNT_POWER)
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
                    rfid.setFrequencyMode(Config.FREQUENCY_MODE)
                    rfid.setPower(Config.DEFAULT_POWER)
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
                
                withPower?.let { power ->
                    updateHardwareState(RfidHardwareState.Configuring)
                    rfid?.setEPCAndTIDMode()
                    rfid?.setPower(power)
                }
                
                updateHardwareState(RfidHardwareState.Scanning)
                
                val tag = rfid?.inventorySingleTag()
                updateHardwareState(RfidHardwareState.Ready)
                
                tag?.let { t ->
                    if (t.tid.isNullOrBlank() || t.epc.isNullOrBlank()) {
                        return@async Result.Error(InputError.NoRfidTag)
                    }
                    
                    _scannedTags.value += t
                    val r = RfidTagInfo(tid = t.tid, epc = t.epc)
                    return@async Result.Success(r)
                } ?: run {
                    return@async Result.Error(InputError.NoRfidTag)
                }
                
            } catch (e: Exception) {
                loge("Error during tag reading: $e")
                updateHardwareState(RfidHardwareState.Error)
                return@async Result.Error(InputError.HardwareError)
            }
        }
        return deferred.await()
    }

    override suspend fun writeTagEpc(tid: String, epc: String): Result<Boolean, InputError> {
        val deferred = scope.async {
            updateHardwareState(RfidHardwareState.Writing)
            if (configureTidFilter(tid)){
                rfid?.let{rf->
                    rf.setPower(Config.WRITE_POWER)
                    val didWrite = rf.writeDataToEpc("00000000",epc)
                    if (didWrite){
                        configureTidFilter("")
                        return@async Result.Success(true)
                    }else{
                        configureTidFilter("")
                        return@async Result.Error(InputError.WriteEpcError)
                    }
                }?: return@async Result.Error(InputError.WriteEpcError)
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

    override suspend fun configureEpcFilter(epc: String,):Boolean {
        updateHardwareState(RfidHardwareState.Configuring)
        val deferred = scope.async {
            rfid?.let{rf->
                val startBit = 2 * 16
                val numBits = epc.length * 4
                val didSetEpc = rf.setFilter(IUHF.Bank_EPC,startBit,numBits,epc)
                logd("EPC filter configured successfully with EPC: $epc")
                rf.setFastID(true)
                updateHardwareState(RfidHardwareState.Ready)
                return@async true
            }

            updateHardwareState(RfidHardwareState.Ready)
            return@async false
        }
        return deferred.await()
    }

    override suspend fun clearEpcFilter():Boolean {
        updateHardwareState(RfidHardwareState.Configuring)
        val deferred = scope.async {
            rfid?.let{rf->
                // Clear the EPC filter by setting it with 0 bits
                val didClearEpc = rf.setFilter(IUHF.Bank_EPC, 0, 0, "")
                rf.setFastID(true)
                updateHardwareState(RfidHardwareState.Ready)
                logd("EPC filter cleared successfully")
                return@async true
            }

            updateHardwareState(RfidHardwareState.Ready)
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