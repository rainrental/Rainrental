package org.rainrental.rainrentalrfid.continuousScanning.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.app.BaseViewModel
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.app.deviceSerial
import org.rainrental.rainrentalrfid.chainway.data.RfidHardwareState.Scanning
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.continuousScanning.MqttDeliveryService
import org.rainrental.rainrentalrfid.continuousScanning.data.MqttTagMessage
import org.rainrental.rainrentalrfid.continuousScanning.data.ContinuousScanningState
import org.rainrental.rainrentalrfid.continuousScanning.data.TagEvent
import org.rainrental.rainrentalrfid.continuousScanning.data.TagInventoryEvent
import org.rainrental.rainrentalrfid.continuousScanning.data.convertToJsonString
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Base64
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import android.content.Context
import org.rainrental.rainrentalrfid.app.AppConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.ScanBarcodeUseCase
import org.rainrental.rainrentalrfid.commission.data.CommissionApi
import org.rainrental.rainrentalrfid.commission.data.CheckInBarcodeRequestDto
import org.rainrental.rainrentalrfid.commission.data.CheckInBarcodeResponseDto
import org.rainrental.rainrentalrfid.apis.ApiCaller
import org.rainrental.rainrentalrfid.result.ApiError
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Named


@HiltViewModel
class ContinuousScanningViewModel @Inject constructor(
    private val mqttDeliveryService: MqttDeliveryService,
    @ApplicationContext private val context: Context,
    private val appConfig: AppConfig,
    private val scanBarcodeUseCase: ScanBarcodeUseCase,
    private val commissionApi: CommissionApi,
    @Named("company_id") private val companyId: String,
    @Named("rain_company_id") private val rainCompanyId: Int,
    dependencies: BaseViewModelDependencies
) : BaseViewModel(dependencies = dependencies), Logger {

    companion object {
        private var instanceCount = 0
    }

    val continuousScanningState = dependencies.rfidManager.continuousScanningState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ContinuousScanningState()
    )

    val currentServer = mqttDeliveryService.currentServer.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ""
    )

    val deliverState = mqttDeliveryService.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = mqttDeliveryService.state.value
    )
    
    // Expose AppConfig for UI access
    val appConfigState = kotlinx.coroutines.flow.MutableStateFlow(appConfig).asStateFlow()
    
    // Expose EPC filter information for UI
    private val _currentEpcFilter = kotlinx.coroutines.flow.MutableStateFlow("")
    val currentEpcFilter = _currentEpcFilter.asStateFlow()
    
    private val _currentRainCompanyId = kotlinx.coroutines.flow.MutableStateFlow(0)
    val currentRainCompanyId = _currentRainCompanyId.asStateFlow()
    
    private val _epcFilterEnabled = kotlinx.coroutines.flow.MutableStateFlow(true)
    val epcFilterEnabled = _epcFilterEnabled.asStateFlow()

    init {
        instanceCount++
        logd("ContinuousScanningViewModel initialized (instance #$instanceCount)")
        
        // Set the RainRental company ID for EPC filtering FIRST
        dependencies.rfidManager.setRainCompanyId(rainCompanyId)
        logd("Set RainRental company ID for EPC filtering: $rainCompanyId")
        
        // THEN initialize the EPC filter display with the correct company ID
        _currentEpcFilter.value = dependencies.rfidManager.getCurrentEpcFilter()
        _currentRainCompanyId.value = dependencies.rfidManager.getRainCompanyId()
        logd("EPC filter display initialized with: ${_currentEpcFilter.value}")
        
        startContinuousScanningWatch()
    }

    private var continuousScanningWatchJob : Job? = null
    private var isScanning = false
    private var isScreenActive = false

    private fun startContinuousScanningWatch(){
        continuousScanningWatchJob?.cancel()
        continuousScanningWatchJob = viewModelScope.launch(Dispatchers.IO){
            ensureActive()
            try {
                dependencies.rfidManager.uniqueTagEventsFlow.collect {
                    ensureActive()
                    viewModelScope.launch(Dispatchers.IO) {
                        processTagEvent(it)
                    }
                }
            } catch (cancellationException: CancellationException) {
                logd("Cancelling Continuous Scanning")
            } catch (e: Exception) {
                logd("Caught exception: ${e}")
            }
        }
    }

    private suspend fun processTagEvent(tagEvent: TagEvent) {
        val stringMessage = tagEventToString(
                report = tagEvent,
                deviceSerial = deviceSerial
            )
        stringMessage?.let{
            val topic = "rfid/mobile/$deviceSerial"
            mqttDeliveryService.publish(stringMessage, topic)
        }
        logd("Tag Event: $tagEvent")
        logd("String Message: $stringMessage")
        if (hardwareState.value == Scanning) {
            logd("Hardware state is Scanning, calling blipBeep()")
            blipBeep()
        } else {
            logd("Hardware state is not Scanning: ${hardwareState.value}")
        }
    }

    private fun hexToBase64(hexString: String): String {
        val byteArray = hexStringToByteArray(hexString)
        return encodeToBase64(byteArray)
    }
    private fun hexStringToByteArray(hexString: String): ByteArray {
        val length = hexString.length
        val data = ByteArray(length / 2)
        for (i in 0 until length step 2) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character.digit(
                hexString[i + 1],
                16
            )).toByte()
        }
        return data
    }

    private fun encodeToBase64(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun tagEventToString(report: TagEvent, deviceSerial: String): String? {
        val mqttMessage = MqttTagMessage(
            timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
            hostname = deviceSerial,
            eventType = "tagInventory",
            tagInventoryEvent = TagInventoryEvent(
                tid = hexToBase64(report.tid),
                tidHex = report.tid,
                epc = report.epc,
                antennaName = "C72",
                antennaPort = 1,
                peakRssiCdbm = report.rssi * 100,
                frequency = report.frequency.toInt(),
                transmitPowerCdbm = report.power * 100
            )
        )
        val msg = try {
            convertToJsonString(mqttMessage)
        } catch (e: Exception) {
            logd("Error converting Tag Event to JSON string")
            loge(e.printStackTrace().toString())
            null
        }
        return msg
    }

    override fun onTriggerDown() {
        if (!isScreenActive) {
            logd("Continuous Scanning Trigger Down - Ignored (screen not active)")
            return
        }
        
        if (!isScanning) {
            logd("Continuous Scanning Trigger Down - Starting RFID continuous scanning")
            isScanning = true
            dependencies.scanningLifecycleManager.setScanningState(true)
            dependencies.rfidManager.startContinuousScanning()
        }
    }

    override fun onTriggerUp() {
        if (!isScreenActive) {
            logd("Continuous Scanning Trigger Up - Ignored (screen not active)")
            return
        }
        
        if (isScanning) {
            logd("Continuous Scanning Trigger Up - Stopping RFID continuous scanning")
            isScanning = false
            dependencies.scanningLifecycleManager.setScanningState(false)
            dependencies.rfidManager.stopContinuousScanning()
        }
    }

    override fun onSideKeyDown() {
        // Side key now triggers barcode scanning instead of RFID scanning
        logd("Continuous Scanning Side Key Down - Starting barcode scan")
        viewModelScope.launch {
            scanBarcodeAndCheckIn()
        }
    }

    override fun onSideKeyUp() {
        // Side key up doesn't do anything for barcode scanning
        logd("Continuous Scanning Side Key Up - Barcode scan completed")
    }

    override fun onCleared() {
        logd("ContinuousScanningViewModel being cleared")
        isScreenActive = false
        continuousScanningWatchJob?.cancel()
        // Ensure scanning is stopped when ViewModel is cleared
        if (isScanning) {
            isScanning = false
            dependencies.scanningLifecycleManager.setScanningState(false)
            dependencies.rfidManager.stopContinuousScanning()
        }
        super.onCleared()
    }
    
    /**
     * Restarts MQTT connection with the configured server
     */
    suspend fun restartMqttConnection() {
        logd("Restarting MQTT connection")
        val serverIp = appConfig.getMqttServerIp(context)
        mqttDeliveryService.restartWithNewServer(serverIp)
    }
    
    /**
     * Toggles the EPC filter on/off
     */
    fun toggleEpcFilter() {
        val newState = !_epcFilterEnabled.value
        _epcFilterEnabled.value = newState
        dependencies.rfidManager.setEpcFilterEnabled(newState)
        logd("EPC filter toggled to: $newState")
    }
    
    
    override fun onScreenResumed() {
        isScreenActive = true
        logd("Continuous Scanning screen resumed - hardware events enabled")
    }
    
    override fun onScreenPaused() {
        isScreenActive = false
        logd("Continuous Scanning screen paused - hardware events disabled")
        
        // Stop any ongoing scanning when screen is paused
        if (isScanning) {
            logd("Stopping continuous scanning due to screen pause")
            isScanning = false
            dependencies.scanningLifecycleManager.setScanningState(false)
            dependencies.rfidManager.stopContinuousScanning()
        }
    }

    /**
     * Scans a barcode and checks in the asset
     */
    private suspend fun scanBarcodeAndCheckIn() {
        try {
            logd("Starting barcode scan for check-in")
            
            when (val barcodeResult = scanBarcodeUseCase()) {
                is Result.Error -> {
                    logd("Barcode scan failed: ${barcodeResult.error.name}")
                    // Could add toast notification here if needed
                }
                is Result.Success -> {
                    val barcode = barcodeResult.data
                    logd("Barcode scanned: $barcode")
                    
                    // Call the checkInBarcode API
                    val request = CheckInBarcodeRequestDto(barcode = barcode, companyId = companyId)
                    when (val checkInResult = ApiCaller()<CheckInBarcodeResponseDto> { commissionApi.checkInBarcode(request) }) {
                        is Result.Error -> {
                            logd("Check-in failed: ${checkInResult.error.apiErrorType}")
                            // Could add toast notification here if needed
                        }
                        is Result.Success -> {
                            logd("Check-in successful: ${checkInResult.data.message}")
                            // Could add toast notification here if needed
                        }
                    }
                }
            }
        } catch (e: Exception) {
            loge("Error during barcode check-in: ${e.message}")
        }
    }
} 