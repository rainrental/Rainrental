package org.rainrental.rainrentalrfid.inventory.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.app.BaseViewModel
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.commission.data.InventoryRecord
import org.rainrental.rainrentalrfid.inventory.data.InventoryEvent
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.inventory.data.InventoryRepository
import org.rainrental.rainrentalrfid.inventory.domain.GetBarcodeAndLookupAssetUseCase
import org.rainrental.rainrentalrfid.inventory.domain.LogInventoryUseCase
import org.rainrental.rainrentalrfid.inventory.domain.StartInventoryUseCase
import org.rainrental.rainrentalrfid.inventory.domain.StopInventoryUseCase
import org.rainrental.rainrentalrfid.inventory.domain.StartGeneralInventoryUseCase
import org.rainrental.rainrentalrfid.inventory.domain.StopGeneralInventoryUseCase
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.navigation.BackConfirmableFeature
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val getBarcodeAndLookupAssetUseCase: GetBarcodeAndLookupAssetUseCase,
    private val startInventoryUseCase: StartInventoryUseCase,
    private val stopInventoryUseCase: StopInventoryUseCase,
    private val logInventoryUseCase: LogInventoryUseCase,
    private val startGeneralInventoryUseCase: StartGeneralInventoryUseCase,
    private val stopGeneralInventoryUseCase: StopGeneralInventoryUseCase,
    dependencies: BaseViewModelDependencies
) : BaseViewModel(dependencies = dependencies), Logger, BackConfirmableFeature {

    val uiFlow: StateFlow<InventoryFlow> = inventoryRepository.uiFlow

    val inventory = dependencies.rfidManager.inventoryCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0
    )

    val saving = inventoryRepository.saving.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    // Check if inventory is empty - derived from inventory count
    val isInventoryEmpty = inventory.map { count -> count == 0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = true
    )

    fun onEvent(event: InventoryEvent){
        when (event){

            InventoryEvent.OnKeyUp -> {
                when (uiFlow.value){
                    is InventoryFlow.WaitingForBarcode -> viewModelScope.launch{
                        getBarcodeAndLookupAssetUseCase()
                    }
                    is InventoryFlow.ManualBarcodeEntry -> {}
                    is InventoryFlow.LookingUpAsset -> {}
                    is InventoryFlow.ReadyToCount -> viewModelScope.launch{
                        startInventoryUseCase(asset = (uiFlow.value as InventoryFlow.ReadyToCount).asset)
                    }
                    is InventoryFlow.Counting -> viewModelScope.launch{
                        stopInventoryUseCase(asset = (uiFlow.value as InventoryFlow.Counting).asset)
                    }
                    is InventoryFlow.FinishedCounting -> {}
                    is InventoryFlow.GeneralInventory -> viewModelScope.launch{
                        startGeneralInventoryUseCase()
                    }
                    is InventoryFlow.GeneralInventoryCounting -> viewModelScope.launch{
                        stopGeneralInventoryUseCase()
                    }
                    is InventoryFlow.GeneralInventoryFinished -> {}
                }
            }

            InventoryEvent.ManualEntry -> {
                when (uiFlow.value){
                    is InventoryFlow.WaitingForBarcode -> viewModelScope.launch{
                        inventoryRepository.updateUiFlow(InventoryFlow.ManualBarcodeEntry())
                    }
                    else -> {}
                }
            }


            InventoryEvent.GeneralInventory -> {
                when (uiFlow.value){
                    is InventoryFlow.WaitingForBarcode -> viewModelScope.launch{
                        inventoryRepository.updateUiFlow(InventoryFlow.GeneralInventory())
                    }
                    else -> {}
                }
            }

            is InventoryEvent.ManualBarcodeSubmitted -> {
                when (uiFlow.value){
                    is InventoryFlow.WaitingForBarcode, is InventoryFlow.ManualBarcodeEntry -> viewModelScope.launch{
                        val barcode = event.barcode
                        if (barcode.isNotBlank()) {
                            inventoryRepository.updateUiFlow(InventoryFlow.LookingUpAsset(barcode = barcode))
                            val assetResult = inventoryRepository.getAsset(barcode)
                            when (assetResult) {
                                is Result.Success -> {
                                    inventoryRepository.updateUiFlow(InventoryFlow.ReadyToCount(asset = assetResult.data))
                                }
                                is Result.Error -> {
                                    inventoryRepository.updateUiFlow(InventoryFlow.WaitingForBarcode(withError = assetResult.error.name))
                                }
                            }
                        } else {
                            inventoryRepository.updateUiFlow(InventoryFlow.WaitingForBarcode(withError = "Barcode cannot be empty"))
                        }
                    }
                    else -> {}
                }
            }

            InventoryEvent.Save -> {
                when (uiFlow.value){
                    is InventoryFlow.FinishedCounting -> viewModelScope.launch {
                        val flow = (uiFlow.value as InventoryFlow.FinishedCounting)
                        logd("Saving inventory with ${dependencies.rfidManager.inventory.value.size} items")
                        logInventoryUseCase(asset = flow.asset, inventory = dependencies.rfidManager.inventory.value.map { InventoryRecord(tidHex = it.value.tid, epcHex = it.value.epc) })
                    }
                    else -> {}
                }

            }

            InventoryEvent.Finish -> {
                when (uiFlow.value){
                    is InventoryFlow.FinishedCounting -> viewModelScope.launch {
                        // Return to waiting state without saving
                        logd("Finishing inventory without saving - inventory was empty")
                        inventoryRepository.updateUiFlow(InventoryFlow.WaitingForBarcode())
                    }
                    else -> {}
                }
            }

            InventoryEvent.Beep -> {
                blipBeep()
            }
        }
    }

    override fun onTriggerUp() {
        logd("InventoryViewModel: onTriggerUp called")
        onEvent(InventoryEvent.OnKeyUp)
    }
    
    override fun onSideKeyUp() {
        logd("InventoryViewModel: onSideKeyUp called")
        onEvent(InventoryEvent.OnKeyUp)
    }

    // BackConfirmableFeature implementation
    override fun hasUnsavedChanges(): Boolean {
        val currentFlow = uiFlow.value
        return when (currentFlow) {
            is InventoryFlow.ReadyToCount,
            is InventoryFlow.Counting,
            is InventoryFlow.FinishedCounting,
            is InventoryFlow.GeneralInventoryCounting,
            is InventoryFlow.GeneralInventoryFinished -> true
            else -> false
        }
    }

    override fun resetState() {
        viewModelScope.launch {
            logd("Resetting inventory state")
            // Stop any ongoing inventory operations
            dependencies.rfidManager.stopInventoryScan()
            dependencies.rfidManager.clearEpcFilter()
            
            // Reset to initial state
            inventoryRepository.updateUiFlow(InventoryFlow.WaitingForBarcode())
        }
    }

    override fun getUnsavedChangesDescription(): String {
        val currentFlow = uiFlow.value
        return when (currentFlow) {
            is InventoryFlow.ReadyToCount -> "You have a product loaded and ready to count"
            is InventoryFlow.Counting -> "You are currently counting inventory"
            is InventoryFlow.FinishedCounting -> "You have completed counting but haven't saved"
            is InventoryFlow.GeneralInventoryCounting -> "You are currently doing general inventory"
            is InventoryFlow.GeneralInventoryFinished -> "You have completed general inventory but haven't saved"
            else -> "You have unsaved changes"
        }
    }

}