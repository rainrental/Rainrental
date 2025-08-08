package org.rainrental.rainrentalrfid.inputmanager.domain.use_case

import org.rainrental.rainrentalrfid.inputmanager.data.manager.HardwareInputMode
import org.rainrental.rainrentalrfid.inputmanager.data.manager.InputManager
import org.rainrental.rainrentalrfid.result.InputError
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

class ScanInvitationCodeUseCase @Inject constructor(
    private val inputManager: InputManager
){
    suspend operator fun invoke() : Result<String, InputError>{
        inputManager.setInputMode(HardwareInputMode.BARCODE)
        return when (val invitationCode = inputManager.getInput()){
            is Result.Error -> Result.Error(invitationCode.error)
            is Result.Success -> {
                // Return the raw invitation code without any processing
                Result.Success(invitationCode.data)
            }
        }
    }
} 