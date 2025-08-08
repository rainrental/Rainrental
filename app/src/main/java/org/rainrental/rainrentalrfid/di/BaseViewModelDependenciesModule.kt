package org.rainrental.rainrentalrfid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.audio.AudioService
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.chainway.data.ScannerManager
import org.rainrental.rainrentalrfid.hardware.HardwareEventBus
import org.rainrental.rainrentalrfid.shared.domain.use_case.TriggerToastUseCase
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
        triggerToastUseCase: TriggerToastUseCase,
        audioService: AudioService,
    ) : BaseViewModelDependencies{
        return BaseViewModelDependencies(
            rfidManager = rfidManager,
            scannerManager = scannerManager,
            hardwareEventBus = hardwareEventBus,
            triggerToastUseCase = triggerToastUseCase,
            audioService = audioService,
        )
    }
}