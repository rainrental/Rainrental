package org.rainrental.rainrentalrfid.inputmanager.domain.use_case

import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.result.InputError
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

class WriteEpcUseCase @Inject constructor(
    private val rfidManager: RfidManager,
) {
    suspend operator fun invoke(tid:String, epc:String): Result<Boolean, InputError> {
        return when(val result = rfidManager.writeTagEpc(tid = tid, epc = epc)){
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> Result.Success(result.data)
        }
    }
}

/**
 * Read TID
 * ptr 0
 * len 6
 * bank tid
 *
 * Read EPC
 * ptr 2
 * len 6
 * bank epc
 */