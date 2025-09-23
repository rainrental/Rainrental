package org.rainrental.rainrentalrfid.app

import android.app.Application
import android.os.Build
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import org.rainrental.rainrentalrfid.BuildConfig

@HiltAndroidApp
class RainRentalRfidApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase Crashlytics
        val crashlytics = FirebaseCrashlytics.getInstance()
        
        // Set device serial number as user ID
        val deviceSerial = getDeviceSerial()
        crashlytics.setUserId(deviceSerial)
        
        // Set custom keys for debugging
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME ?: "unknown")
        crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE ?: "unknown")
        crashlytics.setCustomKey("device_serial", deviceSerial)
        crashlytics.setCustomKey("device_model", Build.MODEL ?: "unknown")
        crashlytics.setCustomKey("android_version", Build.VERSION.RELEASE ?: "unknown")
    }
    
    private fun getDeviceSerial(): String {
        return try {
            // Try to get serial number (requires READ_PHONE_STATE permission on Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial()
            } else {
                @Suppress("DEPRECATION")
                Build.SERIAL
            }
        } catch (e: SecurityException) {
            // Fallback if permission not granted
            "unknown_serial"
        } catch (e: Exception) {
            // Fallback for any other error
            "unknown_serial"
        }
    }
}


