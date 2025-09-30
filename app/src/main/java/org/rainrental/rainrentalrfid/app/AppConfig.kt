package org.rainrental.rainrentalrfid.app

import android.content.Context
import android.content.SharedPreferences
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
    
    private var sharedPreferences: SharedPreferences? = null
    
    private fun getSharedPreferences(context: Context): SharedPreferences {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("RainRentalPrefs", Context.MODE_PRIVATE)
        }
        return sharedPreferences!!
    }
    
    /**
     * Gets the MQTT server IP from persistent storage
     */
    fun getMqttServerIp(context: Context): String {
        return getSharedPreferences(context).getString("mqtt_server_ip", "192.168.1.100") ?: "192.168.1.100"
    }
    
    /**
     * Sets the MQTT server IP in persistent storage
     */
    fun setMqttServerIp(context: Context, serverIp: String) {
        getSharedPreferences(context).edit().putString("mqtt_server_ip", serverIp).apply()
    }
    
    /**
     * Sets whether the right side key should be ignored
     * @param ignore true to ignore right side key events, false to process them normally
     */
    fun setIgnoreRightSideKey(context: Context, ignore: Boolean) {
        HardwareKeys.IGNORE_RIGHT_SIDE_KEY = ignore
        getSharedPreferences(context).edit().putBoolean("ignore_right_side_key", ignore).apply()
    }
    
    /**
     * Gets whether the right side key is currently being ignored
     * @return true if right side key events are being ignored, false otherwise
     */
    fun isRightSideKeyIgnored(context: Context): Boolean {
        val persistedValue = getSharedPreferences(context).getBoolean("ignore_right_side_key", false)
        HardwareKeys.IGNORE_RIGHT_SIDE_KEY = persistedValue
        return persistedValue
    }
    
    /**
     * Sets whether the logo animation has been shown in this app session
     * @param shown true if logo animation has been shown, false otherwise
     */
    fun setLogoAnimationShown(context: Context, shown: Boolean) {
        getSharedPreferences(context).edit().putBoolean("logo_animation_shown", shown).apply()
    }
    
    /**
     * Gets whether the logo animation has been shown in this app session
     * @return true if logo animation has been shown, false otherwise
     */
    fun isLogoAnimationShown(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean("logo_animation_shown", false)
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
    var IGNORE_RIGHT_SIDE_KEY: Boolean = false
)

data class NetworkConfig(
    val MQTT_PORT: Int = 1883,
    val API_BASE_URL: String = "https://europe-west2-rainrental-org.cloudfunctions.net"
)

data class UiConfig(
    val ANIMATION_DURATION_MS: Long = 300L,
    val TOAST_DURATION_MS: Long = 3000L
)

 