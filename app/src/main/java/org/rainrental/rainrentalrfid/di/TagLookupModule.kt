package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupRepository
import org.rainrental.rainrentalrfid.taglookup.data.DefaultTagLookupRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TagLookupModule {

    @Provides
    @Singleton
    fun provideTagLookupRepository(
        defaultTagLookupRepository: DefaultTagLookupRepository
    ): TagLookupRepository {
        return defaultTagLookupRepository
    }
}
