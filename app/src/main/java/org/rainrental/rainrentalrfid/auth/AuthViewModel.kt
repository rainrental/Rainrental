package org.rainrental.rainrentalrfid.auth

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.ScanInvitationCodeUseCase
import org.rainrental.rainrentalrfid.result.InputError
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.auth.TokenRefreshScheduler
import org.rainrental.rainrentalrfid.hardware.HardwareEventListener
import org.rainrental.rainrentalrfid.hardware.HardwareEventBus
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: InvitationAuthService,
    private val auth: FirebaseAuth,
    private val scanInvitationCodeUseCase: ScanInvitationCodeUseCase,
    private val tokenRefreshScheduler: TokenRefreshScheduler,
    private val hardwareEventBus: HardwareEventBus,
    private val authStateService: AuthStateService
) : ViewModel(), HardwareEventListener, Logger {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkAuthState()
        hardwareEventBus.registerListener(this)
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is authenticated, but we need to get location info from token claims
            viewModelScope.launch {
                try {
                    val idToken = currentUser.getIdToken(false).await()
                    val claims = idToken.claims
                    
                    val locationName = claims["locationName"] as? String
                    val companyId = claims["companyId"] as? String
                    val rslId = claims["rsl_id"] as? String
                    
                    val authenticatedState = AuthState.Authenticated(
                        user = currentUser,
                        locationName = locationName,
                        companyId = companyId,
                        rslId = rslId
                    )
                    _authState.value = authenticatedState
                    authStateService.updateAuthState(authenticatedState)
                    // Start automatic token refresh for existing sessions
                    tokenRefreshScheduler.startAutoRefresh()
                } catch (e: Exception) {
                    // Token might be invalid, sign out and show auth screen
                    tokenRefreshScheduler.stopAutoRefresh()
                    auth.signOut()
                    _authState.value = AuthState.NotAuthenticated
                }
            }
        } else {
            _authState.value = AuthState.NotAuthenticated
        }
    }

    fun validateInvitation(invitationCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = authService.validateInvitation(invitationCode)
                
                when (result) {
                    is AuthResult.Success -> {
                        val authenticatedState = AuthState.Authenticated(
                            user = result.user,
                            locationName = result.locationName,
                            companyId = result.companyId,
                            rslId = result.rslId
                        )
                        _authState.value = authenticatedState
                        authStateService.updateAuthState(authenticatedState)
                        
                        // Update Crashlytics user identification with device serial
                        val crashlytics = FirebaseCrashlytics.getInstance()
                        val deviceSerial = getDeviceSerial()
                        crashlytics.setUserId(deviceSerial)
                        crashlytics.setCustomKey("location_name", result.locationName ?: "unknown")
                        crashlytics.setCustomKey("company_id", result.companyId ?: "unknown")
                        crashlytics.setCustomKey("rsl_id", result.rslId ?: "unknown")
                        crashlytics.setCustomKey("firebase_uid", result.user.uid)
                        
                        // Start automatic token refresh
                        tokenRefreshScheduler.startAutoRefresh()
                    }
                    is AuthResult.Error -> {
                        _errorMessage.value = result.message
                        val errorState = AuthState.Error(result.message)
                        _authState.value = errorState
                        authStateService.updateAuthState(errorState)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Authentication failed: ${e.message}"
                val errorState = AuthState.Error(e.message ?: "Unknown error")
                _authState.value = errorState
                authStateService.updateAuthState(errorState)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            try {
                val result = authService.refreshToken()
                when (result) {
                    is TokenRefreshResult.Success -> {
                        // Token refreshed successfully, update state
                        checkAuthState()
                    }
                    is TokenRefreshResult.Error -> {
                        // Token refresh failed, sign out
                        tokenRefreshScheduler.stopAutoRefresh()
                        signOut()
                    }
                }
            } catch (e: Exception) {
                // Token refresh failed, sign out
                tokenRefreshScheduler.stopAutoRefresh()
                signOut()
            }
        }
    }

    fun signOut() {
        logd("AUTH VIEWMODEL: signOut called")
        viewModelScope.launch {
            // Stop automatic token refresh
            tokenRefreshScheduler.stopAutoRefresh()
            authService.signOut()
            
            // Clear Crashlytics user identification but keep device serial
            val crashlytics = FirebaseCrashlytics.getInstance()
            val deviceSerial = getDeviceSerial()
            crashlytics.setUserId(deviceSerial)
            crashlytics.setCustomKey("location_name", "unknown")
            crashlytics.setCustomKey("company_id", "unknown")
            crashlytics.setCustomKey("rsl_id", "unknown")
            crashlytics.setCustomKey("firebase_uid", "unknown")
            
            logd("AUTH VIEWMODEL: Setting auth state to NotAuthenticated")
            val notAuthenticatedState = AuthState.NotAuthenticated
            _authState.value = notAuthenticatedState
            authStateService.updateAuthState(notAuthenticatedState)
            _errorMessage.value = null
            logd("AUTH VIEWMODEL: Sign out completed")
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
    
    fun resetAuth() {
        val notAuthenticatedState = AuthState.NotAuthenticated
        _authState.value = notAuthenticatedState
        authStateService.updateAuthState(notAuthenticatedState)
        _errorMessage.value = null
        _isLoading.value = false
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    suspend fun scanInvitationCode(): Result<String, InputError> {
        return scanInvitationCodeUseCase()
    }
    
    override fun onCleared() {
        super.onCleared()
        // Hardware event unregistration is now handled by MainApp
        // hardwareEventBus.unregisterListener(this)
    }

    override fun onTriggerUp() {
        triggerBarcodeScan()
    }
    
    override fun onSideKeyUp() {
        triggerBarcodeScan()
    }
    
    private fun triggerBarcodeScan() {
        // Only trigger scanning if we're not authenticated and not currently loading
        if (_authState.value == AuthState.NotAuthenticated && !_isLoading.value) {
            viewModelScope.launch {
                try {
                    val result = scanInvitationCode()
                    when (result) {
                        is Result.Success -> {
                            validateInvitation(result.data)
                        }
                        is Result.Error -> {
                            _errorMessage.value = "Scan failed: ${result.error}"
                        }
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Scan error: ${e.message}"
                }
            }
        }
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