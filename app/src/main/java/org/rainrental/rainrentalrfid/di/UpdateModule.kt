package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.update.UpdateManager
import org.rainrental.rainrentalrfid.update.UpdateRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {

    @Provides
    @Singleton
    fun provideUpdateRepository(
        appConfig: org.rainrental.rainrentalrfid.app.AppConfig
    ): UpdateRepository {
        return UpdateRepository(appConfig)
    }



    @Provides
    @Singleton
    fun provideUpdateManager(
        updateRepository: UpdateRepository
    ): UpdateManager {
        return UpdateManager(updateRepository)
    }
}
