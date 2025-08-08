package org.rainrental.rainrentalrfid.inputmanager.domain.use_case

import org.rainrental.rainrentalrfid.inputmanager.data.manager.HardwareInputMode
import org.rainrental.rainrentalrfid.inputmanager.data.manager.InputManager
import org.rainrental.rainrentalrfid.result.InputError
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

class ScanBarcodeUseCase @Inject constructor(
    private val inputManager: InputManager
){
    suspend operator fun invoke() : Result<String, InputError>{
        inputManager.setInputMode(HardwareInputMode.BARCODE)
        return when (val barcode = inputManager.getInput()){
            is Result.Error -> Result.Error(barcode.error)
            is Result.Success -> {
                Result.Success(barcode.data.replace('/','-'))
            }
        }

    }
}