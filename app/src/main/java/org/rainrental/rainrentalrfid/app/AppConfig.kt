package org.rainrental.rainrentalrfid.app

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfig @Inject constructor() {
    
    // RFID Configuration
    val Rfid = RfidConfig()
    
    // Audio Configuration
    val Audio = AudioConfig()
    
    // MQTT Configuration
    val Mqtt = MqttConfig()
    
    // Input Configuration
    val Input = InputConfig()
    
    // Hardware Keys
    val HardwareKeys = HardwareKeysConfig()
    
    // Network Configuration
    val Network = NetworkConfig()
    
    // UI Configuration
    val UI = UiConfig()
    
    /**
     * Gets the MQTT server IP using auto-detection with fallback
     */
    fun getMqttServerIp(context: Context): String {
        return NetworkUtils.getMqttServerIp(context)
    }
    
    /**
     * Gets the best available MQTT server by testing connectivity
     */
    suspend fun getBestMqttServer(context: Context): String {
        return NetworkUtils.getBestMqttServer(context)
    }
    
    /**
     * Sets whether the right side key should be ignored
     * @param ignore true to ignore right side key events, false to process them normally
     */
    fun setIgnoreRightSideKey(ignore: Boolean) {
        HardwareKeys.IGNORE_RIGHT_SIDE_KEY = ignore
    }
    
    /**
     * Gets whether the right side key is currently being ignored
     * @return true if right side key events are being ignored, false otherwise
     */
    fun isRightSideKeyIgnored(): Boolean {
        return HardwareKeys.IGNORE_RIGHT_SIDE_KEY
    }
}

data class RfidConfig(
    val DEFAULT_POWER: Int = 24,
    val WRITE_POWER: Int = 2,
    val HUNT_POWER: Int = 30,
    val FREQUENCY_MODE: Int = 4,
    val DEFAULT_EPC_LENGTH: Int = 24,
    val DEFAULT_TID_LENGTH: Int = 24
)

data class AudioConfig(
    val SOUND_PRIORITY: Int = 1,
    val SOUND_VOLUME: Float = 1.0f,
    val SOUND_RATE: Float = 1.0f,
    val SOUND_LOOP: Int = 0
)

data class MqttConfig(
    val KEEP_ALIVE_SECONDS: Int = 600,
    val CONNECTION_TIMEOUT: Int = 30,
    val MAX_RECONNECT_ATTEMPTS: Int = 3
)

data class InputConfig(
    val LONG_PRESS_THRESHOLD_MS: Long = 1000L,
    val SHORT_PRESS_THRESHOLD_MS: Long = 450L,
    val INPUT_CHECK_INTERVAL_MS: Long = 50L
)

data class HardwareKeysConfig(
    val TRIGGER_KEY_CODE: Int = 293,
    val SIDE_KEY_CODE: Int = 139,
    val AUX_KEY_CODE: Int = 142,
    var IGNORE_RIGHT_SIDE_KEY: Boolean = true
)

data class NetworkConfig(
    val MQTT_PORT: Int = 1883
)

data class UiConfig(
    val ANIMATION_DURATION_MS: Long = 300L,
    val TOAST_DURATION_MS: Long = 3000L
)

 