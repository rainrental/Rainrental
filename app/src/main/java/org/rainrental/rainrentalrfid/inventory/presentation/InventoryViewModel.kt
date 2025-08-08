package org.rainrental.rainrentalrfid.inventory.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
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
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val getBarcodeAndLookupAssetUseCase: GetBarcodeAndLookupAssetUseCase,
    private val startInventoryUseCase: StartInventoryUseCase,
    private val stopInventoryUseCase: StopInventoryUseCase,
    private val logInventoryUseCase: LogInventoryUseCase,
    dependencies: BaseViewModelDependencies
) : BaseViewModel(dependencies = dependencies), Logger {

    val uiFlow = inventoryRepository.uiFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = InventoryFlow.WaitingForBarcode()
    )

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
                    is InventoryFlow.LookingUpAsset -> {}
                    is InventoryFlow.ReadyToCount -> viewModelScope.launch{
                        startInventoryUseCase(asset = (uiFlow.value as InventoryFlow.ReadyToCount).asset)
                    }
                    is InventoryFlow.Counting -> viewModelScope.launch{
                        stopInventoryUseCase(asset = (uiFlow.value as InventoryFlow.Counting).asset)
                    }
                    is InventoryFlow.FinishedCounting -> {}
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
        onEvent(InventoryEvent.OnKeyUp)
    }
    
    override fun onSideKeyUp() {
        onEvent(InventoryEvent.OnKeyUp)
    }

}