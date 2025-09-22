package org.rainrental.rainrentalrfid.update

import android.content.Context
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
                val versionCodeInt = version.getVersionCodeAsInt()
                if (versionCodeInt > highestVersionCode) {
                    highestVersionCode = versionCodeInt
                    latestVersion = UpdateInfo(
                        version = version.version,
                        versionCode = versionCodeInt,
                        downloadUrl = version.downloadUrl,
                        fileSize = version.fileSize,
                        releaseNotes = version.releaseNotes,
                        minSdkVersion = version.minSdkVersion,
                        targetSdkVersion = version.targetSdkVersion,
                        isCurrent = version.isCurrent,
                        fileName = "app-${version.version}.apk"
                    )
                    logd("New highest version found: ${version.version} (code: $versionCodeInt)")
                }
                
                // Track the version marked as current
                if (version.isCurrent) {
                    val versionCodeInt = version.getVersionCodeAsInt()
                    currentVersion = UpdateInfo(
                        version = version.version,
                        versionCode = versionCodeInt,
                        downloadUrl = version.downloadUrl,
                        fileSize = version.fileSize,
                        releaseNotes = version.releaseNotes,
                        minSdkVersion = version.minSdkVersion,
                        targetSdkVersion = version.targetSdkVersion,
                        isCurrent = version.isCurrent,
                        fileName = "app-${version.version}.apk"
                    )
                    logd("Found current version: ${version.version} (code: $versionCodeInt)")
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
     * Download APK file from URL
     * @param downloadUrl URL to download the APK from
     * @param fileSize Expected file size for progress tracking
     * @param fileName Desired filename for the downloaded APK
     * @param progressCallback Callback for download progress
     * @return Downloaded file or null if failed
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        fileSize: Long,
        fileName: String,
        progressCallback: (Float) -> Unit
    ): java.io.File? = withContext(Dispatchers.IO) {
        try {
            logd("Starting APK download from: $downloadUrl")
            
            val request = Request.Builder()
                .url(downloadUrl)
                .get()
                .addHeader("User-Agent", "RainRental-RFID-Android/1.0")
                .addHeader("Accept", "application/vnd.android.package-archive, */*")
                .addHeader("Accept-Encoding", "identity") // Disable compression to get accurate file size
                .build()

            val response = client.newCall(request).execute()
            
            logd("Download response: code=${response.code}, message=${response.message}")
            logd("Response headers: ${response.headers}")
            
            if (!response.isSuccessful) {
                loge("Download failed with status: ${response.code}")
                loge("Response body: ${response.body?.string()}")
                return@withContext null
            }

            val responseBody = response.body ?: return@withContext null
            val contentLength = responseBody.contentLength()
            
            logd("Content-Length header: $contentLength")
            
            if (contentLength <= 0) {
                loge("Invalid content length: $contentLength")
                return@withContext null
            }

            // Create file with proper filename in cache directory
            val cacheDir = java.io.File(context.cacheDir, "updates")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val tempFile = java.io.File(cacheDir, fileName)
            logd("Creating APK file: ${tempFile.absolutePath}")
            
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
                
                // Verify the downloaded file is a valid APK by checking ZIP signature
                try {
                    val inputStream = tempFile.inputStream()
                    val buffer = ByteArray(4)
                    inputStream.read(buffer)
                    inputStream.close()
                    
                    val zipSignature = (buffer[0].toInt() and 0xFF) or 
                                     ((buffer[1].toInt() and 0xFF) shl 8) or 
                                     ((buffer[2].toInt() and 0xFF) shl 16) or 
                                     ((buffer[3].toInt() and 0xFF) shl 24)
                    
                    val isValidZip = zipSignature == 0x04034B50
                    logd("Download verification: ZIP signature check: $isValidZip (signature: 0x${zipSignature.toString(16)})")
                    
                    if (!isValidZip) {
                        loge("Downloaded file is not a valid APK/ZIP file!")
                        return@withContext null
                    }
                } catch (e: Exception) {
                    loge("Error verifying downloaded APK: ${e.message}")
                    return@withContext null
                }
                
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
    val isCurrent: Boolean = false,
    val fileName: String = "app-${version}.apk"
)
