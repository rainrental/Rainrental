package org.rainrental.rainrentalrfid.inventory.domain

import org.rainrental.rainrentalrfid.commission.data.InventoryRecord
import org.rainrental.rainrentalrfid.commission.data.LogInventoryRequestDto
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.shared.domain.use_case.TriggerToastUseCase
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.log

class LogInventoryUseCase @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val triggerToastUseCase: TriggerToastUseCase,
    @Named("company_id") private val companyId: String
) : Logger {
    suspend operator fun invoke(asset:AssetDetailsResponseDto, inventory:List<InventoryRecord>){
        val logInventoryRequestDto = LogInventoryRequestDto(
            epcFilter = asset.epc.slice(0..19),
            skuId = asset.skuId,
            sku = asset.sku,
            departmentId = asset.departmentId,
            department = asset.department,
            companyId = companyId,
            admin = org.rainrental.rainrentalrfid.app.deviceSerial,
            inventory = inventory
        )
        logd("Logging inventory for SKU: ${asset.sku}, Company ID: $companyId, Admin: ${org.rainrental.rainrentalrfid.app.deviceSerial}, Inventory count: ${inventory.size}")
        inventoryRepository.setSaving(true)
        when (val logResult = inventoryRepository.logInventory(logInventoryRequestDto)){
            is Result.Error -> {
                triggerToastUseCase("Error logging data. ${logResult.error.name}")
                inventoryRepository.setSaving(false)
            }
            is Result.Success -> {
                inventoryRepository.setSaving(false)
                when(logResult.data.success){
                    false -> triggerToastUseCase(logResult.data.message)
                    true -> {
                        triggerToastUseCase(logResult.data.message)
                        inventoryRepository.updateUiFlow(InventoryFlow.WaitingForBarcode())
                    }
                }
            }
        }
    }
}