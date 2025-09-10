package org.rainrental.rainrentalrfid.auth

import kotlinx.coroutines.*
import org.rainrental.rainrentalrfid.logging.LogUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshScheduler @Inject constructor(
    private val authService: InvitationAuthService
) {
    private var refreshJob: Job? = null
    private val refreshInterval = 45 * 60 * 1000L // 45 minutes in milliseconds
    
    fun startAutoRefresh() {
        stopAutoRefresh() // Stop any existing job
        
        LogUtils.logd("TokenRefreshScheduler", "Starting automatic token refresh")
        refreshJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    delay(refreshInterval)
                    LogUtils.logd("TokenRefreshScheduler", "Refreshing token...")
                    authService.refreshToken()
                    LogUtils.logd("TokenRefreshScheduler", "Token refreshed successfully")
                } catch (e: Exception) {
                    LogUtils.loge("TokenRefreshScheduler", "Token refresh failed: ${e.message}")
                    // If refresh fails, stop the scheduler
                    // The user will need to re-authenticate
                    break
                }
            }
        }
    }
    
    fun stopAutoRefresh() {
        LogUtils.logd("TokenRefreshScheduler", "Stopping automatic token refresh")
        refreshJob?.cancel()
        refreshJob = null
    }
    
    fun isRefreshing(): Boolean {
        return refreshJob?.isActive == true
    }
} 