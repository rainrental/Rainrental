package org.rainrental.rainrentalrfid.settings.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.app.BaseViewModel
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.update.UpdateManager
import org.rainrental.rainrentalrfid.update.UpdateInfo
import org.rainrental.rainrentalrfid.settings.presentation.ButtonState
import org.rainrental.rainrentalrfid.auth.AuthState
import org.rainrental.rainrentalrfid.auth.AuthViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    dependencies: BaseViewModelDependencies,
    private val updateManager: UpdateManager
) : BaseViewModel(dependencies = dependencies), Logger {

    private val _mqttServerIp = MutableStateFlow("")
    val mqttServerIp: StateFlow<String> = _mqttServerIp.asStateFlow()

    private val _ignoreRightSideKey = MutableStateFlow(false)
    val ignoreRightSideKey: StateFlow<Boolean> = _ignoreRightSideKey.asStateFlow()

    // Update-related state
    private val _updateStatus = MutableStateFlow<String?>(null)
    val updateStatus: StateFlow<String?> = _updateStatus.asStateFlow()

    private val _updateProgress = MutableStateFlow(0f)
    val updateProgress: StateFlow<Float> = _updateProgress.asStateFlow()

    private val _isUpdateInProgress = MutableStateFlow(false)
    val isUpdateInProgress: StateFlow<Boolean> = _isUpdateInProgress.asStateFlow()

    // Button test states
    private val _triggerState = MutableStateFlow(ButtonState.UP)
    val triggerState: StateFlow<ButtonState> = _triggerState.asStateFlow()

    private val _sideState = MutableStateFlow(ButtonState.UP)
    val sideState: StateFlow<ButtonState> = _sideState.asStateFlow()

    private val _auxState = MutableStateFlow(ButtonState.UP)
    val auxState: StateFlow<ButtonState> = _auxState.asStateFlow()

    // Authentication state - will be set from outside
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Confirmation dialog state
    private val _showRevokeConfirmation = MutableStateFlow(false)
    val showRevokeConfirmation: StateFlow<Boolean> = _showRevokeConfirmation.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load MQTT server IP
            val serverIp = dependencies.appConfig.getMqttServerIp(dependencies.context)
            _mqttServerIp.value = serverIp
            
            // Load right side key setting
            val ignoreRightSide = dependencies.appConfig.isRightSideKeyIgnored()
            _ignoreRightSideKey.value = ignoreRightSide
        }
    }

    fun setMqttServerIp(serverIp: String) {
        _mqttServerIp.value = serverIp
        viewModelScope.launch {
            dependencies.appConfig.setMqttServerIp(dependencies.context, serverIp)
            logd("MQTT server IP updated to: $serverIp")
        }
    }

    fun setIgnoreRightSideKey(ignore: Boolean) {
        _ignoreRightSideKey.value = ignore
        dependencies.appConfig.setIgnoreRightSideKey(ignore)
        logd("Right side key ignore setting updated to: $ignore")
    }

    fun checkForUpdates(forceCheck: Boolean = false) {
        if (_isUpdateInProgress.value) {
            logd("Update already in progress")
            return
        }

        _isUpdateInProgress.value = true
        _updateStatus.value = "Checking for updates..."
        _updateProgress.value = 0f

        val companyId = dependencies.context.getString(R.string.company_id)
        updateManager.checkForUpdates(
            context = dependencies.context,
            companyId = companyId,
            forceCheck = forceCheck,
            onUpdateAvailable = { updateInfo ->
                _updateStatus.value = "Update available: ${updateInfo.version}. Downloading..."
                logd("Update available: ${updateInfo.version}")
            },
            onUpdateProgress = { progress ->
                _updateProgress.value = progress
                _updateStatus.value = "Downloading update... ${progress.toInt()}%"
            },
            onUpdateComplete = {
                _updateStatus.value = "Update downloaded successfully. Installation will begin..."
                _updateProgress.value = 100f
                logd("Update download completed")
            },
            onUpdateError = { error ->
                _updateStatus.value = "Update failed: $error"
                _updateProgress.value = 0f
                _isUpdateInProgress.value = false
                loge("Update error: $error")
            }
        )
    }

    fun clearUpdateStatus() {
        _updateStatus.value = null
        _updateProgress.value = 0f
        _isUpdateInProgress.value = false
    }

    // Button test event handlers
    override fun onTriggerDown() {
        logd("SettingsViewModel: onTriggerDown called")
        _triggerState.value = ButtonState.DOWN
    }

    override fun onTriggerUp() {
        logd("SettingsViewModel: onTriggerUp called")
        _triggerState.value = ButtonState.UP
    }

    override fun onSideKeyDown() {
        logd("SettingsViewModel: onSideKeyDown called")
        _sideState.value = ButtonState.DOWN
    }

    override fun onSideKeyUp() {
        logd("SettingsViewModel: onSideKeyUp called")
        _sideState.value = ButtonState.UP
    }

    override fun onAuxKeyDown() {
        logd("SettingsViewModel: onAuxKeyDown called")
        _auxState.value = ButtonState.DOWN
    }

    override fun onAuxKeyUp() {
        logd("SettingsViewModel: onAuxKeyUp called")
        _auxState.value = ButtonState.UP
    }

    // Authentication functions
    fun showRevokeConfirmation() {
        _showRevokeConfirmation.value = true
    }

    fun hideRevokeConfirmation() {
        _showRevokeConfirmation.value = false
    }

    fun setAuthState(authState: AuthState) {
        _authState.value = authState
    }

    fun revokeAuthentication() {
        // This will be handled by the MainApp
        _showRevokeConfirmation.value = false
    }
}
