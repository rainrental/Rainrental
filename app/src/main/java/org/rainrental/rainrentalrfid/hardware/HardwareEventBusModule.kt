package org.rainrental.rainrentalrfid.hardware

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.app.AppConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HardwareEventBusModule {
    
    @Provides
    @Singleton
    fun provideHardwareEventBus(appConfig: AppConfig): HardwareEventBus {
        return HardwareEventBus(appConfig)
    }
} 