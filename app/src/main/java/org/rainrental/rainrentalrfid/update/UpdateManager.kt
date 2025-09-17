package org.rainrental.rainrentalrfid.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import org.rainrental.rainrentalrfid.logging.Logger
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    private val updateRepository: UpdateRepository
) : Logger {

    private val updateScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isUpdateInProgress = false
    private var lastUpdateCheck = 0L
    private val updateCheckInterval = 24 * 60 * 60 * 1000 // 24 hours

    /**
     * Check for updates and handle the update process
     * @param context Application context
     * @param companyId Company ID for the API call
     * @param forceCheck Force check even if recently checked
     * @param useTestMode If true, uses mock data instead of real API
     * @param onUpdateAvailable Callback when update is available
     * @param onUpdateProgress Callback for download progress
     * @param onUpdateComplete Callback when update is complete
     * @param onUpdateError Callback when update fails
     */
    fun checkForUpdates(
        context: Context,
        companyId: String,
        forceCheck: Boolean = false,
        useTestMode: Boolean = false,
        onUpdateAvailable: (UpdateInfo) -> Unit = {},
        onUpdateProgress: (Float) -> Unit = {},
        onUpdateComplete: () -> Unit = {},
        onUpdateError: (String) -> Unit = {},
        onNoUpdateAvailable: () -> Unit = {}
    ) {
        logd("=== UPDATE MANAGER: checkForUpdates called ===")
        logd("Company ID: $companyId")
        logd("Force check: $forceCheck")
        logd("Update in progress: $isUpdateInProgress")
        
        if (isUpdateInProgress) {
            logd("UPDATE MANAGER: Update already in progress, skipping")
            return
        }

        val currentTime = System.currentTimeMillis()
        val timeSinceLastCheck = currentTime - lastUpdateCheck
        logd("Time since last check: ${timeSinceLastCheck}ms (interval: ${updateCheckInterval}ms)")
        
        if (!forceCheck && timeSinceLastCheck < updateCheckInterval) {
            logd("UPDATE MANAGER: Update check skipped, too recent")
            return
        }

        lastUpdateCheck = currentTime
        isUpdateInProgress = true
        logd("UPDATE MANAGER: Starting update check process")

        updateScope.launch {
            try {
                logd("UPDATE MANAGER: Starting update check in coroutine")
                
                // Check for available updates
                logd("UPDATE MANAGER: Calling updateRepository.checkForUpdates (testMode: $useTestMode)")
                val updateInfo = updateRepository.checkForUpdates(companyId, useTestMode)
                
                if (updateInfo == null) {
                    logd("UPDATE MANAGER: No update available from repository")
                    onNoUpdateAvailable()
                    isUpdateInProgress = false
                    return@launch
                }

                logd("UPDATE MANAGER: Update info received from repository")
                logd("UPDATE MANAGER: Available version: ${updateInfo.version} (code: ${updateInfo.versionCode}, isCurrent: ${updateInfo.isCurrent})")

                // Check if this version is newer than current
                val currentVersionCode = getCurrentVersionCode(context)
                logd("UPDATE MANAGER: Current app version code: $currentVersionCode")
                logd("UPDATE MANAGER: Available version code: ${updateInfo.versionCode}")
                
                if (updateInfo.versionCode <= currentVersionCode) {
                    logd("UPDATE MANAGER: Update version is not newer than current version")
                    logd("UPDATE MANAGER: Current: $currentVersionCode, Available: ${updateInfo.versionCode}")
                    onNoUpdateAvailable()
                    isUpdateInProgress = false
                    return@launch
                }

                logd("UPDATE MANAGER: Update is newer, proceeding with download")
                logd("UPDATE MANAGER: Update available: ${updateInfo.version}")
                onUpdateAvailable(updateInfo)

                // Download the APK
                logd("UPDATE MANAGER: Starting APK download")
                val apkFile = updateRepository.downloadApk(
                    updateInfo.downloadUrl,
                    updateInfo.fileSize
                ) { progress ->
                    logd("UPDATE MANAGER: Download progress: ${progress.toInt()}%")
                    onUpdateProgress(progress)
                }

                if (apkFile == null) {
                    loge("UPDATE MANAGER: Failed to download APK")
                    onUpdateError("Failed to download update")
                    isUpdateInProgress = false
                    return@launch
                }

                logd("UPDATE MANAGER: APK downloaded successfully: ${apkFile.absolutePath}")

                // Verify the downloaded file
                logd("UPDATE MANAGER: Verifying downloaded APK")
                logd("UPDATE MANAGER: APK file path: ${apkFile.absolutePath}")
                logd("UPDATE MANAGER: APK file exists: ${apkFile.exists()}")
                logd("UPDATE MANAGER: APK file can read: ${apkFile.canRead()}")
                logd("UPDATE MANAGER: APK file can write: ${apkFile.canWrite()}")
                logd("UPDATE MANAGER: APK file length: ${apkFile.length()}")
                logd("UPDATE MANAGER: Expected file size: ${updateInfo.fileSize}")
                logd("UPDATE MANAGER: APK file permissions: ${apkFile.permissions()}")
                
                if (!verifyApkFile(apkFile, updateInfo.fileSize)) {
                    loge("UPDATE MANAGER: APK file verification failed")
                    loge("UPDATE MANAGER: File verification details:")
                    loge("  - File path: ${apkFile.absolutePath}")
                    loge("  - File exists: ${apkFile.exists()}")
                    loge("  - File readable: ${apkFile.canRead()}")
                    loge("  - File size: ${apkFile.length()}")
                    loge("  - Expected size: ${updateInfo.fileSize}")
                    loge("  - Size difference: ${kotlin.math.abs(apkFile.length() - updateInfo.fileSize)}")
                    loge("  - File permissions: ${apkFile.permissions()}")
                    
                    // Try to read first few bytes to see if it's a valid file
                    try {
                        val firstBytes = apkFile.inputStream().use { it.readBytes(1024) }
                        loge("UPDATE MANAGER: First 1024 bytes (hex): ${firstBytes.joinToString(" ") { "%02x".format(it) }}")
                        loge("UPDATE MANAGER: First 1024 bytes (text): ${String(firstBytes, Charsets.UTF_8).replace(Regex("[^\\p{Print}]"), ".")}")
                    } catch (e: Exception) {
                        loge("UPDATE MANAGER: Could not read file bytes: ${e.message}")
                    }
                    
                    apkFile.delete()
                    onUpdateError("Downloaded file is invalid")
                    isUpdateInProgress = false
                    return@launch
                }

                logd("UPDATE MANAGER: APK verification successful")

                // Install the APK
                logd("UPDATE MANAGER: Starting APK installation")
                logd("UPDATE MANAGER: Installation details:")
                logd("  - APK file: ${apkFile.absolutePath}")
                logd("  - File exists: ${apkFile.exists()}")
                logd("  - File size: ${apkFile.length()}")
                logd("  - File readable: ${apkFile.canRead()}")
                logd("  - Context package name: ${context.packageName}")
                logd("  - Android version: ${android.os.Build.VERSION.SDK_INT}")
                
                val installSuccess = installApk(context, apkFile)
                
                if (installSuccess) {
                    logd("UPDATE MANAGER: APK installation initiated successfully")
                    onUpdateComplete()
                    
                    // Clean up old files after successful installation
                    cleanupOldFiles()
                } else {
                    loge("UPDATE MANAGER: Failed to initiate APK installation")
                    loge("UPDATE MANAGER: Installation failure details:")
                    loge("  - APK file: ${apkFile.absolutePath}")
                    loge("  - File exists: ${apkFile.exists()}")
                    loge("  - File size: ${apkFile.length()}")
                    loge("  - File readable: ${apkFile.canRead()}")
                    loge("  - Context package name: ${context.packageName}")
                    loge("  - Android version: ${android.os.Build.VERSION.SDK_INT}")
                    onUpdateError("Failed to install update")
                }

            } catch (e: Exception) {
                loge("=== UPDATE MANAGER ERROR ===")
                loge("Update process failed: ${e.message}")
                loge("Exception type: ${e.javaClass.simpleName}")
                e.printStackTrace()
                onUpdateError("Update failed: ${e.message}")
            } finally {
                logd("UPDATE MANAGER: Update process completed, setting isUpdateInProgress = false")
                isUpdateInProgress = false
            }
        }
    }

    /**
     * Get current app version code
     */
    private fun getCurrentVersionCode(context: Context): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            loge("Error getting current version code: ${e.message}")
            0
        }
    }

    /**
     * Verify downloaded APK file
     */
    private fun verifyApkFile(file: File, expectedSize: Long): Boolean {
        return try {
            val actualSize = file.length()
            val isValidFile = file.exists() && file.canRead()
            
            // Check if file size is reasonable (at least 1MB for an APK)
            val isReasonableSize = actualSize > 1024 * 1024
            
            // If expected size is 0 or very small, just check that file exists and is readable
            // This handles cases where the backend has incorrect file size data
            val isValidSize = if (expectedSize <= 0 || expectedSize < 1024 * 1024) {
                logd("APK verification: Expected size is invalid ($expectedSize), checking file existence only")
                true
            } else {
                // Allow 10% tolerance for file size differences
                val tolerance = expectedSize * 0.1
                val sizeDifference = kotlin.math.abs(actualSize - expectedSize)
                sizeDifference <= tolerance
            }
            
            logd("APK verification: size=$actualSize/$expectedSize, reasonable=$isReasonableSize, valid=$isValidFile, sizeValid=$isValidSize")
            
            isValidFile && isReasonableSize && isValidSize
        } catch (e: Exception) {
            loge("APK verification failed: ${e.message}")
            false
        }
    }

    /**
     * Install APK file
     */
    private fun installApk(context: Context, apkFile: File): Boolean {
        return try {
            logd("INSTALL APK: Starting installation process")
            logd("INSTALL APK: File path: ${apkFile.absolutePath}")
            logd("INSTALL APK: File exists: ${apkFile.exists()}")
            logd("INSTALL APK: File size: ${apkFile.length()}")
            logd("INSTALL APK: Android version: ${Build.VERSION.SDK_INT}")
            logd("INSTALL APK: Package name: ${context.packageName}")
            
            val intent = Intent(Intent.ACTION_VIEW)
            val uri: Uri
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Use FileProvider for Android 7.0+
                val authority = "${context.packageName}.fileprovider"
                logd("INSTALL APK: Using FileProvider with authority: $authority")
                uri = FileProvider.getUriForFile(context, authority, apkFile)
                logd("INSTALL APK: FileProvider URI: $uri")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                logd("INSTALL APK: Added FLAG_GRANT_READ_URI_PERMISSION")
            } else {
                // Direct file URI for older versions
                logd("INSTALL APK: Using direct file URI (Android < 7.0)")
                uri = Uri.fromFile(apkFile)
                logd("INSTALL APK: Direct file URI: $uri")
            }
            
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            logd("INSTALL APK: Intent data: ${intent.data}")
            logd("INSTALL APK: Intent type: ${intent.type}")
            logd("INSTALL APK: Intent flags: ${intent.flags}")
            
            // Check if there's an app that can handle this intent
            val packageManager = context.packageManager
            val resolveInfo = packageManager.queryIntentActivities(intent, 0)
            logd("INSTALL APK: Found ${resolveInfo.size} activities that can handle the intent")
            
            if (resolveInfo.isEmpty()) {
                loge("INSTALL APK: No activities found to handle APK installation intent")
                loge("INSTALL APK: This might indicate missing package installer or security restrictions")
                return false
            }
            
            context.startActivity(intent)
            logd("INSTALL APK: Installation intent sent successfully")
            true
            
        } catch (e: Exception) {
            loge("INSTALL APK: Failed to install APK: ${e.message}")
            loge("INSTALL APK: Exception type: ${e.javaClass.simpleName}")
            loge("INSTALL APK: Stack trace:")
            e.printStackTrace()
            false
        }
    }

    /**
     * Clean up old update files
     */
    private fun cleanupOldFiles() {
        try {
            val cacheDir = File(System.getProperty("java.io.tmpdir") ?: "/tmp")
            val updateFiles = cacheDir.listFiles { file ->
                file.name.startsWith("app_update_") && file.name.endsWith(".apk")
            } ?: emptyArray()

            for (file in updateFiles) {
                try {
                    if (file.delete()) {
                        logd("Cleaned up old update file: ${file.name}")
                    }
                } catch (e: Exception) {
                    loge("Failed to delete old update file ${file.name}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            loge("Error during cleanup: ${e.message}")
        }
    }

    /**
     * Force check for updates (ignores time interval)
     */
    fun forceCheckForUpdates(
        context: Context,
        companyId: String,
        onUpdateAvailable: (UpdateInfo) -> Unit = {},
        onUpdateProgress: (Float) -> Unit = {},
        onUpdateComplete: () -> Unit = {},
        onUpdateError: (String) -> Unit = {}
    ) {
        checkForUpdates(
            context = context,
            companyId = companyId,
            forceCheck = true,
            onUpdateAvailable = onUpdateAvailable,
            onUpdateProgress = onUpdateProgress,
            onUpdateComplete = onUpdateComplete,
            onUpdateError = onUpdateError
        )
    }

    /**
     * Check if update is currently in progress
     */
    fun isUpdateInProgress(): Boolean = isUpdateInProgress

    /**
     * Get time since last update check
     */
    fun getTimeSinceLastCheck(): Long = System.currentTimeMillis() - lastUpdateCheck
}
