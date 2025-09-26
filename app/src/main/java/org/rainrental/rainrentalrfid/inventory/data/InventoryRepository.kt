package org.rainrental.rainrentalrfid.inventory.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.rainrental.rainrentalrfid.apis.ApiCaller
import org.rainrental.rainrentalrfid.commission.data.BackendApi
import org.rainrental.rainrentalrfid.commission.data.LogInventoryRequestDto
import org.rainrental.rainrentalrfid.commission.data.LogInventoryResponseDto
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.result.ApiError
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto
import org.rainrental.rainrentalrfid.commission.data.GetAssetRequestDto
import javax.inject.Inject
import javax.inject.Named

interface InventoryRepository : Logger{
    val uiFlow: StateFlow<InventoryFlow>
    val saving: StateFlow<Boolean>
    suspend fun getAsset(barcode:String) : Result<AssetDetailsResponseDto,ApiError>
    suspend fun updateUiFlow(uiFlow: InventoryFlow)
    suspend fun logInventory(logInventoryRequestDto: LogInventoryRequestDto) : Result<LogInventoryResponseDto, ApiError>
    suspend fun setSaving(saving: Boolean)
}

class DefaultInventoryRepository @Inject constructor(
    private val backendApi: BackendApi,
    @Named("company_id") private val companyId: String
) : InventoryRepository {

    private val _uiFlow: MutableStateFlow<InventoryFlow> = MutableStateFlow(InventoryFlow.WaitingForBarcode())
    override val uiFlow: StateFlow<InventoryFlow> = _uiFlow.asStateFlow()

    private val _saving: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val saving: StateFlow<Boolean> = _saving.asStateFlow()


    override suspend fun getAsset(barcode: String) : Result<AssetDetailsResponseDto,ApiError> {
        val request = GetAssetRequestDto(barcode = barcode, companyId = companyId)
        return when (val result = (ApiCaller())<AssetDetailsResponseDto> { backendApi.getAssetDetails(request) }){
            is Result.Error -> Result.Error(result.error.apiErrorType)
            is Result.Success -> Result.Success(result.data)
        }
    }

    override suspend fun updateUiFlow(uiFlow: InventoryFlow) {
        _uiFlow.value = uiFlow
    }

    override suspend fun logInventory(logInventoryRequestDto: LogInventoryRequestDto): Result<LogInventoryResponseDto, ApiError> {
        return when (val result = ApiCaller()<LogInventoryResponseDto> { backendApi.logInventory(logInventoryRequestDto) }){
            is Result.Error -> {
                loge("Error saving inventory data")
                Result.Error(result.error.apiErrorType)
            }
            is Result.Success -> Result.Success(result.data)
        }
    }

    override suspend fun setSaving(saving: Boolean) {
        _saving.update { saving }
    }

}

sealed interface InventoryFlow{
    data class WaitingForBarcode(val withError:String? = null): InventoryFlow
    data class ManualBarcodeEntry(val withError:String? = null): InventoryFlow
    data class LookingUpAsset(val barcode: String): InventoryFlow
    data class ReadyToCount(val asset: AssetDetailsResponseDto, val withError: String? = null): InventoryFlow
    data class Counting(val asset: AssetDetailsResponseDto, val currentCount: Int): InventoryFlow
    data class FinishedCounting(val asset: AssetDetailsResponseDto, val count: Int): InventoryFlow
    data class GeneralInventory(val withError:String? = null): InventoryFlow
    data class GeneralInventoryCounting(val currentCount: Int): InventoryFlow
    data class GeneralInventoryFinished(val count: Int): InventoryFlow
}

sealed interface InventoryEvent{
    data object OnKeyUp: InventoryEvent
    data object Save: InventoryEvent
    data object Finish: InventoryEvent
    data object Beep: InventoryEvent
    data object ManualEntry: InventoryEvent
    data object GeneralInventory: InventoryEvent
    data class ManualBarcodeSubmitted(val barcode: String): InventoryEvent
}