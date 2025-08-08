package org.rainrental.rainrentalrfid.commission.presentation.model

data class CommissionUiState(
    val scanning: Boolean = true,
    val saving: Boolean = false,
)

enum class ScanningState{

}
