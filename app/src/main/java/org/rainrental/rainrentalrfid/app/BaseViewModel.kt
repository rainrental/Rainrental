package org.rainrental.rainrentalrfid.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.chainway.data.BarcodeHardwareState
import org.rainrental.rainrentalrfid.chainway.data.RfidHardwareState
import org.rainrental.rainrentalrfid.hardware.HardwareEventListener
import org.rainrental.rainrentalrfid.hardware.HardwareEventBus

abstract class BaseViewModel(
    protected val dependencies: BaseViewModelDependencies
) : ViewModel(), HardwareEventListener {

    init {
        dependencies.hardwareEventBus.registerListener(this)
    }

    val hardwareState = dependencies.rfidManager.hardwareState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = RfidHardwareState.Init
    )
    val scannerState = dependencies.scannerManager.barcodeHardwareState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = BarcodeHardwareState.Initialising
    )

    override fun onCleared() {
        dependencies.hardwareEventBus.unregisterListener(this)
        super.onCleared()
    }

    // Default empty implementations - ViewModels can override what they need
    override fun onTriggerDown() {
        android.util.Log.d("BaseViewModel", "${this.javaClass.simpleName}: onTriggerDown called")
    }
    override fun onTriggerUp() {
        android.util.Log.d("BaseViewModel", "${this.javaClass.simpleName}: onTriggerUp called")
    }
    override fun onSideKeyDown() {
        android.util.Log.d("BaseViewModel", "${this.javaClass.simpleName}: onSideKeyDown called")
    }
    override fun onSideKeyUp() {
        android.util.Log.d("BaseViewModel", "${this.javaClass.simpleName}: onSideKeyUp called")
    }
    override fun onAuxKeyDown() {
        android.util.Log.d("BaseViewModel", "${this.javaClass.simpleName}: onAuxKeyDown called")
    }
    override fun onAuxKeyUp() {
        android.util.Log.d("BaseViewModel", "${this.javaClass.simpleName}: onAuxKeyUp called")
    }

    fun triggerToast(text:String){
        viewModelScope.launch {
            dependencies.triggerToastUseCase(text)
        }
    }

    fun blipBeep() { dependencies.audioService.playSuccess() }
    fun successBeep() {dependencies.audioService.playSuccess() }
    fun errorBeep() { dependencies.audioService.playError() }
}