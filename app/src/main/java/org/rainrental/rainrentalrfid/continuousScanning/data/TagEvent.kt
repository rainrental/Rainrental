package org.rainrental.rainrentalrfid.continuousScanning.data

data class TagEvent(
    val tid: String,
    val epc: String,
    val rssi: Double,
    var seen: Int,
    val roundId: Long,
    val frequency: Float,
    val power: Int = 30,
) 