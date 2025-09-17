package org.rainrental.rainrentalrfid.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.rainrental.rainrentalrfid.commission.data.BackendApi
import org.rainrental.rainrentalrfid.commission.data.AppVersionsRequestDto
import org.rainrental.rainrentalrfid.commission.data.AppVersionsResponseDto
import org.rainrental.rainrentalrfid.commission.data.AppVersionDto
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.result.ApiError
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.update.UpdateInfo
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val backendApi: BackendApi
) : Logger {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Check for available app updates
     * @param companyId Company ID for the API call
     * @param useTestMode If true, returns mock data instead of calling real API
     * @return UpdateInfo if update is available, null otherwise
     */
    suspend fun checkForUpdates(companyId: String, useTestMode: Boolean = false): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            logd("=== UPDATE CHECK START ===")
            logd("Checking for updates for company: $companyId")
            logd("Test mode: $useTestMode")
            
            // Test mode - return mock data for testing
            if (useTestMode) {
                logd("=== USING TEST MODE - RETURNING MOCK DATA ===")
                return@withContext UpdateInfo(
                    version = "1.0.99",
                    versionCode = 10099,
                    downloadUrl = "https://example.com/test-update.apk",
                    fileSize = 1024 * 1024 * 10, // 10MB
                    releaseNotes = "Test update for auto-update functionality",
                    minSdkVersion = 30,
                    targetSdkVersion = 34
                )
            }
            
            logd("Calling backendApi.getAppVersions...")
            val response = backendApi.getAppVersions(companyId)
            
            logd("Response received - Code: ${response.code()}")
            
            if (!response.isSuccessful) {
                loge("UPDATE CHECK FAILED: HTTP ${response.code()}")
                loge("Response body: ${response.errorBody()?.string()}")
                return@withContext null
            }

            val responseBody = response.body()
            if (responseBody == null) {
                loge("UPDATE CHECK FAILED: Null response body")
                return@withContext null
            }
            
            logd("UPDATE API RESPONSE: success=${responseBody.success}, message=${responseBody.message}")
            logd("Found ${responseBody.versions.size} versions in response")
            
            if (!responseBody.success) {
                loge("UPDATE CHECK FAILED: API returned success=false, message=${responseBody.message}")
                return@withContext null
            }
            
            if (responseBody.versions.isEmpty()) {
                logd("No versions available in response")
                return@withContext null
            }

            // Smart update logic: prefer newer versions, then fall back to isCurrent
            var latestVersion: UpdateInfo? = null
            var highestVersionCode = 0
            var currentVersion: UpdateInfo? = null
            
            logd("Analyzing versions...")
            for (version in responseBody.versions) {
                logd("Version: ${version.version} (code: ${version.versionCode}, isCurrent: ${version.isCurrent})")
                
                // Track the version with highest versionCode (newest)
                if (version.versionCode > highestVersionCode) {
                    highestVersionCode = version.versionCode
                    latestVersion = UpdateInfo(
                        version = version.version,
                        versionCode = version.versionCode,
                        downloadUrl = version.downloadUrl,
                        fileSize = version.fileSize,
                        releaseNotes = version.releaseNotes,
                        minSdkVersion = version.minSdkVersion,
                        targetSdkVersion = version.targetSdkVersion,
                        isCurrent = version.isCurrent
                    )
                    logd("New highest version found: ${version.version} (code: ${version.versionCode})")
                }
                
                // Track the version marked as current
                if (version.isCurrent) {
                    currentVersion = UpdateInfo(
                        version = version.version,
                        versionCode = version.versionCode,
                        downloadUrl = version.downloadUrl,
                        fileSize = version.fileSize,
                        releaseNotes = version.releaseNotes,
                        minSdkVersion = version.minSdkVersion,
                        targetSdkVersion = version.targetSdkVersion,
                        isCurrent = version.isCurrent
                    )
                    logd("Found current version: ${version.version} (code: ${version.versionCode})")
                }
            }
            
            // Choose update strategy: prefer newer versions, fall back to current
            val selectedVersion = if (latestVersion != null) {
                logd("Using latest version strategy: ${latestVersion.version} (code: ${latestVersion.versionCode})")
                latestVersion
            } else if (currentVersion != null) {
                logd("Falling back to current version strategy: ${currentVersion.version} (code: ${currentVersion.versionCode})")
                currentVersion
            } else {
                logd("No suitable version found")
                null
            }
            
            if (selectedVersion != null) {
                logd("=== UPDATE CHECK SUCCESS ===")
                logd("Selected version: ${selectedVersion.version} (code: ${selectedVersion.versionCode}, isCurrent: ${selectedVersion.isCurrent})")
                logd("Download URL: ${selectedVersion.downloadUrl}")
                logd("File size: ${selectedVersion.fileSize} bytes")
                return@withContext selectedVersion
            } else {
                loge("UPDATE CHECK FAILED: No valid version found after analysis")
                return@withContext null
            }
            
        } catch (e: Exception) {
            loge("=== UPDATE CHECK ERROR ===")
            loge("Error checking for updates: ${e.message}")
            loge("Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Test API connectivity without downloading
     */
    suspend fun testConnectivity(companyId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            logd("=== API CONNECTIVITY TEST ===")
            logd("Testing connectivity for company: $companyId")
            
            logd("Calling backendApi.getAppVersions for connectivity test...")
            val response = backendApi.getAppVersions(companyId)
            
            logd("Response received - Code: ${response.code()}")
            
            val isSuccessful = response.isSuccessful
            logd("Connectivity test result: $isSuccessful")
            return@withContext isSuccessful
            
        } catch (e: Exception) {
            loge("=== API CONNECTIVITY TEST FAILED ===")
            loge("Error testing connectivity: ${e.message}")
            loge("Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Download APK file from URL
     * @param downloadUrl URL to download the APK from
     * @param fileSize Expected file size for progress tracking
     * @param progressCallback Callback for download progress
     * @return Downloaded file or null if failed
     */
    suspend fun downloadApk(
        downloadUrl: String,
        fileSize: Long,
        progressCallback: (Float) -> Unit
    ): java.io.File? = withContext(Dispatchers.IO) {
        try {
            logd("Starting APK download from: $downloadUrl")
            
            val request = Request.Builder()
                .url(downloadUrl)
                .get()
                .build()

            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                loge("Download failed with status: ${response.code}")
                return@withContext null
            }

            val responseBody = response.body ?: return@withContext null
            val contentLength = responseBody.contentLength()
            
            if (contentLength <= 0) {
                loge("Invalid content length: $contentLength")
                return@withContext null
            }

            // Create temporary file
            val tempFile = java.io.File.createTempFile("app_update_", ".apk")
            tempFile.deleteOnExit()
            
            val inputStream = responseBody.byteStream()
            val outputStream = tempFile.outputStream()
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L
            
            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    
                    val progress = (totalBytesRead.toFloat() / contentLength) * 100
                    progressCallback(progress)
                }
                
                outputStream.flush()
                logd("APK download completed: ${tempFile.length()} bytes")
                return@withContext tempFile
                
            } finally {
                inputStream.close()
                outputStream.close()
            }
            
        } catch (e: IOException) {
            loge("Network error during APK download: ${e.message}")
            return@withContext null
        } catch (e: Exception) {
            loge("Error downloading APK: ${e.message}")
            return@withContext null
        }
    }
}

/**
 * Data class representing update information
 */
data class UpdateInfo(
    val version: String,
    val versionCode: Int,
    val downloadUrl: String,
    val fileSize: Long,
    val releaseNotes: String,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val isCurrent: Boolean = false
)
