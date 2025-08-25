package org.rainrental.rainrentalrfid.continuousScanning


import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.rainrental.rainrentalrfid.di.HiveMqttClientFactory
import org.rainrental.rainrentalrfid.continuousScanning.data.DeliveryConnectionState
import javax.inject.Inject
import android.content.Context
import org.rainrental.rainrentalrfid.app.AppConfig


class HiveMqttDeliveryService @Inject constructor(
    private val application: Application,
    private val mqttClientFactory: HiveMqttClientFactory,
    ) : MqttDeliveryService {

    private lateinit var lifecycle: Lifecycle
    private lateinit var mqttLifecycleOwner: LifecycleOwner

    private var mqttClient: Mqtt3Client? = null

    private val _failedConnections: MutableStateFlow<Int> = MutableStateFlow(0)
    override val failedConnections: StateFlow<Int> = _failedConnections.asStateFlow()

    private var serverUris: List<String> = listOf()

    private val _currentServer: MutableStateFlow<String?> = MutableStateFlow(null)
    override val currentServer: StateFlow<String?> = _currentServer.asStateFlow()


    private var _state: MutableStateFlow<DeliveryConnectionState> = MutableStateFlow(DeliveryConnectionState.DEAD)
    override val state: StateFlow<DeliveryConnectionState> = _state.asStateFlow()



    override fun initialiseClient(withServerUris: List<String>, lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            ensureActive()
            withContext(Dispatchers.Main) {
                _state.update { DeliveryConnectionState.INIT }
            }
            serverUris = withServerUris
            lifecycle = lifecycleOwner.lifecycle
            mqttLifecycleOwner = lifecycleOwner
            quietLog("Initialising Mqtt Service ${this.hashCode()}")
            initMqtt()
        }
    }

    override fun restartWithNewServer(server: String) {
        if (::lifecycle.isInitialized) {
            _currentServer.update { server }
            lifecycle.coroutineScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                ensureActive()
                serverUris = listOf(server)
                quietLog("restartWithNewServer($server)")
                initMqtt()
            }
        } else {
            quietLog("Mqtt Is Not Initialised ${this.hashCode()}")
        }
    }

    override fun cleanup() {
        try {
            // Disconnect MQTT client
            mqttClient?.let { client ->
                if (client.state.isConnected) {
                    client.toBlocking().disconnect()
                    logd("MQTT client disconnected successfully")
                }
                mqttClient = null
            }
            
            // Cancel lifecycle scope
            if (::lifecycle.isInitialized) {
                lifecycle.coroutineScope.cancel()
                ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
                logd("MQTT lifecycle scope cancelled")
            }
            
            // Reset state
            _state.update { DeliveryConnectionState.DEAD }
            _currentServer.update { null }
            _failedConnections.update { 0 }
            
            logd("MQTT service cleanup completed")
        } catch (e: Exception) {
            loge("Error during MQTT cleanup: $e")
        }
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        loge(
            "💥 Handle $exception in CoroutineExceptionHandler"
        )
    }

    private fun quietLog(msg: String) {
        if (failedConnections.value < 2) {
            logd(msg)
        } else if (failedConnections.value == 2) {
            logd("$msg. Will continue silently")
        }
    }

    private fun initMqtt(useSecondary: Boolean = false, withReason: String? = null) {
        var serverUri = serverUris.firstOrNull() ?: return
        if (useSecondary) {
            serverUri = serverUris.lastOrNull() ?: return
        }
        quietLog("Initialising MQTT connection with server: [$serverUri]")
        _currentServer.update { serverUri }
        quietLog("Initialising MQTT connection with server: [$serverUri] ${withReason?.let { " with reason: $it" }} [${failedConnections.value}]")
        mqttLifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            ensureActive()
            _state.update { DeliveryConnectionState.CONNECTING }
            _failedConnections.update { it + 1 }
            try {
                mqttClient = null
                mqttClient = mqttClientFactory.createMqttClient(serverUri)
                mqttClient?.let {
                    if (it.state.isConnected) it.toBlocking().disconnect()
                    it.toBlocking().connectWith().keepAlive(600).send()
                    logd("Client: ${it.state.name} ${serverUri}")
                    _state.update { DeliveryConnectionState.CONNECTED }
                    _failedConnections.update { 0 }
                } ?: loge(
                    "No client returned"
                )
            } catch (e: Exception) {
                _state.update { DeliveryConnectionState.DEAD }
                quietLog("Unable to create MQTT Client")
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        logd("onStart")
        application.mainExecutor.execute {
            initMqtt(withReason = "Service onStart()")
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        logd("onStop")
        if (mqttClient != null) {
            if (mqttClient?.state?.isConnected == true) {
                mqttClient?.toBlocking()?.disconnect()
                _state.update { DeliveryConnectionState.DEAD }
            }
        }
    }


    override suspend fun publish(msg: String, topic: String): Boolean {
        if (mqttClient == null) {
            logd("Client not initialized")
            initMqtt()
            return false
        } else {
            try {
                if (mqttClient?.state?.isConnected != true) {
                    return false
                }
                val r =
                    mqttClient?.toAsync()?.publishWith()?.topic(topic)?.qos(MqttQos.AT_LEAST_ONCE)
                        ?.payload(msg.toByteArray())?.send()
                r?.await()

                return true
            } catch (e: Exception) {
                loge("Failed")
                return false
            }

        }
    }

    private var isCheckingMutex = Mutex()
    override suspend fun checkConnection() {
        isCheckingMutex.withLock {
            if (mqttClient == null) {
                quietLog("Mqtt is null")
                initMqtt(withReason = "Mqtt is Null")
                return
            } else {
                if (mqttClient!!.state.isConnectedOrReconnect) {
                    return
                } else {
                    quietLog("Check connection: MqttClient Not connected")
                    initMqtt(withReason = "MqttClient Not connected")
                    return
                }
            }
        }
    }

    override fun isConnected(): Boolean {
        return if (mqttClient != null) (mqttClient?.state?.isConnected ?: false) else false
    }
    
    override suspend fun reDetectAndConnect(context: Context, appConfig: AppConfig) {
        logd("Re-detecting MQTT server...")
        cleanup()
        
        val newServerIp = appConfig.getMqttServerIp(context)
        logd("Re-detected MQTT server: $newServerIp")
        
        if (::lifecycle.isInitialized) {
            initialiseClient(listOf(newServerIp), mqttLifecycleOwner)
        }
    }
} 