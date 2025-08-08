package org.rainrental.rainrentalrfid.inventory.domain

import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto
import javax.inject.Inject

class StartInventoryUseCase @Inject constructor(
    private val rfidManager: RfidManager,
    private val inventoryRepository: InventoryRepository,
) : Logger{
    suspend operator fun invoke(asset: AssetDetailsResponseDto){
        val useFilter = asset.epc.slice(0..10)
        logd("Setting epc filter to $useFilter")
        when (val didSetFilter = rfidManager.configureEpcFilter(useFilter)){
            false -> {
                inventoryRepository.updateUiFlow(InventoryFlow.ReadyToCount(asset = asset, withError = "Could not set EPC Filter"))
            }
            true -> {
                inventoryRepository.updateUiFlow(InventoryFlow.Counting(asset = asset, currentCount = 0))
                rfidManager.startInventoryScan(asset.epc)
            }
        }

    }
}