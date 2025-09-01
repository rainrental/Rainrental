package org.rainrental.rainrentalrfid.inventory.domain

import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

class StartInventoryAllUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val dependencies: BaseViewModelDependencies
) : Logger {

    suspend operator fun invoke() {
        logd("Starting inventory all - clearing EPC filter to capture all company assets")
        
        // Clear any existing inventory by stopping current scan
        dependencies.rfidManager.stopInventoryScan()
        
        // Clear EPC filter to capture all company assets (no specific filter)
        val success = dependencies.rfidManager.clearEpcFilter()
        
        if (success) {
            logd("EPC filter cleared successfully - ready to capture all assets")
            // Start inventory scan without specific EPC filter
            val scanSuccess = dependencies.rfidManager.startInventoryScan("")
            if (scanSuccess) {
                inventoryRepository.updateUiFlow(InventoryFlow.InventoryAllCounting(currentCount = 0))
            } else {
                inventoryRepository.updateUiFlow(InventoryFlow.InventoryAll(withError = "Could not start inventory scan"))
            }
        } else {
            logd("Failed to clear EPC filter")
            inventoryRepository.updateUiFlow(InventoryFlow.InventoryAll(withError = "Could not clear EPC Filter"))
        }
    }
}
