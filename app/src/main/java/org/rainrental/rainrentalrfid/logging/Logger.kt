package org.rainrental.rainrentalrfid.logging

import android.util.Log
import org.rainrental.rainrentalrfid.BuildConfig

interface Logger {
    fun logi(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.i(this.javaClass.simpleName, msg)
        }
    }
    
    fun logd(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(this.javaClass.simpleName, msg)
        }
    }
    
    fun loge(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(this.javaClass.simpleName, msg)
        }
    }
}