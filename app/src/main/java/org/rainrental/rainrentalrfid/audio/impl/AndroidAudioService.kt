package org.rainrental.rainrentalrfid.audio.impl

import android.content.Context
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.audio.AudioService
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.app.AppConfig
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AndroidAudioService @Inject constructor(
    @ActivityContext private val context: Context,
    @Named("AudioCoroutine") private val audioCoroutine: CoroutineScope,
    private val audioSoundPool: SoundPool,
    private val appConfig: AppConfig
) : AudioService, Logger {

    private var successSoundId: Int? = null
    private var warningSoundId: Int? = null
    private var errorSoundId: Int? = null
    
    // Add cleanup flag
    private var isCleanedUp = false



    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        if (isCleanedUp) return
        
        audioCoroutine.launch {
            try {
                successSoundId = audioSoundPool.load(context, R.raw.success_sound, 1)
                warningSoundId = audioSoundPool.load(context, R.raw.error_sound, 1)
                errorSoundId = audioSoundPool.load(context, R.raw.error_sound, 1)
                logd("Audio sounds loaded successfully")
            } catch (e: Exception) {
                loge("Error loading audio sounds: $e")
            }
        }
    }

    private fun playSoundWithPoolId(soundPoolId: Int?) {
        if (isCleanedUp || soundPoolId == null) {
            logd("playSoundWithPoolId: isCleanedUp=$isCleanedUp, soundPoolId=$soundPoolId")
            return
        }
        
        soundPoolId.let {
            logd("Playing sound with ID: $it")
            audioCoroutine.launch(Dispatchers.IO) {
                try {
                    audioSoundPool.play(
                        it, 
                        appConfig.Audio.SOUND_VOLUME, 
                        appConfig.Audio.SOUND_VOLUME, 
                        appConfig.Audio.SOUND_PRIORITY, 
                        appConfig.Audio.SOUND_LOOP, 
                        appConfig.Audio.SOUND_RATE
                    )
                    logd("Sound played successfully")
                } catch (e: Exception) {
                    loge("Error playing sound: $e")
                }
            }
        }
    }

    override fun playSuccess() {
        logd("playSuccess() called - successSoundId: $successSoundId")
        playSoundWithPoolId(successSoundId)
    }

    override fun playWarning() {
        playSoundWithPoolId(warningSoundId)
    }

    override fun playError() {
        playSoundWithPoolId(errorSoundId)
    }

    override fun cleanupAudioService() {
        if (isCleanedUp) return
        isCleanedUp = true
        
        try {
            // Unload sounds before releasing SoundPool
            successSoundId?.let { audioSoundPool.unload(it) }
            warningSoundId?.let { audioSoundPool.unload(it) }
            errorSoundId?.let { audioSoundPool.unload(it) }
            
            // Clear references
            successSoundId = null
            warningSoundId = null
            errorSoundId = null
            
            // Release SoundPool
            audioSoundPool.release()
            
            logd("Audio service cleaned up successfully")
        } catch (e: Exception) {
            loge("Error during audio service cleanup: $e")
        }
    }
}