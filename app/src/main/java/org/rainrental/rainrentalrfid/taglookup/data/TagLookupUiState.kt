package org.rainrental.rainrentalrfid.taglookup.data

import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto

data class TagLookupUiState(
    val loading: Boolean = false
)

sealed interface TagLookupUiFlow {
    data object WaitingForTag : TagLookupUiFlow
    
    data object ScanningTag : TagLookupUiFlow
    
    data class LookingUpAsset(val tidHex: String, val scannedEpc: String) : TagLookupUiFlow
    
    data class AssetFound(
        val asset: AssetDetailsResponseDto,
        val tidHex: String,
        val scannedEpc: String
    ) : TagLookupUiFlow
    
    data class AssetNotFound(
        val tidHex: String,
        val scannedEpc: String,
        val withError: String? = null
    ) : TagLookupUiFlow
    
    data class TagDeleted(
        val tidHex: String,
        val scannedEpc: String,
        val deletedFrom: String? = null
    ) : TagLookupUiFlow
    
    data class ClearingEpc(
        val tidHex: String,
        val scannedEpc: String
    ) : TagLookupUiFlow
}

sealed interface TagLookupEvent {
    data object OnTriggerUp : TagLookupEvent
    data object OnSideKeyUp : TagLookupEvent
    data class DeleteTag(val tidHex: String) : TagLookupEvent
    data class ConfirmDeleteTag(val tidHex: String) : TagLookupEvent
    data object CancelDeleteTag : TagLookupEvent
}
