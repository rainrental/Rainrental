package org.rainrental.rainrentalrfid.app

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch

@Singleton
class ScanningLifecycleManager @Inject constructor(
    private val rfidManager: RfidManager
) : DefaultLifecycleObserver, Logger {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        // Observe app lifecycle to cancel scanning when app is backgrounded
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Cancels all active RFID scanning operations
     */
    fun cancelAllScanning() {
        logd("ScanningLifecycleManager: Cancelling all scanning operations")
        _isScanning.value = false
        
        try {
            // Stop continuous scanning
            rfidManager.stopContinuousScanning()
            
            // Stop tag hunt and inventory scan in coroutine scope
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    rfidManager.stopTagHunt()
                    rfidManager.stopInventoryScan()
                } catch (e: Exception) {
                    loge("ScanningLifecycleManager: Error stopping hunt/inventory: $e")
                }
            }
            
            logd("ScanningLifecycleManager: All scanning operations cancelled successfully")
        } catch (e: Exception) {
            loge("ScanningLifecycleManager: Error cancelling scanning operations: $e")
        }
    }

    /**
     * Called when app is backgrounded - cancel all scanning
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        logd("ScanningLifecycleManager: App backgrounded, cancelling scanning")
        cancelAllScanning()
    }

    /**
     * Called when app is foregrounded
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        logd("ScanningLifecycleManager: App foregrounded")
    }

    /**
     * Called when app is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        logd("ScanningLifecycleManager: App destroyed, cancelling scanning")
        cancelAllScanning()
    }

    /**
     * Sets the scanning state
     */
    fun setScanningState(scanning: Boolean) {
        _isScanning.value = scanning
        logd("ScanningLifecycleManager: Scanning state set to: $scanning")
    }
}
