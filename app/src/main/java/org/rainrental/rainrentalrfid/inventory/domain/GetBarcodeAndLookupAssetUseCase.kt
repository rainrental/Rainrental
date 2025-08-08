package org.rainrental.rainrentalrfid.inventory.domain

import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.ScanBarcodeUseCase
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.shared.domain.use_case.TriggerToastUseCase
import javax.inject.Inject

class GetBarcodeAndLookupAssetUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val scanBarcodeUseCase: ScanBarcodeUseCase,
    private val triggerToastUseCase: TriggerToastUseCase,

    ) {
    suspend operator fun invoke(){
        when (val barcode = scanBarcodeUseCase()){
            is Result.Error -> {
                inventoryRepository.updateUiFlow(InventoryFlow.WaitingForBarcode(barcode.error.name))
            }
            is Result.Success -> {
                inventoryRepository.updateUiFlow(InventoryFlow.LookingUpAsset(barcode = barcode.data))
                when (val asset = inventoryRepository.getAsset(barcode.data)){
                    is Result.Error -> {
                        triggerToastUseCase(asset.error.name)
                        inventoryRepository.updateUiFlow(InventoryFlow.WaitingForBarcode(asset.error.name))
                    }
                    is Result.Success -> {
                        inventoryRepository.updateUiFlow(InventoryFlow.ReadyToCount(asset = asset.data))
                    }
                }
            }
        }
    }
}