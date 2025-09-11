package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.update.UpdateManager
import org.rainrental.rainrentalrfid.update.UpdateRepository
import org.rainrental.rainrentalrfid.commission.data.BackendApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {

    @Provides
    @Singleton
    fun provideUpdateRepository(
        backendApi: BackendApi
    ): UpdateRepository {
        return UpdateRepository(backendApi)
    }



    @Provides
    @Singleton
    fun provideUpdateManager(
        updateRepository: UpdateRepository
    ): UpdateManager {
        return UpdateManager(updateRepository)
    }
}
