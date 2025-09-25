package org.rainrental.rainrentalrfid.taglookup.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.rainrental.rainrentalrfid.apis.ApiCaller
import org.rainrental.rainrentalrfid.commission.data.BackendApi
import org.rainrental.rainrentalrfid.commission.data.GetAssetByTidRequestDto
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.result.ApiError
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto
import javax.inject.Inject
import javax.inject.Named

class DefaultTagLookupRepository @Inject constructor(
    private val backendApi: BackendApi,
    @Named("company_id") private val companyId: String,
) : TagLookupRepository, Logger {

    private val _uiState: MutableStateFlow<TagLookupUiState> = MutableStateFlow(TagLookupUiState())
    override val uiState: StateFlow<TagLookupUiState> = _uiState.asStateFlow()

    private val _uiFlow: MutableSharedFlow<TagLookupUiFlow> = MutableSharedFlow()
    override val uiFlow: SharedFlow<TagLookupUiFlow> = _uiFlow.asSharedFlow()

    override suspend fun updateUiFlow(uiFlow: TagLookupUiFlow) {
        _uiFlow.emit(uiFlow)
    }

    override suspend fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(loading = loading) }
    }

    override suspend fun getAssetByTid(tidHex: String): Result<AssetDetailsResponseDto, org.rainrental.rainrentalrfid.result.ApiCallerApiError<AssetDetailsResponseDto>> {
        val request = GetAssetByTidRequestDto(tidHex = tidHex, companyId = companyId)
        return when (val result = ApiCaller()<AssetDetailsResponseDto> { backendApi.getAssetByTid(request) }) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> Result.Success(result.data)
            else -> Result.Error(org.rainrental.rainrentalrfid.result.ApiCallerApiError(apiErrorType = ApiError.UnknownException))
        }
    }
}
