package org.rainrental.rainrentalrfid.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.chainway.data.BarcodeHardwareState
import org.rainrental.rainrentalrfid.chainway.data.RfidHardwareState
import org.rainrental.rainrentalrfid.hardware.HardwareEventListener
import org.rainrental.rainrentalrfid.hardware.HardwareEventBus
import org.rainrental.rainrentalrfid.logging.Logger

abstract class BaseViewModel(
    protected val dependencies: BaseViewModelDependencies
) : ViewModel(), HardwareEventListener, Logger {

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

    /**
     * Called when the user navigates back or the screen is being destroyed
     * Override this in subclasses to handle specific cleanup
     */
    open fun onBackPressed() {
        logd("${this.javaClass.simpleName}: onBackPressed called")
        cancelAllScanning()
    }

    /**
     * Called when the screen is paused (user navigates away, app backgrounded, etc.)
     * Override this in subclasses to handle specific pause logic
     */
    open fun onScreenPaused() {
        logd("${this.javaClass.simpleName}: onScreenPaused called")
        cancelAllScanning()
    }

    /**
     * Called when the screen is resumed
     * Override this in subclasses to handle specific resume logic
     */
    open fun onScreenResumed() {
        logd("${this.javaClass.simpleName}: onScreenResumed called")
    }

    /**
     * Cancels all active scanning operations
     */
    protected fun cancelAllScanning() {
        logd("${this.javaClass.simpleName}: Cancelling all scanning operations")
        dependencies.scanningLifecycleManager.cancelAllScanning()
    }

    /**
     * Triggers a toast message
     */
    protected fun triggerToast(message: String) {
        viewModelScope.launch {
            dependencies.toastRepository.sendToast(message)
        }
    }

    override fun onCleared() {
        logd("${this.javaClass.simpleName}: onCleared called")
        cancelAllScanning()
        dependencies.hardwareEventBus.unregisterListener(this)
        super.onCleared()
    }

    // Default empty implementations - ViewModels can override what they need
    override fun onTriggerDown() {
        logd("${this.javaClass.simpleName}: onTriggerDown called")
    }
    override fun onTriggerUp() {
        logd("${this.javaClass.simpleName}: onTriggerUp called")
    }
    override fun onSideKeyDown() {
        logd("${this.javaClass.simpleName}: onSideKeyDown called")
    }
    override fun onSideKeyUp() {
        logd("${this.javaClass.simpleName}: onSideKeyUp called")
    }
    override fun onAuxKeyDown() {
        logd("${this.javaClass.simpleName}: onAuxKeyDown called")
    }
    override fun onAuxKeyUp() {
        logd("${this.javaClass.simpleName}: onAuxKeyUp called")
    }

    // Audio methods
    fun blipBeep() { dependencies.audioService.playSuccess() }
    fun successBeep() { dependencies.audioService.playSuccess() }
    fun errorBeep() { dependencies.audioService.playError() }
}