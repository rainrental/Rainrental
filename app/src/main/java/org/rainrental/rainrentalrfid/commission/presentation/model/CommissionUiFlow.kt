package org.rainrental.rainrentalrfid.commission.presentation.model

import org.rainrental.rainrentalrfid.commission.data.CommissionTagResponseDto
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto

sealed interface CommissionUiFlow {
    data class WaitingForBarcodeInput(val withError: String? = null) : CommissionUiFlow

    data object ScanningBarcode: CommissionUiFlow
    data class ScanningRfid(val withText: String): CommissionUiFlow

    data class LookingUpAsset(val barcode:String): CommissionUiFlow

    data class LoadedAsset(
        val asset: AssetDetailsResponseDto,
        val scannedTags: List<ScanningTagData> = emptyList(),
        val withError: String? = null
    ): CommissionUiFlow

    data class WritingEPC(
        val asset: AssetDetailsResponseDto,
        val scannedTags: List<ScanningTagData> = emptyList(),
        val writingEpc: String,
        val writingTid: String,
        val withError: String? = null
    ): CommissionUiFlow

    data class CommissioningTags(
        val asset: AssetDetailsResponseDto,
        val scannedTags: List<ScanningTagData> = emptyList(),
        val withError: String? = null
    ): CommissionUiFlow
    data class CommissionedTags(
        val asset: CommissionTagResponseDto,
        val withError: String? = null
    ): CommissionUiFlow

}

data class ScanningTagData(
    val tidHex: String,
    val epcHex: String,
    val writtenEpc: Boolean = false,
    val epcData: String? = null
)