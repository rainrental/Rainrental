package org.rainrental.rainrentalrfid.taglookup.data

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.rainrental.rainrentalrfid.result.ApiError
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto

interface TagLookupRepository {
    val uiState: StateFlow<TagLookupUiState>
    val uiFlow: SharedFlow<TagLookupUiFlow>
    
    suspend fun updateUiFlow(uiFlow: TagLookupUiFlow)
    suspend fun setLoading(loading: Boolean)
    suspend fun getAssetByTid(tidHex: String): Result<AssetDetailsResponseDto, ApiError>
}
