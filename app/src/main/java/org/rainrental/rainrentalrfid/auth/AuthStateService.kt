package org.rainrental.rainrentalrfid.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthStateService @Inject constructor() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun updateAuthState(authState: AuthState) {
        _authState.value = authState
    }

    fun getLocationName(): String? {
        return when (val currentState = _authState.value) {
            is AuthState.Authenticated -> currentState.locationName
            else -> null
        }
    }

    fun getRslId(): String? {
        return when (val currentState = _authState.value) {
            is AuthState.Authenticated -> currentState.rslId
            else -> null
        }
    }
}

