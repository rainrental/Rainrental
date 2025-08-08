package org.rainrental.rainrentalrfid.di

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.rainrental.rainrentalrfid.audio.AudioService
import org.rainrental.rainrentalrfid.audio.DeviceAudioManager
import org.rainrental.rainrentalrfid.audio.impl.AndroidAudioService
import org.rainrental.rainrentalrfid.audio.impl.AndroidDeviceAudioManager
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    @Provides
    @Singleton
    fun provideAndroidAudioService(@ApplicationContext appContext: Context, @Named("AudioCoroutine") coroutineScope: CoroutineScope, soundPool: SoundPool, appConfig: org.rainrental.rainrentalrfid.app.AppConfig): AudioService = AndroidAudioService(context = appContext, audioCoroutine = coroutineScope, audioSoundPool = soundPool, appConfig = appConfig)

    @Provides
    @Singleton
    fun providesSoundPool() : SoundPool {
        return SoundPool.Builder().setMaxStreams(100).build()
    }


    @Provides
    @Named("AudioCoroutine")
    fun provideAudioCoroutine() : CoroutineScope {
        return CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

}

@Module
@InstallIn(SingletonComponent::class)
object AudioDeviceModule{

    @Provides
    @Singleton
    fun provideAudioManager(@ApplicationContext context: Context): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    @Provides
    @Singleton
    fun provideAndroidDeviceAudioManager(audioManager: AudioManager): DeviceAudioManager = AndroidDeviceAudioManager(audioManager = audioManager)
}