package org.rainrental.rainrentalrfid.inputmanager.domain.model

import org.rainrental.rainrentalrfid.inputmanager.data.manager.HardwareInputMode
import org.rainrental.rainrentalrfid.result.InputError

data class InputPanelState(
    val showInputPanel:Boolean = false,
    val lastResult: String? = null,
    val lastResultType: HardwareInputMode = HardwareInputMode.BARCODE,
    val lastSuccess: Boolean? = null,
    val lastHash:Int = 0,
    val lastError: InputError? = null,
    val message:String? = null,
    val lastTime: String = "",
    val currentInputMode: HardwareInputMode = HardwareInputMode.BARCODE,
    val isScanning: Boolean = false
)