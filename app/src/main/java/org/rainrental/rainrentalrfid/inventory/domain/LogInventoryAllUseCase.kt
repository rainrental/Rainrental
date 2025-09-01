package org.rainrental.rainrentalrfid.inventory.domain

import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.commission.data.InventoryRecord
import org.rainrental.rainrentalrfid.commission.data.LogInventoryRequestDto
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

class LogInventoryAllUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val dependencies: BaseViewModelDependencies
) : Logger {

    suspend operator fun invoke(inventory: List<InventoryRecord>) {
        logd("Logging inventory all with ${inventory.size} items")
        
        try {
            inventoryRepository.setSaving(true)
            
            val request = LogInventoryRequestDto(
                companyId = dependencies.context.getString(org.rainrental.rainrentalrfid.R.string.company_id),
                assetSku = null, // No specific asset for inventory all
                inventory = inventory,
                isInventoryAll = true
            )
            
            val result = inventoryRepository.logInventory(request)
            
            when (result) {
                is org.rainrental.rainrentalrfid.result.Result.Success -> {
                    logd("Inventory all saved successfully")
                    inventoryRepository.updateUiFlow(InventoryFlow.WaitingForBarcode())
                }
                is org.rainrental.rainrentalrfid.result.Result.Error -> {
                    loge("Failed to save inventory all: ${result.error}")
                    inventoryRepository.updateUiFlow(InventoryFlow.WaitingForBarcode(withError = "Failed to save inventory"))
                }
            }
        } catch (e: Exception) {
            loge("Exception saving inventory all: ${e.message}")
            inventoryRepository.updateUiFlow(InventoryFlow.WaitingForBarcode(withError = "Error saving inventory"))
        } finally {
            inventoryRepository.setSaving(false)
        }
    }
}

