package org.rainrental.rainrentalrfid.continuousScanning

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.continuousScanning.data.DeliveryConnectionState
import org.rainrental.rainrentalrfid.app.AppConfig


interface MqttDeliveryService : DefaultLifecycleObserver, Logger {
    fun initialiseClient(withServerUris: List<String>, lifecycleOwner: LifecycleOwner)
    val failedConnections: StateFlow<Int>
    val currentServer: StateFlow<String?>
    suspend fun publish(msg: String, topic: String): Boolean
    suspend fun checkConnection()
    fun isConnected(): Boolean
    val state: StateFlow<DeliveryConnectionState>
    fun restartWithNewServer(server: String)
    fun cleanup()
    suspend fun reDetectAndConnect(context: Context, appConfig: AppConfig)
} 