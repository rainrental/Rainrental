package org.rainrental.rainrentalrfid.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.app.deviceSerial
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject
import javax.inject.Singleton

// API Response Models
data class ValidateInvitationRequest(
    val invitationCode: String,
    val hostname: String,
    val deviceType: String,
    val companyId: String
)

data class ValidateInvitationResponse(
    val success: Boolean,
    val customToken: String?,
    val expiresIn: Int?,
    val locationName: String?,
    val companyId: String?,
    val rslId: String?,
    val error: String?
)

data class RefreshTokenResponse(
    val success: Boolean,
    val customToken: String?,
    val expiresIn: Int?,
    val locationName: String?,
    val companyId: String?,
    val rslId: String?,
    val error: String?
)

// API Interface
interface InvitationApiService {
    @POST("validateInvitation")
    suspend fun validateInvitation(
        @Body request: ValidateInvitationRequest
    ): Response<ValidateInvitationResponse>
    
    @POST("refreshToken")
    suspend fun refreshToken(
        @Header("Authorization") authorization: String
    ): Response<RefreshTokenResponse>
}

// Result Classes
sealed class AuthResult {
    data class Success(
        val user: FirebaseUser,
        val locationName: String?,
        val companyId: String?,
        val rslId: String?
    ) : AuthResult()
    
    data class Error(val message: String) : AuthResult()
}

sealed class TokenRefreshResult {
    data class Success(val user: FirebaseUser) : TokenRefreshResult()
    data class Error(val message: String) : TokenRefreshResult()
}

@Singleton
class InvitationAuthService @Inject constructor(
    private val apiService: InvitationApiService,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    
    /**
     * Validate invitation code and authenticate user
     */
    suspend fun validateInvitation(invitationCode: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val request = ValidateInvitationRequest(
                    invitationCode = invitationCode,
                    hostname = deviceSerial.ifEmpty { "unknown" },
                    deviceType = "mobile",
                    companyId = context.getString(R.string.company_id)
                )
                val response = apiService.validateInvitation(request)
                
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data?.success == true && data.customToken != null) {
                        // Sign in with custom token
                        val result = auth.signInWithCustomToken(data.customToken).await()
                        
                        if (result.user != null) {
                            AuthResult.Success(
                                user = result.user!!,
                                locationName = data.locationName,
                                companyId = data.companyId,
                                rslId = data.rslId
                            )
                        } else {
                            AuthResult.Error("Authentication failed")
                        }
                    } else {
                        AuthResult.Error(data?.error ?: "Unknown error")
                    }
                } else {
                    AuthResult.Error("Network error: ${response.code()}")
                }
            } catch (e: Exception) {
                AuthResult.Error("Exception: ${e.message}")
            }
        }
    }
    
    /**
     * Refresh Firebase custom token
     */
    suspend fun refreshToken(): TokenRefreshResult {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    return@withContext TokenRefreshResult.Error("No authenticated user")
                }
                
                val idToken = currentUser.getIdToken(false).await()
                val response = apiService.refreshToken("Bearer $idToken")
                
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data?.success == true && data.customToken != null) {
                        // Sign in with new custom token
                        val result = auth.signInWithCustomToken(data.customToken).await()
                        
                        if (result.user != null) {
                            TokenRefreshResult.Success(result.user!!)
                        } else {
                            TokenRefreshResult.Error("Token refresh failed")
                        }
                    } else {
                        TokenRefreshResult.Error(data?.error ?: "Unknown error")
                    }
                } else {
                    TokenRefreshResult.Error("Network error: ${response.code()}")
                }
            } catch (e: Exception) {
                TokenRefreshResult.Error("Exception: ${e.message}")
            }
        }
    }
    
    /**
     * Get current authentication state
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    /**
     * Sign out user
     */
    suspend fun signOut() {
        auth.signOut()
    }
} 