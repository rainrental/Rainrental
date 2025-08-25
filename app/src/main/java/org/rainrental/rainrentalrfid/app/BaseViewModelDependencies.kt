package org.rainrental.rainrentalrfid.app

import android.content.Context
import org.rainrental.rainrentalrfid.audio.AudioService
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.chainway.data.ScannerManager
import org.rainrental.rainrentalrfid.hardware.HardwareEventBus
import org.rainrental.rainrentalrfid.toast.data.repository.ToastRepository

data class BaseViewModelDependencies(
    val rfidManager: RfidManager,
    val scannerManager: ScannerManager,
    val audioService: AudioService,
    val hardwareEventBus: HardwareEventBus,
    val toastRepository: ToastRepository,
    val appConfig: AppConfig,
    val context: Context,
    val scanningLifecycleManager: ScanningLifecycleManager
)
