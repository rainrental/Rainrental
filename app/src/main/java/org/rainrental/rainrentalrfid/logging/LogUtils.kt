package org.rainrental.rainrentalrfid.logging

import android.util.Log
import org.rainrental.rainrentalrfid.BuildConfig

/**
 * Utility class for direct logging calls that should be replaced with Logger interface usage.
 * This class provides build-time optimized logging that is completely removed in release builds.
 */
object LogUtils {
    
    @JvmStatic
    fun logi(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg)
        }
    }
    
    @JvmStatic
    fun logd(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }
    
    @JvmStatic
    fun loge(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg)
        }
    }
}
