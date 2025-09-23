package org.rainrental.rainrentalrfid.inventory.domain

import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject
import javax.inject.Named

class StartGeneralInventoryUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val dependencies: BaseViewModelDependencies,
    @Named("company_id") private val companyId: String
) : Logger {

    suspend operator fun invoke() {
        logd("Starting general inventory - using company EPC filter to capture all company assets")
        
        // Clear any existing inventory by stopping current scan
        dependencies.rfidManager.stopInventoryScan()
        
        // Apply company-specific EPC filter (like continuous scanning does)
        // This will filter to only company tags, not all tags
        val companyEpcFilter = createCompanyEpcFilter(companyId.toInt())
        val success = dependencies.rfidManager.configureEpcFilter(companyEpcFilter)
        
        if (success) {
            logd("Company EPC filter configured successfully - ready to capture all company assets")
            // Start inventory scan with company EPC filter
            val scanSuccess = dependencies.rfidManager.startInventoryScan("")
            if (scanSuccess) {
                inventoryRepository.updateUiFlow(InventoryFlow.GeneralInventoryCounting(currentCount = 0))
            } else {
                inventoryRepository.updateUiFlow(InventoryFlow.GeneralInventory(withError = "Could not start inventory scan"))
            }
        } else {
            logd("Failed to configure company EPC filter")
            inventoryRepository.updateUiFlow(InventoryFlow.GeneralInventory(withError = "Could not configure company EPC filter"))
        }
    }
    
    /**
     * Creates a company-specific EPC filter similar to continuous scanning
     * This filters to only tags belonging to the company
     */
    private fun createCompanyEpcFilter(companyId: Int): String {
        // Convert company ID to 16-bit binary string with proper padding
        val companyIdBits = Integer.toBinaryString(companyId).padStart(16, '0')
        
        // RainRental standard prefix is "1111" (4 bits)
        // Company ID is 16 bits
        // Total filter length is 20 bits
        val filterValue = "1111$companyIdBits"
        
        logd("Created company EPC filter: $filterValue for company ID: $companyId")
        logd("Filter breakdown: RainRental prefix='1111', Company ID bits='$companyIdBits'")
        
        val hexValue = Integer.toHexString(filterValue.toInt(2)).uppercase()
        logd("Hex value of company filter: $hexValue")
        
        if (hexValue.length % 2 == 1) {
            logd("Adding trailing zero to hex value to make it even: ${hexValue}0")
            return hexValue + "0"
        }
        return hexValue
    }
}
