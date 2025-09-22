package org.rainrental.rainrentalrfid.app

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.continuousScanning.MqttDeliveryService
import org.rainrental.rainrentalrfid.continuousScanning.data.DeliveryConnectionState
import org.rainrental.rainrentalrfid.mqtt.MqttConnectionWatchdog
import org.rainrental.rainrentalrfid.mqtt.WatchdogState
import javax.inject.Inject

@HiltViewModel
class MainAppViewModel @Inject constructor(
    private val mqttDeliveryService: MqttDeliveryService,
    private val mqttWatchdog: MqttConnectionWatchdog,
    dependencies: BaseViewModelDependencies
) : BaseViewModel(dependencies = dependencies) {
    
    val deliveryState = mqttDeliveryService.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = DeliveryConnectionState.DEAD
    )
    
    val watchdogState = mqttWatchdog.watchdogState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = WatchdogState.STOPPED
    )
    
    val consecutiveFailures = mqttWatchdog.consecutiveFailures.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0
    )
    
    val lastCheckTime = mqttWatchdog.lastCheckTime.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0L
    )
    
    /**
     * Start the MQTT connection watchdog
     */
    fun startMqttWatchdog(context: Context) {
        viewModelScope.launch {
            logd("MainAppViewModel: Starting MQTT connection watchdog")
            mqttWatchdog.startWatchdog(context)
        }
    }
    
    /**
     * Stop the MQTT connection watchdog
     */
    fun stopMqttWatchdog() {
        viewModelScope.launch {
            logd("MainAppViewModel: Stopping MQTT connection watchdog")
            mqttWatchdog.stopWatchdog()
        }
    }
    
    /**
     * Force a connection check
     */
    fun forceConnectionCheck(context: Context) {
        viewModelScope.launch {
            logd("MainAppViewModel: Force MQTT connection check")
            mqttWatchdog.forceConnectionCheck(context)
        }
    }
    
    /**
     * Get current connection status
     */
    fun getConnectionStatus() = mqttWatchdog.getConnectionStatus()
}
