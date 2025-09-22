package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.mqtt.MqttConnectionWatchdog
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MqttWatchdogModule {
    
    @Provides
    @Singleton
    fun provideMqttConnectionWatchdog(
        mqttDeliveryService: org.rainrental.rainrentalrfid.continuousScanning.MqttDeliveryService,
        appConfig: org.rainrental.rainrentalrfid.app.AppConfig
    ): MqttConnectionWatchdog {
        return MqttConnectionWatchdog(mqttDeliveryService, appConfig)
    }
}
