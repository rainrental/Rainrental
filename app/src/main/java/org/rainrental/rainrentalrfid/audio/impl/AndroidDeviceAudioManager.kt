package org.rainrental.rainrentalrfid.audio.impl

import android.media.AudioManager
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.rainrental.rainrentalrfid.audio.DeviceAudioManager
import javax.inject.Inject

@ActivityScoped
class AndroidDeviceAudioManager @Inject constructor(
    private val audioManager: AudioManager
): DeviceAudioManager {
    private val audioStreamType = AudioManager.STREAM_MUSIC
    private val ringerVolume = AudioManager.STREAM_VOICE_CALL
    private val notification = AudioManager.STREAM_NOTIFICATION
    private val alarmVolume = AudioManager.STREAM_ALARM

    override suspend fun volumeMax() : Result<Int> {
        val newVolume = CoroutineScope(Dispatchers.IO + SupervisorJob()).async {
            val maxVolume = audioManager.getStreamMaxVolume(audioStreamType)
            audioManager.setStreamVolume(
                audioStreamType,
                maxVolume,
                0
            )
            val newVolume = audioManager.getStreamVolume(audioStreamType)
            logd(
                "new Volume: $newVolume, max Volume: $maxVolume"
            )
            newVolume
        }
        return Result.success(newVolume.await())
    }

    override suspend fun volumeMin() : Result<Int>{
        val newVolume = CoroutineScope(Dispatchers.IO + SupervisorJob()).async {
            val minVolume = audioManager.getStreamMinVolume(audioStreamType)
            audioManager.setStreamVolume(
                audioStreamType,
                minVolume,
                0
            )
            val newVolume = audioManager.getStreamVolume(audioStreamType)
            logd("new Volume: $newVolume, min Volume: $minVolume")
            newVolume
        }
        return Result.success(newVolume.await())
    }

    override suspend fun getVolume(): Result<Int> {
        val currentV = CoroutineScope(Dispatchers.IO + SupervisorJob()).async {
            val currentVolume = audioManager.getStreamVolume(audioStreamType)
            logd("Current Volume: $currentVolume")
            currentVolume
        }
        return Result.success(currentV.await())
    }

    override suspend fun volume(volume: Int): Result<Int> {
        val newVolume = CoroutineScope(Dispatchers.IO + SupervisorJob()).async {
            audioManager.setStreamVolume(
                audioStreamType,
                volume,
                0
            )
            audioManager.setStreamVolume(
                ringerVolume,
                (volume*7)/15,
                0
            )
            audioManager.setStreamVolume(
                notification,
                volume,
                0
            )
            audioManager.setStreamVolume(
                alarmVolume,
                volume,
                0
            )
            val newVolume = audioManager.getStreamVolume(audioStreamType)
            logd("Set new Volume: $newVolume")
            newVolume
        }
        return Result.success(newVolume.await())
    }

    override suspend fun volumeDown(): Result<Int> {
        val newVolume = CoroutineScope(Dispatchers.IO + SupervisorJob()).async {
            val minVolume = audioManager.getStreamMinVolume(audioStreamType)
            val currentVolume = audioManager.getStreamVolume(audioStreamType)
            val intendedVolume = currentVolume - 1
            if (intendedVolume >= minVolume) audioManager.setStreamVolume(
                audioStreamType,
                intendedVolume,
                0
            )
            val newVolume = audioManager.getStreamVolume(audioStreamType)
            logd("previous Volume: $currentVolume, intended Volume: $intendedVolume, new Volume: $newVolume, min Volume: $minVolume")
            newVolume
        }
        return Result.success(newVolume.await())
    }

    override suspend fun volumeUp(): Result<Int> {
        val newVolume = CoroutineScope(Dispatchers.IO + SupervisorJob()).async {
            val maxVolume = audioManager.getStreamMaxVolume(audioStreamType)
            val currentVolume = audioManager.getStreamVolume(audioStreamType)
            val intendedVolume = currentVolume + 1
            if (intendedVolume <= maxVolume) audioManager.setStreamVolume(
                audioStreamType,
                intendedVolume,
                0
            )
            val newVolume = audioManager.getStreamVolume(audioStreamType)
            logd("previous Volume: $currentVolume, intended Volume: $intendedVolume, new Volume: $newVolume, max Volume: $maxVolume")
            newVolume
        }
        return Result.success(newVolume.await())
    }
}



