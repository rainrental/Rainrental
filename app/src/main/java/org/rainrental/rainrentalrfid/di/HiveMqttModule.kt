package org.rainrental.rainrentalrfid.di

import android.app.Application
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.continuousScanning.HiveMqttDeliveryService
import org.rainrental.rainrentalrfid.continuousScanning.MqttDeliveryService
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiveMqttModule {
    @Singleton
    @Provides
    fun providesHiveMqttDeliveryService(
        application: Application,
        mqttClientFactory: HiveMqttClientFactory,
    ): MqttDeliveryService {
        return HiveMqttDeliveryService(application, mqttClientFactory)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AndroidMqttClientModule {

    @Provides
    @Singleton
    fun providesHiveMqttClientFactory(): HiveMqttClientFactory {
        return DefaultHiveMqttClientFactory()
    }

}


interface HiveMqttClientFactory {
    fun createMqttClient(url: String): Mqtt3Client
}

class DefaultHiveMqttClientFactory @Inject constructor(

) : HiveMqttClientFactory {
    override fun createMqttClient(url: String): Mqtt3Client {
        return Mqtt3Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost(url)
            .buildBlocking()
    }

}
