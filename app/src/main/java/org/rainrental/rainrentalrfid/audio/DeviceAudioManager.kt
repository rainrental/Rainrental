package org.rainrental.rainrentalrfid.audio

import org.rainrental.rainrentalrfid.logging.Logger


interface DeviceAudioManager : Logger {
    suspend fun volumeUp(): Result<Int>
    suspend fun volumeDown(): Result<Int>
    suspend fun volumeMax() : Result<Int>
    suspend fun volumeMin() : Result<Int>
    suspend fun getVolume() : Result<Int>
    suspend fun volume(volume: Int): Result<Int>
}