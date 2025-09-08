package org.rainrental.rainrentalrfid.app

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.rainrental.rainrentalrfid.continuousScanning.MqttDeliveryService
import org.rainrental.rainrentalrfid.continuousScanning.data.DeliveryConnectionState
import javax.inject.Inject

@HiltViewModel
class MainAppViewModel @Inject constructor(
    private val mqttDeliveryService: MqttDeliveryService,
    dependencies: BaseViewModelDependencies
) : BaseViewModel(dependencies = dependencies) {
    
    val deliveryState = mqttDeliveryService.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = DeliveryConnectionState.DEAD
    )
}
