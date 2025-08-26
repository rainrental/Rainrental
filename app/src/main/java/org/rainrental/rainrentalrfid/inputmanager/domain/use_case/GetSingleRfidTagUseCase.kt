package org.rainrental.rainrentalrfid.inputmanager.domain.use_case

import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.commission.data.CommissionRepository
import org.rainrental.rainrentalrfid.inputmanager.data.manager.HardwareInputMode
import org.rainrental.rainrentalrfid.inputmanager.data.manager.InputManager
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.result.InputError
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

class GetSingleRfidTagUseCase @Inject constructor(
    private val rfidManager: RfidManager,
    private val commissionRepository: CommissionRepository,
    private val inputManager: InputManager
) : Logger {
    suspend operator fun invoke(withPower:Int? = null): Result<RfidTagInfo, InputError>{
        val defaultLowPower = 2
        logd("Getting single RFID tag withPower $withPower and defaultPower $defaultLowPower")
        
        // Set input mode to RFID to ensure we're using the RFID scanner, not barcode
        inputManager.setInputMode(HardwareInputMode.RFID)
        
        try{

        return when (val response = rfidManager.readSingleTag(withPower?:defaultLowPower)){
            is Result.Error -> {
                Result.Error(response.error)
            }
            is Result.Success -> {
                val isTagAvailable = commissionRepository.isTagAvailable(tidHex = response.data.tid)
                return when (isTagAvailable){
                    is Result.Error -> {
                        Result.Error(InputError.TagAlreadyInUse)
                    }

                    is Result.Success -> {
                        when (isTagAvailable.data){
                            true -> Result.Success(response.data)
                            false -> Result.Error(InputError.TagAlreadyInUse)
                        }
                    }
                }

            }


        }
        }catch (e:Exception){
            loge("Error getting single RFID tag: Bastard fucker")
            return Result.Error(InputError.HardwareError)
        }
    }
}

data class RfidTagInfo(
    val tid:String,
    val epc:String
)