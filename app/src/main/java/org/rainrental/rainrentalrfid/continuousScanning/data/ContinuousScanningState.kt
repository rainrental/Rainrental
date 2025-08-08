package org.rainrental.rainrentalrfid.continuousScanning.data

data class ContinuousScanningState(
    val isRunning: Boolean = false,
    val isTest: Boolean = false,
    val lastRssi: Double = -100.0,
    val lastTagEvent: TagEvent? = null,
    val tagCount: Int = 0,
    val uniqueCount: Int = 0,
    val scanningRoundId: Long = 0L,
    val tagId: String? = null,
    val tagIds: List<String> = listOf(),
    val cacheSize: Int = 0,
) 