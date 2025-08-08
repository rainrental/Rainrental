package org.rainrental.rainrentalrfid.logging

import android.util.Log

interface Logger {
    fun logi(msg:String){
        Log.i(this.javaClass.simpleName,msg)
    }
    fun logd(msg:String){
        Log.d(this.javaClass.simpleName,msg)
    }
    fun loge(msg:String){
        Log.e(this.javaClass.simpleName,msg)
    }
}