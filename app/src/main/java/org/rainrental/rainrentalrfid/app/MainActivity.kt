package org.rainrental.rainrentalrfid.app

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.Surface
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import org.rainrental.rainrentalrfid.chainway.data.OrientationManager
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.chainway.data.ScannerManager
import org.rainrental.rainrentalrfid.inputmanager.data.manager.InputManager
import org.rainrental.rainrentalrfid.hardware.HardwareEventBus
import org.rainrental.rainrentalrfid.continuousScanning.MqttDeliveryService
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme
import org.rainrental.rainrentalrfid.chainway.data.ChainwayRfidManager
import org.rainrental.rainrentalrfid.audio.impl.AndroidAudioService
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.app.AppConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch
var deviceSerial = ""
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    @Singleton
    lateinit var scannerManager: ScannerManager

    @Inject
    @Singleton
    lateinit var rfidManager: RfidManager

    @Inject
    @Singleton
    lateinit var inputManager: InputManager

    @Inject
    lateinit var mqttService: MqttDeliveryService

    @Inject
    lateinit var dependencies: BaseViewModelDependencies

    @Inject
    lateinit var appConfig: AppConfig

    @Inject
    @Singleton
    lateinit var hardwareEventBus: HardwareEventBus

    @Inject
    @Singleton
    lateinit var scanningLifecycleManager: ScanningLifecycleManager

    private val orientationManager = OrientationManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceSerial = try {
            Settings.Global.getString(this.contentResolver, "Serial")
        } catch (error: Exception) {
            ""
        }
        orientationManager.setApplication(application)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        getPermissions()
        enableEdgeToEdge()
//        supportActionBar?.hide()
        scannerManager.initiliseScanner(this.lifecycle)
        lifecycle.addObserver(scannerManager)
        lifecycle.addObserver(rfidManager)

        // Use configured MQTT server IP
        val mqttServerIp = appConfig.getMqttServerIp(this@MainActivity)
        Log.i("MainActivity", "Using MQTT server: $mqttServerIp")
        mqttService.initialiseClient(listOf(mqttServerIp), this@MainActivity)
        lifecycle.addObserver(mqttService)

        setContent {
            RainRentalRfidTheme{
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Surface {
                        MainApp()
                    }
                }
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
//        Log.d("MainActivity", "onKeyUp called with keyCode: $keyCode")
        hardwareEventBus.onKeyUp(keyCode)
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        Log.d("MainActivity", "onKeyDown called with keyCode: $keyCode")
        
        // Check if this is a repeated key event
        val isRepeated = (event?.repeatCount ?: 0) > 0
        if (isRepeated) {
            Log.d("MainActivity", "Ignoring repeated key event for keyCode: $keyCode with repeatCount: ${event?.repeatCount}")
            return true // Consume the event, don't pass to super
        }
        
        hardwareEventBus.onKeyDown(keyCode)
        return super.onKeyDown(keyCode, event)
    }

    @Deprecated("Deprecated in favor of the new back handling system")
    override fun onBackPressed() {
        Log.i("MainActivity", "Back button pressed - cancelling all scanning")
        // Cancel all scanning operations when back is pressed
        scanningLifecycleManager.cancelAllScanning()
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Log.i("MainActivity","On Pause - cancelling all scanning")
        // Cancel all scanning operations when app is paused
        scanningLifecycleManager.cancelAllScanning()
    }

    override fun onResume() {
        super.onResume()
        Log.i("MainActivity", "On Resume")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.i("MainActivity", "On Destroy - Cleaning up resources")
        
        // Cleanup resources
        try {
            // Cleanup RFID manager
            (rfidManager as? ChainwayRfidManager)?.cleanup()
            
            // Cleanup audio service
            (dependencies.audioService as? AndroidAudioService)?.cleanupAudioService()
            
            // Cleanup MQTT service
            mqttService.cleanup()
            
            Log.i("MainActivity", "Resource cleanup completed")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error during cleanup: $e")
        }
    }
}

