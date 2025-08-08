package org.rainrental.rainrentalrfid.app

import org.rainrental.rainrentalrfid.audio.AudioService
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.chainway.data.ScannerManager
import org.rainrental.rainrentalrfid.hardware.HardwareEventBus
import org.rainrental.rainrentalrfid.shared.domain.use_case.TriggerToastUseCase

data class BaseViewModelDependencies(
    val rfidManager: RfidManager,
    val scannerManager: ScannerManager,
    val hardwareEventBus: HardwareEventBus,
    val triggerToastUseCase: TriggerToastUseCase,
    val audioService: AudioService,
)
