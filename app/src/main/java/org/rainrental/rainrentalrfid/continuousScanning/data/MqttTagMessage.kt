package org.rainrental.rainrentalrfid.continuousScanning.data

import org.rainrental.rainrentalrfid.logging.LogUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MqttTagMessage(
    val timestamp: String,
    val hostname: String,
    val eventType: String,
    val tagInventoryEvent: TagInventoryEvent,
)


@Serializable
class TagInventoryEvent(
    val tid: String,
    val tidHex: String,
    val epc: String,
    val antennaPort: Int,
    val antennaName: String,
    val peakRssiCdbm: Double,
    val frequency: Int,
    val transmitPowerCdbm: Int,
)

fun convertToJsonString(data: MqttTagMessage): String {
    return try {
        Json.encodeToString(data)
    } catch (e: SerializationException) {
        LogUtils.loge("Serialization", "Failed to serialize MqttTagMessage: ${e.message}")
        throw e
    }
} 