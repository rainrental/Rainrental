package org.rainrental.rainrentalrfid.inventory.domain

import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

class StopInventoryAllUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val dependencies: BaseViewModelDependencies
) : Logger {

    suspend operator fun invoke() {
        logd("Stopping inventory all - final count: ${dependencies.rfidManager.inventory.value.size}")
        
        // Stop RFID scanning
        dependencies.rfidManager.stopInventoryScan()
        
        // Update UI flow to show finished state
        inventoryRepository.updateUiFlow(InventoryFlow.InventoryAllFinished(count = dependencies.rfidManager.inventory.value.size))
    }
}
