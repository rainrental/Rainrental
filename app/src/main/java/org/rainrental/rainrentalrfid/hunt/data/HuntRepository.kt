package org.rainrental.rainrentalrfid.hunt.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.rainrental.rainrentalrfid.apis.ApiCaller
import org.rainrental.rainrentalrfid.commission.data.BackendApi
import org.rainrental.rainrentalrfid.commission.data.GetAssetRequestDto
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.result.ApiError
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto
import javax.inject.Inject
import javax.inject.Named

interface HuntRepository : Logger {
    val backendApi : BackendApi
    val uiFlow: SharedFlow<HuntFlow>
    suspend fun updateUiFlow(flow:HuntFlow)
    suspend fun getAsset(barcode:String): Result<AssetDetailsResponseDto, ApiError>
}

class DefaultHuntRepository @Inject constructor(
    override val backendApi: BackendApi,
    @Named("company_id") private val companyId: String
) : HuntRepository{

    private val _uiFlow : MutableSharedFlow<HuntFlow> = MutableSharedFlow()
    override val uiFlow: SharedFlow<HuntFlow> = _uiFlow.asSharedFlow()
    override suspend fun updateUiFlow(flow: HuntFlow) {
        _uiFlow.emit(flow)
    }
    override suspend fun getAsset(barcode: String): Result<AssetDetailsResponseDto, ApiError> {
        val request = GetAssetRequestDto(barcode = barcode, companyId = companyId)
        return when (val result = (ApiCaller())<AssetDetailsResponseDto>{ backendApi.getAssetDetails(request)}){
            is Result.Error -> Result.Error(result.error.apiErrorType)
            is Result.Success -> Result.Success(result.data)
            else -> Result.Error(ApiError.UnknownException)
        }
    }
}

sealed interface HuntFlow{
    data class WaitingForBarcode(
        val withError: String? = null,
        val previousHuntResults: Int? = null
    ) : HuntFlow
    data object ScanningBarcode : HuntFlow
    data class LookingUpAsset(val barcode:String) : HuntFlow
    data class LoadedAsset(val asset: AssetDetailsResponseDto, val withError: String? = null) : HuntFlow
    data class Hunting(val asset: AssetDetailsResponseDto, val lastRssi: Double? = null) : HuntFlow
}

sealed interface HuntEvent{
    data object OnKeyUp : HuntEvent
    data class OnManualBarcodeEntry(val barcode: String) : HuntEvent
}