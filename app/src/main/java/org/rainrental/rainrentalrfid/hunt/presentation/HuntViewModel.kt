package org.rainrental.rainrentalrfid.hunt.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.app.BaseViewModel
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.hunt.data.HuntEvent
import org.rainrental.rainrentalrfid.hunt.data.HuntFlow
import org.rainrental.rainrentalrfid.hunt.data.HuntRepository
import org.rainrental.rainrentalrfid.hunt.domain.GetBarcodeAndLookupAssetUseCase
import org.rainrental.rainrentalrfid.hunt.domain.LookupAssetUseCase
import org.rainrental.rainrentalrfid.hunt.domain.StartTagHuntUseCase
import org.rainrental.rainrentalrfid.hunt.domain.StopHuntUseCase
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.navigation.BackConfirmableFeature
import javax.inject.Inject

@HiltViewModel
class HuntViewModel @Inject constructor(
    private val huntRepository: HuntRepository,
    private val getBarcodeAndLookupAssetUseCase: GetBarcodeAndLookupAssetUseCase,
    private val lookupAssetUseCase: LookupAssetUseCase,
    private val startTagHuntUseCase: StartTagHuntUseCase,
    private val stopHuntUseCase: StopHuntUseCase,
    dependencies: BaseViewModelDependencies,
) : BaseViewModel(dependencies), Logger, BackConfirmableFeature {



    val uiFlow = huntRepository.uiFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = HuntFlow.WaitingForBarcode()
    )

    val huntResults = dependencies.rfidManager.huntResults.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    fun onEvent(event:HuntEvent){
        when (event){
            HuntEvent.OnKeyUp -> {
                when (uiFlow.value){
                    is HuntFlow.WaitingForBarcode -> viewModelScope.launch { getBarcodeAndLookupAssetUseCase() }
                    HuntFlow.ScanningBarcode -> {}
                    is HuntFlow.LookingUpAsset -> {}
                    is HuntFlow.LoadedAsset -> viewModelScope.launch{
                        startTagHuntUseCase((uiFlow.value as HuntFlow.LoadedAsset).asset)
                    }
                    is HuntFlow.Hunting -> viewModelScope.launch{
                        stopHuntUseCase(asset = (uiFlow.value as HuntFlow.Hunting).asset)
                    }
                }
            }
            is HuntEvent.OnManualBarcodeEntry -> {
                viewModelScope.launch { lookupAssetUseCase(event.barcode) }
            }
        }
    }

    override fun onTriggerUp() {
        logd("HuntViewModel: onTriggerUp called - IGNORED (not on hunt screen)")
        // Disable barcode scan when not on hunt screen
        // onEvent(HuntEvent.OnKeyUp)
    }
    
    override fun onSideKeyUp() {
        logd("HuntViewModel: onSideKeyUp called - IGNORED (not on hunt screen)")
        // Disable barcode scan when not on hunt screen
        // onEvent(HuntEvent.OnKeyUp)
    }

    // BackConfirmableFeature implementation
    override fun hasUnsavedChanges(): Boolean {
        val currentFlow = uiFlow.value
        return when (currentFlow) {
            is HuntFlow.LoadedAsset,
            is HuntFlow.Hunting -> true
            else -> false
        }
    }

    override fun resetState() {
        viewModelScope.launch {
            logd("Resetting hunt state")
            // Stop any ongoing hunt operations
            dependencies.rfidManager.stopTagHunt()
            dependencies.rfidManager.clearEpcFilter()
            
            // Reset to initial state
            huntRepository.updateUiFlow(HuntFlow.WaitingForBarcode())
        }
    }

    override fun getUnsavedChangesDescription(): String {
        val currentFlow = uiFlow.value
        return when (currentFlow) {
            is HuntFlow.LoadedAsset -> "You have a product loaded and ready to hunt"
            is HuntFlow.Hunting -> "You are currently hunting for tags"
            else -> "You have unsaved changes"
        }
    }

}