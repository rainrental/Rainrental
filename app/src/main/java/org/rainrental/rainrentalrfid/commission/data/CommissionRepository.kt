package org.rainrental.rainrentalrfid.commission.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiFlow
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiState
import org.rainrental.rainrentalrfid.rainrental.data.model.EncodeEpcRequestDto
import org.rainrental.rainrentalrfid.result.ApiError
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto
import javax.inject.Inject

interface CommissionRepository {
    val commissionApi: CommissionApi
    val uiState: StateFlow<CommissionUiState>
    val uiFlow: SharedFlow<CommissionUiFlow>
    suspend fun updateUiFlow(uiFlow: CommissionUiFlow)
    suspend fun setSaving(saving:Boolean){}
    suspend fun getAsset(barcode:String): Result<AssetDetailsResponseDto,ApiError>
    suspend fun getEpc(encodeEpcRequestDto: EncodeEpcRequestDto): Result<String,ApiError>
    suspend fun commissionTag(requestDto: CommissionTagRequestDto) : Result<CommissionTagResponseDto, ApiError>
    suspend fun isTagAvailable(tidHex: String): Result<Boolean, ApiError>
}

class DummyCommissionRepository @Inject constructor(
    override val commissionApi: CommissionApi
) : CommissionRepository{

    private val _uiState: MutableStateFlow<CommissionUiState> = MutableStateFlow(CommissionUiState())
    override val uiState: StateFlow<CommissionUiState> = _uiState.asStateFlow()

    private val _uiFlow: MutableSharedFlow<CommissionUiFlow> = MutableSharedFlow()
    override val uiFlow: SharedFlow<CommissionUiFlow> = _uiFlow.asSharedFlow()

    override suspend fun updateUiFlow(uiFlow: CommissionUiFlow) {
        _uiFlow.emit(uiFlow)
    }

    override suspend fun getAsset(barcode: String): Result<AssetDetailsResponseDto, ApiError> {
        val asset = AssetDetailsResponseDto.example()
        return Result.Success(asset)
    }

    override suspend fun getEpc(encodeEpcRequestDto: EncodeEpcRequestDto): Result<String, ApiError> {
        return Result.Success("AABBCCDDEEFFAABBCCDDEEFF")
    }

    override suspend fun commissionTag(requestDto: CommissionTagRequestDto): Result<CommissionTagResponseDto,ApiError> {
        return Result.Success(CommissionTagResponseDto.example())
    }

    override suspend fun isTagAvailable(tidHex: String): Result<Boolean, ApiError> {
        return Result.Success(true)
    }

}