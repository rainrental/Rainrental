package org.rainrental.rainrentalrfid.inventory.domain

import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto
import javax.inject.Inject

class StopInventoryUseCase @Inject constructor(
    private val rfidManager: RfidManager,
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(asset:AssetDetailsResponseDto){
        when (rfidManager.stopInventoryScan()){
            true -> inventoryRepository.updateUiFlow(InventoryFlow.FinishedCounting(asset = asset, count = rfidManager.inventory.value.size))
            false -> {}
        }
    }
}