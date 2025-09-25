package org.rainrental.rainrentalrfid.settings.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.app.BaseViewModel
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.update.UpdateManager
import org.rainrental.rainrentalrfid.update.UpdateInfo
import org.rainrental.rainrentalrfid.update.UpdateRepository
import org.rainrental.rainrentalrfid.settings.presentation.ButtonState
import org.rainrental.rainrentalrfid.auth.AuthState
import org.rainrental.rainrentalrfid.auth.AuthViewModel
import org.rainrental.rainrentalrfid.continuousScanning.MqttDeliveryService
import org.rainrental.rainrentalrfid.mqtt.MqttConnectionWatchdog
import org.rainrental.rainrentalrfid.mqtt.WatchdogState
import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    dependencies: BaseViewModelDependencies,
    private val updateManager: UpdateManager,
    private val updateRepository: UpdateRepository,
    private val mqttDeliveryService: MqttDeliveryService,
    private val mqttWatchdog: MqttConnectionWatchdog,
    @ApplicationContext private val context: Context
) : BaseViewModel(dependencies = dependencies), Logger {

    private val _mqttServerIp = MutableStateFlow("")
    val mqttServerIp: StateFlow<String> = _mqttServerIp.asStateFlow()
    
    // Debounced job for MQTT server IP changes
    private var mqttServerIpChangeJob: Job? = null

    private val _ignoreRightSideKey = MutableStateFlow(false)
    val ignoreRightSideKey: StateFlow<Boolean> = _ignoreRightSideKey.asStateFlow()

    // Volume control state
    private val _systemVolume = MutableStateFlow(0)
    val systemVolume: StateFlow<Int> = _systemVolume.asStateFlow()
    
    private val _maxSystemVolume = MutableStateFlow(15)
    val maxSystemVolume: StateFlow<Int> = _maxSystemVolume.asStateFlow()
    
    // MQTT Watchdog state
    val watchdogState: StateFlow<WatchdogState> = mqttWatchdog.watchdogState
    val consecutiveFailures: StateFlow<Int> = mqttWatchdog.consecutiveFailures
    val lastCheckTime: StateFlow<Long> = mqttWatchdog.lastCheckTime
    val nextCheckDelay: StateFlow<Long> = mqttWatchdog.nextCheckDelay
    val isPaused: StateFlow<Boolean> = mqttWatchdog.isPaused
    
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    init {
        // Initialize volume state
        _maxSystemVolume.value = getMaxSystemVolume()
        getCurrentSystemVolume()
    }

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
    
    // Debug counter to force UI recomposition
    private val _buttonPressCount = MutableStateFlow(0)
    val buttonPressCount: StateFlow<Int> = _buttonPressCount.asStateFlow()

    // Authentication state - will be set from outside
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Confirmation dialog state
    private val _showRevokeConfirmation = MutableStateFlow(false)
    val showRevokeConfirmation: StateFlow<Boolean> = _showRevokeConfirmation.asStateFlow()

    // Installed app version
    private val _installedVersion = MutableStateFlow("Unknown")
    val installedVersion: StateFlow<String> = _installedVersion.asStateFlow()

    private val _backendVersionData = MutableStateFlow<String?>(null)
    val backendVersionData: StateFlow<String?> = _backendVersionData.asStateFlow()

    init {
        loadSettings()
        loadInstalledVersion()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load MQTT server IP
            val serverIp = dependencies.appConfig.getMqttServerIp(dependencies.context)
            _mqttServerIp.value = serverIp
            
            // Load right side key setting
            val ignoreRightSide = dependencies.appConfig.isRightSideKeyIgnored(dependencies.context)
            _ignoreRightSideKey.value = ignoreRightSide
        }
    }

    private fun loadInstalledVersion() {
        viewModelScope.launch {
            try {
                val packageInfo = dependencies.context.packageManager.getPackageInfo(dependencies.context.packageName, 0)
                val versionName = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.versionName
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionName
                }
                _installedVersion.value = versionName
                logd("Loaded installed version: $versionName")
            } catch (e: Exception) {
                _installedVersion.value = "Error"
                loge("Failed to load installed version: ${e.message}")
            }
        }
    }

    fun setMqttServerIp(serverIp: String) {
        _mqttServerIp.value = serverIp
        
        // Cancel any existing debounced job
        mqttServerIpChangeJob?.cancel()
        
        // Start a new debounced job
        mqttServerIpChangeJob = viewModelScope.launch {
            // Wait for 1 second of no changes before triggering reconnection
            delay(1000)
            
            dependencies.appConfig.setMqttServerIp(dependencies.context, serverIp)
            logd("MQTT server IP updated to: $serverIp")
            
            // Trigger MQTT reconnection with new server IP
            try {
                logd("Triggering MQTT reconnection with new server: $serverIp")
                mqttDeliveryService.restartWithNewServer(serverIp)
                logd("MQTT reconnection initiated successfully")
            } catch (e: Exception) {
                loge("Failed to trigger MQTT reconnection: ${e.message}")
            }
        }
    }

    fun setIgnoreRightSideKey(ignore: Boolean) {
        _ignoreRightSideKey.value = ignore
        dependencies.appConfig.setIgnoreRightSideKey(dependencies.context, ignore)
        logd("Right side key ignore setting updated to: $ignore")
    }

    fun checkForUpdates(forceCheck: Boolean = false) {
        logd("=== SETTINGS VIEWMODEL: checkForUpdates called ===")
        logd("Force check: $forceCheck")
        logd("Current update in progress: ${_isUpdateInProgress.value}")
        
        if (_isUpdateInProgress.value) {
            logd("SETTINGS VIEWMODEL: Update already in progress, returning early")
            return
        }

        logd("SETTINGS VIEWMODEL: Setting update in progress to true")
        _isUpdateInProgress.value = true
        _updateStatus.value = "Checking for updates..."
        _updateProgress.value = 0f

        val companyId = dependencies.context.getString(R.string.company_id)
        logd("SETTINGS VIEWMODEL: Company ID: $companyId")
        logd("SETTINGS VIEWMODEL: Calling updateManager.checkForUpdates")
        
        updateManager.checkForUpdates(
            context = dependencies.context,
            companyId = companyId,
            forceCheck = forceCheck,
            onUpdateAvailable = { updateInfo ->
                logd("SETTINGS VIEWMODEL: onUpdateAvailable callback called")
                _updateStatus.value = "Update available: ${updateInfo.version}. Downloading..."
                _backendVersionData.value = "Available: ${updateInfo.version} (code: ${updateInfo.versionCode}), URL: ${updateInfo.downloadUrl}, Size: ${updateInfo.fileSize} bytes"
                logd("Update available: ${updateInfo.version}")
            },
            onUpdateProgress = { progress ->
                logd("SETTINGS VIEWMODEL: onUpdateProgress callback called: $progress")
                _updateProgress.value = progress
                _updateStatus.value = "Downloading update... ${progress.toInt()}%"
            },
            onUpdateComplete = {
                logd("SETTINGS VIEWMODEL: onUpdateComplete callback called")
                _updateStatus.value = "Update downloaded successfully. Installation will begin..."
                _updateProgress.value = 100f
                logd("Update download completed")
            },
            onUpdateError = { error ->
                logd("SETTINGS VIEWMODEL: onUpdateError callback called: $error")
                _updateStatus.value = "Update failed: $error"
                _updateProgress.value = 0f
                _isUpdateInProgress.value = false
                _backendVersionData.value = "Error: $error"
                loge("Update error: $error")
            },
            onNoUpdateAvailable = {
                logd("SETTINGS VIEWMODEL: onNoUpdateAvailable callback called")
                _updateStatus.value = "No updates available. You have the latest version."
                _updateProgress.value = 0f
                _isUpdateInProgress.value = false
                _backendVersionData.value = "No update available from backend"
                logd("No updates available")
            }
        )
        
        logd("SETTINGS VIEWMODEL: checkForUpdates function completed")
    }

    fun clearUpdateStatus() {
        _updateStatus.value = null
        _updateProgress.value = 0f
        _isUpdateInProgress.value = false
        _backendVersionData.value = null
    }


    // Button test event handlers
    override fun onTriggerDown() {
        logd("SettingsViewModel: onTriggerDown called")
        logd("SettingsViewModel: Current thread: ${Thread.currentThread().name}")
        logd("SettingsViewModel: Before setting trigger state to DOWN")
        _triggerState.value = ButtonState.DOWN
        logd("SettingsViewModel: After setting trigger state to DOWN")
        _buttonPressCount.value = _buttonPressCount.value + 1
        logd("SettingsViewModel: Set trigger state to DOWN, press count: ${_buttonPressCount.value}")
        logd("SettingsViewModel: Current trigger state: ${_triggerState.value}")
        logd("SettingsViewModel: Current button press count: ${_buttonPressCount.value}")
    }

    override fun onTriggerUp() {
        logd("SettingsViewModel: onTriggerUp called")
        _triggerState.value = ButtonState.UP
        logd("SettingsViewModel: Set trigger state to UP")
    }

    override fun onSideKeyDown() {
        logd("SettingsViewModel: onSideKeyDown called")
        _sideState.value = ButtonState.DOWN
        logd("SettingsViewModel: Set side state to DOWN")
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
        logd("SETTINGS VIEWMODEL: setAuthState called with: $authState")
        _authState.value = authState
    }

    fun revokeAuthentication() {
        logd("SETTINGS VIEWMODEL: revokeAuthentication called")
        _showRevokeConfirmation.value = false
    }
    
    // Volume control functions
    fun getCurrentSystemVolume(): Int {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        _systemVolume.value = currentVolume
        logd("Current system volume: $currentVolume")
        return currentVolume
    }
    
    fun setSystemVolume(volume: Int) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val clampedVolume = volume.coerceIn(0, maxVolume)
        
        // Set all volume streams to the same level
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clampedVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_RING, clampedVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, clampedVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, clampedVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, clampedVolume, 0)
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, clampedVolume, 0)
        
        _systemVolume.value = clampedVolume
        logd("System volume set to: $clampedVolume (max: $maxVolume)")
    }
    
    fun getMaxSystemVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }
    
    /**
     * Force MQTT connection check
     */
    fun forceMqttConnectionCheck() {
        viewModelScope.launch {
            logd("SettingsViewModel: Force MQTT connection check")
            mqttWatchdog.forceConnectionCheck()
        }
    }
    
    /**
     * Get MQTT connection status
     */
    fun getMqttConnectionStatus() = mqttWatchdog.getConnectionStatus()
    
}
