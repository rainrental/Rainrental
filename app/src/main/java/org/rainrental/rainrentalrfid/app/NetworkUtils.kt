package org.rainrental.rainrentalrfid.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.net.NetworkInterface
import java.util.*

object NetworkUtils {
    
    private const val MQTT_SERVER_SUFFIX = "250"
    private const val FALLBACK_MQTT_SERVER = "192.168.5.82"
    private const val MQTT_PORT = 1883
    private const val CONNECTION_TIMEOUT_MS = 2000L
    private const val TAG = "NetworkUtils"
    
    /**
     * Gets the current MQTT server IP by detecting the network and using .250 suffix
     * Supports both 192.168.x.x and 10.x.x.x private network ranges
     * Falls back to hardcoded default if detection fails
     */
    fun getMqttServerIp(context: Context): String {
        return try {
            val networkPrefix = getCurrentNetworkPrefix(context)
            if (networkPrefix.isNotEmpty()) {
                val detectedIp = "$networkPrefix.$MQTT_SERVER_SUFFIX"
                Log.i(TAG, "Auto-detected MQTT server: $detectedIp")
                detectedIp
            } else {
                Log.w(TAG, "Network detection failed, using fallback: $FALLBACK_MQTT_SERVER")
                FALLBACK_MQTT_SERVER
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during network detection: ${e.message}, using fallback: $FALLBACK_MQTT_SERVER")
            FALLBACK_MQTT_SERVER
        }
    }
    
    /**
     * Tests if a MQTT server is reachable by attempting a TCP connection
     */
    private suspend fun testMqttServerConnectivity(serverIp: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(serverIp, MQTT_PORT), CONNECTION_TIMEOUT_MS.toInt())
                socket.close()
                Log.i(TAG, "MQTT server $serverIp is reachable")
                true
            } catch (e: Exception) {
                Log.w(TAG, "MQTT server $serverIp is not reachable: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Gets the best available MQTT server by testing connectivity
     */
    suspend fun getBestMqttServer(context: Context): String {
        val networkPrefix = getCurrentNetworkPrefix(context)
        if (networkPrefix.isNotEmpty()) {
            val detectedIp = "$networkPrefix.$MQTT_SERVER_SUFFIX"
            Log.i(TAG, "Testing auto-detected MQTT server: $detectedIp")
            
            if (testMqttServerConnectivity(detectedIp)) {
                Log.i(TAG, "Using auto-detected MQTT server: $detectedIp")
                return detectedIp
            } else {
                Log.w(TAG, "Auto-detected server not reachable, trying fallback")
            }
        }
        
        Log.i(TAG, "Using fallback MQTT server: $FALLBACK_MQTT_SERVER")
        return FALLBACK_MQTT_SERVER
    }
    
    /**
     * Gets the current network prefix (e.g., "192.168.50" or "10.0.1") from the device's IP
     * Supports both 192.168.x.x and 10.x.x.x private network ranges
     */
    private fun getCurrentNetworkPrefix(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Get active network
        val activeNetwork = connectivityManager.activeNetwork ?: return ""
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return ""
        
        // Check if we have internet connectivity
        if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            Log.d(TAG, "No internet connectivity detected")
            return ""
        }
        
        // Get all network interfaces
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()
            
            // Skip loopback and down interfaces
            if (networkInterface.isLoopback || !networkInterface.isUp) {
                continue
            }
            
            val inetAddresses = networkInterface.inetAddresses
            while (inetAddresses.hasMoreElements()) {
                val inetAddress = inetAddresses.nextElement()
                
                // Look for IPv4 addresses in the expected private network ranges
                if (!inetAddress.isLoopbackAddress && 
                    (inetAddress.hostAddress?.startsWith("192.168.") == true || 
                     inetAddress.hostAddress?.startsWith("10.") == true)) {
                    
                    val ipParts = inetAddress.hostAddress?.split(".")
                    if (ipParts?.size == 4) {
                        val networkPrefix = "${ipParts[0]}.${ipParts[1]}.${ipParts[2]}"
                        Log.d(TAG, "Found network interface: ${inetAddress.hostAddress}, network prefix: $networkPrefix")
                        // Return the first 3 octets (network prefix)
                        return networkPrefix
                    }
                }
            }
        }
        
        Log.d(TAG, "No suitable network interface found")
        return ""
    }
} 