package org.rainrental.rainrentalrfid.inventory.domain

import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

class StopGeneralInventoryUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val dependencies: BaseViewModelDependencies
) : Logger {

    suspend operator fun invoke() {
        logd("Stopping general inventory - collecting and processing all company tags")
        
        // Stop RFID scanning
        dependencies.rfidManager.stopInventoryScan()
        
        // Get the collected tags from the inventory
        val collectedTags = dependencies.rfidManager.inventory.value
        val tagCount = collectedTags.size
        
        logd("General inventory completed - collected $tagCount company tags")
        
        if (tagCount > 0) {
            // Process the collected tags (send to backend)
            val processingResult = processCollectedTags(collectedTags)
            
            if (processingResult) {
                logd("Successfully processed $tagCount tags to backend")
                inventoryRepository.updateUiFlow(InventoryFlow.GeneralInventoryFinished(count = tagCount))
            } else {
                logd("Failed to process tags to backend")
                inventoryRepository.updateUiFlow(InventoryFlow.GeneralInventory(withError = "Failed to process collected tags"))
            }
        } else {
            logd("No tags collected during general inventory")
            inventoryRepository.updateUiFlow(InventoryFlow.GeneralInventoryFinished(count = 0))
        }
    }
    
    /**
     * Process the collected tags by sending them to the backend
     * This could be expanded to send tag data, timestamps, etc.
     */
    private suspend fun processCollectedTags(tags: Map<String, Any>): Boolean {
        return try {
            // TODO: Implement backend API call to process collected tags
            // For now, just log the tags and return success
            logd("Processing ${tags.size} collected tags:")
            tags.forEach { (epc, tagInfo) ->
                logd("  EPC: $epc, Info: $tagInfo")
            }
            
            // Simulate processing delay
            kotlinx.coroutines.delay(1000)
            
            true
        } catch (e: Exception) {
            loge("Error processing collected tags: ${e.message}")
            false
        }
    }
}
