package org.rainrental.rainrentalrfid.taglookup.domain

import org.rainrental.rainrentalrfid.apis.ApiCaller
import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.commission.data.BackendApi
import org.rainrental.rainrentalrfid.commission.data.DeleteTagByTidRequestDto
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.WriteEpcUseCase
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject
import javax.inject.Named

class DeleteTagUseCase @Inject constructor(
    private val backendApi: BackendApi,
    private val apiCaller: ApiCaller,
    private val writeEpcUseCase: WriteEpcUseCase,
    @Named("company_id") private val companyId: String
) {
    suspend operator fun invoke(tidHex: String): org.rainrental.rainrentalrfid.result.Result<DeleteTagResult, org.rainrental.rainrentalrfid.result.ApiError> {
        // Step 1: Clear the EPC memory by writing the TID as the EPC
        // This prevents the tag from showing up in inventory counts
        when (val epcClearResult = writeEpcUseCase(tid = tidHex, epc = tidHex)) {
            is org.rainrental.rainrentalrfid.result.Result.Error -> {
                return org.rainrental.rainrentalrfid.result.Result.Error(org.rainrental.rainrentalrfid.result.ApiError.UnknownException)
            }
            is org.rainrental.rainrentalrfid.result.Result.Success -> {
                // EPC cleared successfully, continue with backend deletion
            }
        }
        
        // Step 2: Delete the tag from the backend
        val request = DeleteTagByTidRequestDto(tidHex = tidHex, companyId = companyId)
        return when (val result = apiCaller { backendApi.deleteTagByTid(request) }) {
            is org.rainrental.rainrentalrfid.result.Result.Error -> org.rainrental.rainrentalrfid.result.Result.Error(result.error.apiErrorType)
            is org.rainrental.rainrentalrfid.result.Result.Success -> org.rainrental.rainrentalrfid.result.Result.Success(DeleteTagResult.Success)
            else -> org.rainrental.rainrentalrfid.result.Result.Error(org.rainrental.rainrentalrfid.result.ApiError.UnknownException)
        }
    }
    
    suspend fun deleteFromBackendOnly(tidHex: String): org.rainrental.rainrentalrfid.result.Result<DeleteTagResult, org.rainrental.rainrentalrfid.result.ApiError> {
        // Skip EPC clearing and just delete from backend
        val request = DeleteTagByTidRequestDto(tidHex = tidHex, companyId = companyId)
        return when (val result = apiCaller { backendApi.deleteTagByTid(request) }) {
            is org.rainrental.rainrentalrfid.result.Result.Error -> org.rainrental.rainrentalrfid.result.Result.Error(result.error.apiErrorType)
            is org.rainrental.rainrentalrfid.result.Result.Success -> org.rainrental.rainrentalrfid.result.Result.Success(DeleteTagResult.Success)
            else -> org.rainrental.rainrentalrfid.result.Result.Error(org.rainrental.rainrentalrfid.result.ApiError.UnknownException)
        }
    }
}

sealed class DeleteTagResult {
    data object Success : DeleteTagResult()
}

