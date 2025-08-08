package org.rainrental.rainrentalrfid.chainway.presentation


import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient.Mqtt3SubscribeAndCallbackBuilder.Call.Ex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.chainway.data.BarcodeHardwareState
import org.rainrental.rainrentalrfid.chainway.data.RfidHardwareState
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.chainway.data.ScannerManager
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiFlow
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

@HiltViewModel
class RfidViewModel @Inject constructor(
    private val rfidManager: RfidManager,
    private val scannerManager: ScannerManager,
) : ViewModel(), LifecycleEventObserver, Logger {

    init {
        initializeHardware()
    }

    private val _connectionStatus = rfidManager.getConnectionStatus()
    val connectionStatus: StateFlow<Boolean> = _connectionStatus

    val hardwareState = rfidManager.hardwareState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = RfidHardwareState.Init
    )
    val scannerState = scannerManager.barcodeHardwareState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = BarcodeHardwareState.Initialising
    )

    val scannedTags = rfidManager.scannedTags
    val huntResults = rfidManager.huntResults
    val hunting = rfidManager.hunting

    private fun initializeHardware() = rfidManager.initializeHardware()

    private fun shutdownHardware() = rfidManager.shutdownHardware()

    fun scanTag(){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                rfidManager.readSingleTag()
            }catch (e:Exception){
                loge("Error getting tag: ${e.message}")
            }

        }
    }

    fun huntTag(tid:String){
        viewModelScope.launch(Dispatchers.IO){ rfidManager.startTagHunt(tid) }
    }

    override fun onCleared() {
        super.onCleared()
        shutdownHardware()
        logi("onCleared")
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        logi("State changed: ${event.name}")
        when (event){
            Lifecycle.Event.ON_RESUME -> initializeHardware()
            Lifecycle.Event.ON_PAUSE -> shutdownHardware()
            else -> {}
        }
    }
}