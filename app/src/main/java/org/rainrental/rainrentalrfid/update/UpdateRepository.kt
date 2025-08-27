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
            logd("Checking for updates for company: $companyId")
            
            val request = Request.Builder()
                .url("${appConfig.Network.API_BASE_URL}/api/v1/appVersions/$companyId")
                .addHeader("companyid", companyId)
                .get()
                .build()

            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                loge("Update check failed with status: ${response.code}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            val jsonResponse = JSONObject(responseBody)
            
            val currentVersion = jsonResponse.optString("currentVersion")
            val versions = jsonResponse.getJSONArray("versions")
            
            if (currentVersion.isBlank()) {
                logd("No current version set")
                return@withContext null
            }

            // Find the current version in the versions array
            for (i in 0 until versions.length()) {
                val version = versions.getJSONObject(i)
                if (version.getString("version") == currentVersion) {
                    val updateInfo = UpdateInfo(
                        version = version.getString("version"),
                        versionCode = version.getInt("versionCode"),
                        downloadUrl = version.getString("downloadUrl"),
                        fileSize = version.getLong("fileSize"),
                        releaseNotes = version.optString("releaseNotes", ""),
                        minSdkVersion = version.optInt("minSdkVersion", 30),
                        targetSdkVersion = version.optInt("targetSdkVersion", 34)
                    )
                    
                    logd("Found update info: ${updateInfo.version}")
                    return@withContext updateInfo
                }
            }
            
            logd("No update found")
            return@withContext null
            
        } catch (e: IOException) {
            loge("Network error during update check: ${e.message}")
            return@withContext null
        } catch (e: Exception) {
            loge("Error checking for updates: ${e.message}")
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
