package org.rainrental.rainrentalrfid.apis.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class SafeLoggingInterceptor : Interceptor {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            loggingInterceptor.intercept(chain)
        } catch (e: IOException) {
            Log.e("SafeLoggingInterceptor", "IOException during logging: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("SafeLoggingInterceptor", "Exception during logging: ${e.message}")
            throw e
        }
    }
}