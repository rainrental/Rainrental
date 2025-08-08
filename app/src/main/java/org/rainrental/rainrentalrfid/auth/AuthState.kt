package org.rainrental.rainrentalrfid.auth

import com.google.firebase.auth.FirebaseUser

sealed class AuthState {
    object Loading : AuthState()
    object NotAuthenticated : AuthState()
    data class Authenticated(
        val user: FirebaseUser,
        val locationName: String?,
        val companyId: String?
    ) : AuthState()
    data class Error(val message: String) : AuthState()
} 