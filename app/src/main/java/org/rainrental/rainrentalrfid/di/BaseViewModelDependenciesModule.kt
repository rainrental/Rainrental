package org.rainrental.rainrentalrfid.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.app.AppConfig
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.app.ScanningLifecycleManager
import org.rainrental.rainrentalrfid.audio.AudioService
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.chainway.data.ScannerManager
import org.rainrental.rainrentalrfid.hardware.HardwareEventBus
import org.rainrental.rainrentalrfid.toast.data.repository.ToastRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BaseViewModelDependenciesModule {

    @Provides
    @Singleton
    fun providesBaseViewModelDependencies(
        rfidManager: RfidManager,
        scannerManager: ScannerManager,
        hardwareEventBus: HardwareEventBus,
        audioService: AudioService,
        appConfig: AppConfig,
        @ApplicationContext context: Context,
        scanningLifecycleManager: ScanningLifecycleManager,
        toastRepository: ToastRepository,
    ) : BaseViewModelDependencies{
        return BaseViewModelDependencies(
            rfidManager = rfidManager,
            scannerManager = scannerManager,
            audioService = audioService,
            hardwareEventBus = hardwareEventBus,
            toastRepository = toastRepository,
            appConfig = appConfig,
            context = context,
            scanningLifecycleManager = scanningLifecycleManager
        )
    }
}