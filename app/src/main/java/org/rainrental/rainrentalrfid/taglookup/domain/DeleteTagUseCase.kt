package org.rainrental.rainrentalrfid.taglookup.domain

import org.rainrental.rainrentalrfid.apis.ApiCaller
import org.rainrental.rainrentalrfid.commission.data.BackendApi
import org.rainrental.rainrentalrfid.commission.data.DeleteTagByTidRequestDto
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject
import javax.inject.Named

class DeleteTagUseCase @Inject constructor(
    private val backendApi: BackendApi,
    private val apiCaller: ApiCaller,
    @Named("company_id") private val companyId: String
) {
    suspend operator fun invoke(tidHex: String): Result<Unit, org.rainrental.rainrentalrfid.result.ApiError> {
        val request = DeleteTagByTidRequestDto(tidHex = tidHex, companyId = companyId)
        return when (val result = apiCaller { backendApi.deleteTagByTid(request) }) {
            is Result.Error -> Result.Error(result.error.apiErrorType)
            is Result.Success -> Result.Success(Unit)
            else -> Result.Error(org.rainrental.rainrentalrfid.result.ApiError.UnknownException)
        }
    }
}
