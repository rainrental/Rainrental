package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.toast.data.repository.DefaultToastRepository
import org.rainrental.rainrentalrfid.toast.data.repository.ToastRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ToastRepositoryModule {
    @Singleton
    @Provides
    fun providesToastRepository() : ToastRepository{
        return DefaultToastRepository()
    }
}