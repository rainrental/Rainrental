package org.rainrental.rainrentalrfid.app

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import org.rainrental.rainrentalrfid.BuildConfig

@HiltAndroidApp
class RainRentalRfidApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase Crashlytics
        val crashlytics = FirebaseCrashlytics.getInstance()
        
        // Set user identifier (will be updated when user authenticates)
        crashlytics.setUserId("anonymous")
        
        // Set custom keys for debugging
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME ?: "unknown")
        crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE ?: "unknown")
    }
}


