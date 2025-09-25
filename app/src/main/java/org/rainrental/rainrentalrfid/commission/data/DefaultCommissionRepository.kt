package org.rainrental.rainrentalrfid.commission.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.rainrental.rainrentalrfid.apis.ApiCaller
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiState
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiFlow
import org.rainrental.rainrentalrfid.rainrental.data.RainRentalApi
import org.rainrental.rainrentalrfid.rainrental.data.model.EncodeEpcRequestDto
import org.rainrental.rainrentalrfid.rainrental.data.model.EncodeEpcResponseDto
import org.rainrental.rainrentalrfid.result.ApiError
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto
import javax.inject.Inject
import javax.inject.Named

class DefaultCommissionRepository @Inject constructor(
    override val backendApi: BackendApi,
    private val rainRentalApi: RainRentalApi,
    @Named("company_id") private val companyId: String,
) : CommissionRepository {
    private val _uiState: MutableStateFlow<CommissionUiState> = MutableStateFlow(CommissionUiState())
    override val uiState: StateFlow<CommissionUiState> = _uiState.asStateFlow()

    private val _uiFlow: MutableStateFlow<CommissionUiFlow> = MutableStateFlow(CommissionUiFlow.WaitingForBarcodeInput())
    override val uiFlow: StateFlow<CommissionUiFlow> = _uiFlow.asStateFlow()

    override suspend fun updateUiFlow(uiFlow: CommissionUiFlow) {
        _uiFlow.value = uiFlow
    }

    override suspend fun setSaving(saving: Boolean) {
        _uiState.update { it.copy(saving = saving) }
    }

    override suspend fun getAsset(barcode: String): Result<AssetDetailsResponseDto, ApiError> {
        val request = GetAssetRequestDto(barcode = barcode, companyId = companyId)
        return when (val result = ApiCaller()<AssetDetailsResponseDto>{ backendApi.getAssetDetails(request)}){
            is Result.Error -> Result.Error(result.error.apiErrorType)
            is Result.Success -> Result.Success(result.data)
            else -> Result.Error(ApiError.UnknownException)
        }
    }

    override suspend fun getEpc(encodeEpcRequestDto: EncodeEpcRequestDto): Result<String, ApiError> {
        return when (val result = ApiCaller()<EncodeEpcResponseDto>{ rainRentalApi.encodeEpc(encodeEpcRequestDto) }){
            is Result.Error -> Result.Error(result.error.apiErrorType)
            is Result.Success -> Result.Success(result.data.epcHexString)
            else -> Result.Error(ApiError.UnknownException)
        }
    }

    override suspend fun commissionTag(requestDto: CommissionTagRequestDto): Result<CommissionTagResponseDto,ApiError> {
        return when (val result = ApiCaller()<CommissionTagResponseDto> { backendApi.commissionTag(requestDto) }){
            is Result.Error -> Result.Error(result.error.apiErrorType)
            is Result.Success -> Result.Success(result.data)
            else -> Result.Error(ApiError.UnknownException)
        }
    }

    override suspend fun isTagAvailable(tidHex: String): Result<Boolean, ApiError> {
        return when (val result = ApiCaller()<IsTagAvailableResponseDto> { backendApi.isTagAvailable(IsTagAvailableRequestDto(tidHex = tidHex, companyId = companyId)) }){ //TODO get from settings
            is Result.Error -> Result.Error(result.error.apiErrorType)
            is Result.Success -> Result.Success(result.data.success)
        }
    }

    override suspend fun deleteTag(barcode: String, tidHex: String): Result<DeleteTagResponseDto, ApiError> {
        val request = DeleteTagRequestDto(barcode = barcode, tidHex = tidHex, companyId = companyId)
        return when (val result = ApiCaller()<DeleteTagResponseDto> { backendApi.deleteTag(request) }) {
            is Result.Error -> Result.Error(result.error.apiErrorType)
            is Result.Success -> Result.Success(result.data)
            else -> Result.Error(ApiError.UnknownException)
        }
    }

}