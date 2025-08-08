package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.chainway.data.ScannerManager
import org.rainrental.rainrentalrfid.inputmanager.data.manager.InputManager
import org.rainrental.rainrentalrfid.inputmanager.data.manager.NewInputManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InputManagerModule {

    @Singleton
    @Provides
    fun providesInputManager(rfidManager: RfidManager,scannerManager: ScannerManager) : InputManager{
        return NewInputManager(rfidManager,scannerManager)
    }

}