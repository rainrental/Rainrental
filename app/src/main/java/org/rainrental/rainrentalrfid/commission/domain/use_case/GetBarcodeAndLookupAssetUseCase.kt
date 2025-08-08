package org.rainrental.rainrentalrfid.commission.domain.use_case

import org.rainrental.rainrentalrfid.commission.data.CommissionRepository
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiFlow
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.ScanBarcodeUseCase
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.shared.domain.use_case.TriggerToastUseCase
import javax.inject.Inject

class GetBarcodeAndLookupAssetUseCase @Inject constructor(
    private val scanBarcodeUseCase: ScanBarcodeUseCase,
    private val triggerToastUseCase: TriggerToastUseCase,
    private val commissionRepository: CommissionRepository,
) {
    suspend operator fun invoke(){
        commissionRepository.updateUiFlow(CommissionUiFlow.ScanningBarcode)
        when (val barcode = scanBarcodeUseCase()){
            is Result.Error -> {
                commissionRepository.updateUiFlow(CommissionUiFlow.WaitingForBarcodeInput(withError = "Error getting barcode: ${barcode.error.name}"))
            }
            is Result.Success -> {
                commissionRepository.updateUiFlow(CommissionUiFlow.LookingUpAsset(barcode = barcode.data))
                when (val lookupResult = commissionRepository.getAsset(barcode.data)){
                    is Result.Error -> {
                        commissionRepository.updateUiFlow(CommissionUiFlow.WaitingForBarcodeInput("Error getting asset: ${lookupResult.error.name}"))
                    }
                    is Result.Success -> {
                        commissionRepository.updateUiFlow(CommissionUiFlow.LoadedAsset(asset = lookupResult.data))
                    }
                }
            }
        }
    }
}