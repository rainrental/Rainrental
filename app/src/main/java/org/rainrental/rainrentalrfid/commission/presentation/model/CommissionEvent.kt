package org.rainrental.rainrentalrfid.commission.presentation.model

import org.rainrental.rainrentalrfid.commission.data.TagData

sealed interface CommissionEvent {
    data object ScanTagButton: CommissionEvent
    data class EncodeEpcButtonPressed(val scannedTagData: ScanningTagData): CommissionEvent
    data object OnKeyUp : CommissionEvent
    data object SaveButtonPressed : CommissionEvent
    data class DeleteTagPressed(val barcode: String, val tidHex: String): CommissionEvent

}