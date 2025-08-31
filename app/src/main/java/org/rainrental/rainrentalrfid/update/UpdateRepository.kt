package org.rainrental.rainrentalrfid.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.rainrental.rainrentalrfid.app.AppConfig
import org.rainrental.rainrentalrfid.logging.Logger
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val appConfig: AppConfig
) : Logger {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Check for available app updates
     * @param companyId Company ID for the API call
     * @return UpdateInfo if update is available, null otherwise
     */
    suspend fun checkForUpdates(companyId: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            logd("=== UPDATE CHECK START ===")
            logd("Checking for updates for company: $companyId")
            logd("API URL: ${appConfig.Network.API_BASE_URL}/api/v1/appVersions/$companyId")
            
            val request = Request.Builder()
                .url("${appConfig.Network.API_BASE_URL}/api/v1/appVersions/$companyId")
                .addHeader("companyid", companyId)
                .get()
                .build()

            logd("Making HTTP request...")
            val response = client.newCall(request).execute()
            
            logd("Response received - Status: ${response.code}, Headers: ${response.headers}")
            
            if (!response.isSuccessful) {
                loge("UPDATE CHECK FAILED: HTTP ${response.code}")
                loge("Response body: ${response.body?.string()}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: run {
                loge("UPDATE CHECK FAILED: Empty response body")
                return@withContext null
            }
            
            logd("UPDATE API RESPONSE: $responseBody")
            
            val jsonResponse = JSONObject(responseBody)
            
            if (!jsonResponse.has("versions")) {
                loge("UPDATE CHECK FAILED: No 'versions' field in response")
                return@withContext null
            }
            
            val versions = jsonResponse.getJSONArray("versions")
            logd("Found ${versions.length()} versions in response")
            
            if (versions.length() == 0) {
                logd("No versions available in response")
                return@withContext null
            }

            // Find the version with the highest versionCode (latest version)
            var latestVersion: UpdateInfo? = null
            var highestVersionCode = 0
            
            logd("Analyzing versions...")
            for (i in 0 until versions.length()) {
                val version = versions.getJSONObject(i)
                val versionCode = version.getInt("versionCode")
                val versionName = version.getString("version")
                
                logd("Version $i: $versionName (code: $versionCode)")
                
                if (versionCode > highestVersionCode) {
                    highestVersionCode = versionCode
                    latestVersion = UpdateInfo(
                        version = versionName,
                        versionCode = versionCode,
                        downloadUrl = version.getString("downloadUrl"),
                        fileSize = version.getLong("fileSize"),
                        releaseNotes = version.optString("releaseNotes", ""),
                        minSdkVersion = version.optInt("minSdkVersion", 30),
                        targetSdkVersion = version.optInt("targetSdkVersion", 34)
                    )
                    logd("New highest version found: $versionName (code: $versionCode)")
                }
            }
            
            if (latestVersion != null) {
                logd("=== UPDATE CHECK SUCCESS ===")
                logd("Latest version available: ${latestVersion.version} (code: ${latestVersion.versionCode})")
                logd("Download URL: ${latestVersion.downloadUrl}")
                logd("File size: ${latestVersion.fileSize} bytes")
                return@withContext latestVersion
            } else {
                loge("UPDATE CHECK FAILED: No valid version found after analysis")
                return@withContext null
            }
            
        } catch (e: IOException) {
            loge("=== UPDATE CHECK NETWORK ERROR ===")
            loge("Network error during update check: ${e.message}")
            loge("Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            return@withContext null
        } catch (e: Exception) {
            loge("=== UPDATE CHECK GENERAL ERROR ===")
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
    val targetSdkVersion: Int
)
