package org.rainrental.rainrentalrfid.settings.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareStateRepository @Inject constructor() {
    
    // Button test states - singleton source of truth
    private val _triggerState = MutableStateFlow(ButtonState.UP)
    val triggerState: StateFlow<ButtonState> = _triggerState.asStateFlow()

    private val _sideState = MutableStateFlow(ButtonState.UP)
    val sideState: StateFlow<ButtonState> = _sideState.asStateFlow()

    private val _auxState = MutableStateFlow(ButtonState.UP)
    val auxState: StateFlow<ButtonState> = _auxState.asStateFlow()
    
    // Test counter to verify state changes are working
    private val _testCounter = MutableStateFlow(0)
    val testCounter: StateFlow<Int> = _testCounter.asStateFlow()
    
    fun setTriggerState(state: ButtonState) {
        _triggerState.value = state
        if (state == ButtonState.DOWN) {
            _testCounter.value = _testCounter.value + 1
        }
    }
    
    fun setSideState(state: ButtonState) {
        _sideState.value = state
    }
    
    fun setAuxState(state: ButtonState) {
        _auxState.value = state
    }
}
