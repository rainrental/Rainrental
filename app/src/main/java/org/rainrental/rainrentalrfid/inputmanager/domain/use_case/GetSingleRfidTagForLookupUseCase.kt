package org.rainrental.rainrentalrfid.inputmanager.domain.use_case

import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.inputmanager.data.manager.HardwareInputMode
import org.rainrental.rainrentalrfid.inputmanager.data.manager.InputManager
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.result.InputError
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

class GetSingleRfidTagForLookupUseCase @Inject constructor(
    private val rfidManager: RfidManager,
    private val inputManager: InputManager
) : Logger {
    suspend operator fun invoke(withPower:Int? = null): Result<RfidTagInfo, InputError>{
        val defaultLowPower = 2
        logd("Getting single RFID tag for lookup withPower $withPower and defaultPower $defaultLowPower")
        
        // Set input mode to RFID to ensure we're using the RFID scanner, not barcode
        inputManager.setInputMode(HardwareInputMode.RFID)
        
        try{
            return when (val response = rfidManager.readSingleTag(withPower?:defaultLowPower)){
                is Result.Error -> {
                    Result.Error(response.error)
                }
                is Result.Success -> {
                    // For lookup, we don't check if the tag is available - we just return the tag info
                    // regardless of whether it's already associated with an asset
                    Result.Success(response.data)
                }
            }
        }catch (e:Exception){
            loge("Error getting single RFID tag for lookup: ${e.message}")
            return Result.Error(InputError.HardwareError)
        }
    }
}

data class RfidTagInfo(
    val tid:String,
    val epc:String
)
