package org.rainrental.rainrentalrfid.mqtt

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.rainrental.rainrentalrfid.continuousScanning.MqttDeliveryService
import org.rainrental.rainrentalrfid.continuousScanning.data.DeliveryConnectionState
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.app.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MQTT Connection Watchdog Service
 * 
 * Monitors MQTT connection health and automatically attempts reconnection
 * when the connection is lost or becomes unhealthy.
 */
@Singleton
class MqttConnectionWatchdog @Inject constructor(
    private val mqttDeliveryService: MqttDeliveryService,
    private val appConfig: AppConfig
) : DefaultLifecycleObserver, Logger {
    
    private var watchdogJob: Job? = null
    private var isWatchdogActive = false
    
    // Watchdog configuration
    private val checkIntervalMs = 30_000L // Check every 30 seconds
    private val maxConsecutiveFailures = 3 // Max failures before giving up temporarily
    private val backoffMultiplier = 2.0 // Exponential backoff multiplier
    private val maxBackoffMs = 300_000L // Max 5 minutes between attempts
    
    // State tracking
    private val _watchdogState = MutableStateFlow(WatchdogState.STOPPED)
    val watchdogState: StateFlow<WatchdogState> = _watchdogState.asStateFlow()
    
    private val _lastCheckTime = MutableStateFlow(0L)
    val lastCheckTime: StateFlow<Long> = _lastCheckTime.asStateFlow()
    
    private val _consecutiveFailures = MutableStateFlow(0)
    val consecutiveFailures: StateFlow<Int> = _consecutiveFailures.asStateFlow()
    
    private val _nextCheckDelay = MutableStateFlow(checkIntervalMs)
    val nextCheckDelay: StateFlow<Long> = _nextCheckDelay.asStateFlow()
    
    private var lifecycleScope: CoroutineScope? = null
    
    /**
     * Start the MQTT connection watchdog
     */
    fun startWatchdog(context: Context) {
        if (isWatchdogActive) {
            logd("MQTT Watchdog: Already active, ignoring start request")
            return
        }
        
        logd("MQTT Watchdog: Starting connection monitoring")
        _watchdogState.value = WatchdogState.STARTING
        isWatchdogActive = true
        
        // Register lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // Start monitoring coroutine
        lifecycleScope = ProcessLifecycleOwner.get().lifecycle.coroutineScope
        watchdogJob = lifecycleScope?.launch(Dispatchers.IO) {
            startConnectionMonitoring(context)
        }
    }
    
    /**
     * Stop the MQTT connection watchdog
     */
    fun stopWatchdog() {
        if (!isWatchdogActive) {
            logd("MQTT Watchdog: Not active, ignoring stop request")
            return
        }
        
        logd("MQTT Watchdog: Stopping connection monitoring")
        _watchdogState.value = WatchdogState.STOPPING
        
        // Cancel monitoring job
        watchdogJob?.cancel()
        watchdogJob = null
        
        // Remove lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        
        isWatchdogActive = false
        _watchdogState.value = WatchdogState.STOPPED
        logd("MQTT Watchdog: Stopped successfully")
    }
    
    /**
     * Main monitoring loop
     */
    private suspend fun startConnectionMonitoring(context: Context) {
        _watchdogState.value = WatchdogState.RUNNING
        logd("MQTT Watchdog: Connection monitoring started")
        
        while (isActive && isWatchdogActive) {
            try {
                _lastCheckTime.value = System.currentTimeMillis()
                
                // Check MQTT connection health
                val isHealthy = checkConnectionHealth()
                
                if (isHealthy) {
                    // Connection is healthy, reset failure count
                    if (_consecutiveFailures.value > 0) {
                        logd("MQTT Watchdog: Connection restored after ${_consecutiveFailures.value} failures")
                        _consecutiveFailures.value = 0
                        _nextCheckDelay.value = checkIntervalMs
                    }
                } else {
                    // Connection is unhealthy, attempt reconnection
                    handleConnectionFailure(context)
                }
                
                // Wait for next check
                val delayMs = _nextCheckDelay.value
                logd("MQTT Watchdog: Next check in ${delayMs}ms")
                delay(delayMs)
                
            } catch (e: CancellationException) {
                logd("MQTT Watchdog: Monitoring cancelled")
                break
            } catch (e: Exception) {
                loge("MQTT Watchdog: Error during monitoring: ${e.message}")
                _consecutiveFailures.value = _consecutiveFailures.value + 1
                handleConnectionFailure(context)
                
                // Wait before retrying
                delay(_nextCheckDelay.value)
            }
        }
        
        _watchdogState.value = WatchdogState.STOPPED
        logd("MQTT Watchdog: Monitoring loop ended")
    }
    
    /**
     * Check if MQTT connection is healthy
     */
    private suspend fun checkConnectionHealth(): Boolean {
        return try {
            // Check if MQTT service reports connection
            val isConnected = mqttDeliveryService.isConnected()
            
            if (!isConnected) {
                logd("MQTT Watchdog: Connection check failed - not connected")
                return false
            }
            
            // Additional health check - try to check connection
            mqttDeliveryService.checkConnection()
            
            // Verify connection is still good after check
            val stillConnected = mqttDeliveryService.isConnected()
            if (!stillConnected) {
                logd("MQTT Watchdog: Connection lost during health check")
                return false
            }
            
            logd("MQTT Watchdog: Connection health check passed")
            true
            
        } catch (e: Exception) {
            loge("MQTT Watchdog: Health check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Handle connection failure and attempt reconnection
     */
    private suspend fun handleConnectionFailure(context: Context) {
        val currentFailures = _consecutiveFailures.value + 1
        _consecutiveFailures.value = currentFailures
        
        logd("MQTT Watchdog: Connection failure #$currentFailures")
        
        if (currentFailures > maxConsecutiveFailures) {
            // Too many failures, use exponential backoff
            val backoffDelay = (checkIntervalMs * kotlin.math.pow(backoffMultiplier, (currentFailures - maxConsecutiveFailures).toDouble())).toLong()
            _nextCheckDelay.value = kotlin.math.min(backoffDelay, maxBackoffMs)
            
            logd("MQTT Watchdog: Too many failures ($currentFailures), backing off for ${_nextCheckDelay.value}ms")
        } else {
            _nextCheckDelay.value = checkIntervalMs
        }
        
        // Attempt reconnection
        try {
            logd("MQTT Watchdog: Attempting MQTT reconnection")
            mqttDeliveryService.reDetectAndConnect(context, appConfig)
            
            // Give it a moment to connect
            delay(2000)
            
            // Check if reconnection was successful
            if (mqttDeliveryService.isConnected()) {
                logd("MQTT Watchdog: Reconnection successful")
                _consecutiveFailures.value = 0
                _nextCheckDelay.value = checkIntervalMs
            } else {
                logd("MQTT Watchdog: Reconnection failed")
            }
            
        } catch (e: Exception) {
            loge("MQTT Watchdog: Reconnection attempt failed: ${e.message}")
        }
    }
    
    /**
     * Force a connection check (useful for manual triggers)
     */
    suspend fun forceConnectionCheck(context: Context) {
        logd("MQTT Watchdog: Force connection check requested")
        _lastCheckTime.value = System.currentTimeMillis()
        
        val isHealthy = checkConnectionHealth()
        if (!isHealthy) {
            handleConnectionFailure(context)
        }
    }
    
    /**
     * Get current connection status
     */
    fun getConnectionStatus(): ConnectionStatus {
        return ConnectionStatus(
            isConnected = mqttDeliveryService.isConnected(),
            watchdogActive = isWatchdogActive,
            consecutiveFailures = _consecutiveFailures.value,
            lastCheckTime = _lastCheckTime.value,
            nextCheckDelay = _nextCheckDelay.value
        )
    }
    
    // Lifecycle callbacks
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        logd("MQTT Watchdog: App started")
        // Watchdog continues running when app comes to foreground
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        logd("MQTT Watchdog: App stopped")
        // Watchdog continues running when app goes to background
    }
}

/**
 * Watchdog state enumeration
 */
enum class WatchdogState {
    STOPPED,
    STARTING,
    RUNNING,
    STOPPING
}

/**
 * Connection status data class
 */
data class ConnectionStatus(
    val isConnected: Boolean,
    val watchdogActive: Boolean,
    val consecutiveFailures: Int,
    val lastCheckTime: Long,
    val nextCheckDelay: Long
)
