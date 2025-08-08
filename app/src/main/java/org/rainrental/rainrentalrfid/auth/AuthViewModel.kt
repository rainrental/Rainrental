package org.rainrental.rainrentalrfid.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: InvitationAuthService,
    private val auth: FirebaseAuth,
    private val scanInvitationCodeUseCase: ScanInvitationCodeUseCase,
    private val tokenRefreshScheduler: TokenRefreshScheduler,
    private val hardwareEventBus: HardwareEventBus
) : ViewModel(), HardwareEventListener {

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
                    
                    _authState.value = AuthState.Authenticated(
                        user = currentUser,
                        locationName = locationName,
                        companyId = companyId
                    )
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
                        _authState.value = AuthState.Authenticated(
                            user = result.user,
                            locationName = result.locationName,
                            companyId = result.companyId
                        )
                        // Start automatic token refresh
                        tokenRefreshScheduler.startAutoRefresh()
                    }
                    is AuthResult.Error -> {
                        _errorMessage.value = result.message
                        _authState.value = AuthState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Authentication failed: ${e.message}"
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
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
        viewModelScope.launch {
            // Stop automatic token refresh
            tokenRefreshScheduler.stopAutoRefresh()
            authService.signOut()
            _authState.value = AuthState.NotAuthenticated
            _errorMessage.value = null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
    
    fun resetAuth() {
        _authState.value = AuthState.NotAuthenticated
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
        hardwareEventBus.unregisterListener(this)
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
} 