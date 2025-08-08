package org.rainrental.rainrentalrfid.test.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.rainrental.rainrentalrfid.app.BaseViewModel
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

@HiltViewModel
class ButtonTestViewModel @Inject constructor(
    dependencies: BaseViewModelDependencies
) : BaseViewModel(dependencies), Logger {

    private val _triggerState = MutableStateFlow(ButtonState.UP)
    val triggerState: StateFlow<ButtonState> = _triggerState.asStateFlow()

    private val _sideState = MutableStateFlow(ButtonState.UP)
    val sideState: StateFlow<ButtonState> = _sideState.asStateFlow()

    private val _auxState = MutableStateFlow(ButtonState.UP)
    val auxState: StateFlow<ButtonState> = _auxState.asStateFlow()

    override fun onTriggerDown() {
        logd("ButtonTestViewModel: onTriggerDown called")
        _triggerState.value = ButtonState.DOWN
    }

    override fun onTriggerUp() {
        logd("ButtonTestViewModel: onTriggerUp called")
        _triggerState.value = ButtonState.UP
    }

    override fun onSideKeyDown() {
        logd("ButtonTestViewModel: onSideKeyDown called")
        _sideState.value = ButtonState.DOWN
    }

    override fun onSideKeyUp() {
        logd("ButtonTestViewModel: onSideKeyUp called")
        _sideState.value = ButtonState.UP
    }

    override fun onAuxKeyDown() {
        logd("ButtonTestViewModel: onAuxKeyDown called")
        _auxState.value = ButtonState.DOWN
    }

    override fun onAuxKeyUp() {
        logd("ButtonTestViewModel: onAuxKeyUp called")
        _auxState.value = ButtonState.UP
    }
}

enum class ButtonState {
    UP, DOWN
} 