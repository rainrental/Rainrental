package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.chainway.data.BarcodeManager
import org.rainrental.rainrentalrfid.chainway.data.ChainwayRfidManager
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @Provides
//    @Singleton
//    fun provideRfidManager(): RfidManager = ChainwayRfidManager

    @Provides
    @Singleton
    fun provideBarcodeManager(): BarcodeManager = BarcodeManager


}