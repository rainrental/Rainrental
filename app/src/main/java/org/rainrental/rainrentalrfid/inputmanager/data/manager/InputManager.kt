package org.rainrental.rainrentalrfid.inputmanager.data.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.rainrental.rainrentalrfid.chainway.data.BarcodeHardwareState
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.chainway.data.ScannerManager
import org.rainrental.rainrentalrfid.inputmanager.data.model.InputFlowState
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.result.InputError
import javax.inject.Inject
import org.rainrental.rainrentalrfid.result.Result

interface InputManager {
    val inputMode: StateFlow<HardwareInputMode>
    var inputFlowState: StateFlow<InputFlowState>
    val barcodeHardwareState: StateFlow<BarcodeHardwareState>
    val barcodeEntity: StateFlow<String?>
    fun setInputMode(mode: HardwareInputMode)
    fun toggleInputMode()
    suspend fun getInput(withRfidPower:Int? = null): Result<String, InputError>


}

class NewInputManager @Inject constructor(
    private val rfidManager: RfidManager,
    private val scannerManager: ScannerManager,
) : InputManager, Logger {
    private val _inputMode = MutableStateFlow(HardwareInputMode.BARCODE)
    override val inputMode: StateFlow<HardwareInputMode> = _inputMode.asStateFlow()

    private var _inputFlowState = MutableStateFlow<InputFlowState>(InputFlowState.Waiting)
    override var inputFlowState: StateFlow<InputFlowState> = _inputFlowState.asStateFlow()

    private val _barcodeHardwareState : MutableStateFlow<BarcodeHardwareState> = MutableStateFlow(BarcodeHardwareState.Startup)
    override val barcodeHardwareState: StateFlow<BarcodeHardwareState> = _barcodeHardwareState.asStateFlow()

    private val _barcodeEntity : MutableStateFlow<String?> = MutableStateFlow(null)
    override val barcodeEntity: StateFlow<String?> = _barcodeEntity.asStateFlow()

    override fun setInputMode(mode: HardwareInputMode) {
        _inputMode.update { mode }
    }

    override fun toggleInputMode() {
        when (_inputMode.value){
            HardwareInputMode.BARCODE -> _inputMode.update { HardwareInputMode.RFID }
            HardwareInputMode.RFID -> _inputMode.update { HardwareInputMode.BARCODE }
        }
    }

    override suspend fun getInput(withRfidPower: Int?): Result<String, InputError> {
        return when (_inputMode.value){
            HardwareInputMode.BARCODE -> {
                return when (val barcode = scannerManager.getBarcode()){
                    is Result.Error -> Result.Error(barcode.error)
                    is Result.Success -> Result.Success(barcode.data)
                }
            }
            HardwareInputMode.RFID -> {
                Result.Error(InputError.HardwareError)
            }
        }
    }


}

enum class HardwareInputMode{
    BARCODE,
    RFID
}