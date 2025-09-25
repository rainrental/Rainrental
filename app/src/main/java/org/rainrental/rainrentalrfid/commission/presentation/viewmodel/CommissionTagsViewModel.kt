package org.rainrental.rainrentalrfid.commission.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient.Mqtt3SubscribeAndCallbackBuilder.Call.Ex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.app.BaseViewModel
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.commission.data.CommissionRepository
import org.rainrental.rainrentalrfid.commission.domain.use_case.EncodeRequestUseCase
import org.rainrental.rainrentalrfid.commission.domain.use_case.GetBarcodeAndLookupAssetUseCase
import org.rainrental.rainrentalrfid.commission.domain.use_case.SaveCommissionUseCase
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionEvent
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiFlow
import org.rainrental.rainrentalrfid.commission.presentation.model.ScanningTagData
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.GetSingleRfidTagUseCase
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.WriteEpcUseCase
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.navigation.BackConfirmableFeature
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

@HiltViewModel
class CommissionTagsViewModel @Inject constructor(
    private val commissionRepository: CommissionRepository,
    private val getBarcodeAndLookupAssetUseCase: GetBarcodeAndLookupAssetUseCase,
    private val getSingleRfidTagUseCase: GetSingleRfidTagUseCase,
    private val writeEpcUseCase: WriteEpcUseCase,
    private val encodeRequestUseCase: EncodeRequestUseCase,
    private val saveCommissionUseCase: SaveCommissionUseCase,
    dependencies: BaseViewModelDependencies
): BaseViewModel(dependencies = dependencies), Logger, BackConfirmableFeature {



    val uiState = commissionRepository.uiState
    val uiFlow : StateFlow<CommissionUiFlow> = commissionRepository.uiFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = CommissionUiFlow.WaitingForBarcodeInput()
    )


    fun onEvent(event: CommissionEvent) {
        when (event) {
            CommissionEvent.ScanTagButton -> {}
            CommissionEvent.OnKeyUp -> {
                when (uiFlow.value) {
                    is CommissionUiFlow.WaitingForBarcodeInput -> viewModelScope.launch { getBarcodeAndLookupAssetUseCase() }
                    CommissionUiFlow.ScanningBarcode -> {}
                    is CommissionUiFlow.LookingUpAsset -> {}
                    is CommissionUiFlow.CommissionedTags -> {}
                    is CommissionUiFlow.CommissioningTags -> {}
                    is CommissionUiFlow.LoadedAsset -> {
                        if ((uiFlow.value as CommissionUiFlow.LoadedAsset).scannedTags.none { !it.writtenEpc } ){
                            // All tags are encoded, scan another tag
                            viewModelScope.launch{
                                when (val rfidTag = getSingleRfidTagUseCase()){
                                    is Result.Error -> triggerToast("Error getting tag ${rfidTag.error.name}")
                                    is Result.Success -> {
                                        val oldTags = (uiFlow.value as CommissionUiFlow.LoadedAsset).scannedTags
                                        commissionRepository.updateUiFlow(CommissionUiFlow.LoadedAsset((uiFlow.value as CommissionUiFlow.LoadedAsset).asset, scannedTags = oldTags + ScanningTagData(tidHex = rfidTag.data.tid, epcHex = rfidTag.data.epc)))
                                    }
                                }
                            }
                        }else{
                            val oldFlow = uiFlow.value as CommissionUiFlow.LoadedAsset
                            // Not all tags are encoded, write the un-encoded tag
                            viewModelScope.launch {
                                val scannedTagData = ScanningTagData(
                                    tidHex = oldFlow.scannedTags.first { !it.writtenEpc }.tidHex,
                                    epcHex = oldFlow.scannedTags.first { !it.writtenEpc }.tidHex,
                                    epcData = oldFlow.asset.epc
                                )
                                encodeRequestUseCase(event = CommissionEvent.EncodeEpcButtonPressed(scannedTagData = scannedTagData), oldFlow = oldFlow)
                            }
                        }
                    }
                    is CommissionUiFlow.ScanningRfid -> {}
                    is CommissionUiFlow.WritingEPC -> {}
                }
            }

            is CommissionEvent.EncodeEpcButtonPressed -> {
                when (uiFlow.value){
                    is CommissionUiFlow.LoadedAsset -> {
                        val oldFlow = uiFlow.value as CommissionUiFlow.LoadedAsset
                        viewModelScope.launch {
                            encodeRequestUseCase(event = event,oldFlow = oldFlow)
                        }
                    }
                    else -> {}
                }


            }

            CommissionEvent.SaveButtonPressed -> {
                when (uiFlow.value){
                    is CommissionUiFlow.LoadedAsset -> {
                        try{
                            val flow = uiFlow.value as CommissionUiFlow.LoadedAsset
                            viewModelScope.launch {
                                saveCommissionUseCase(barcode = flow.asset.barcode, scannedTags = flow.scannedTags)
                            }
                        }catch (e:Exception){
                            triggerToast("Error saving tags\n$e")
                        }
                    }
                    else -> {

                    }
                }
            }

            is CommissionEvent.DeleteTagPressed -> {
                when (uiFlow.value) {
                    is CommissionUiFlow.LoadedAsset -> {
                        viewModelScope.launch {
                            val result = commissionRepository.deleteTag(event.barcode, event.tidHex)
                            when (result) {
                                is Result.Success -> {
                                    triggerToast("Tag deleted successfully")
                                    // Refresh the asset to get updated tag list
                                    val assetResult = commissionRepository.getAsset(event.barcode)
                                    when (assetResult) {
                                        is Result.Success -> {
                                            val currentFlow = uiFlow.value as CommissionUiFlow.LoadedAsset
                                            commissionRepository.updateUiFlow(
                                                CommissionUiFlow.LoadedAsset(
                                                    asset = assetResult.data,
                                                    scannedTags = currentFlow.scannedTags
                                                )
                                            )
                                        }
                                        is Result.Error -> {
                                            triggerToast("Error refreshing asset: ${assetResult.error.name}")
                                        }
                                    }
                                }
                                is Result.Error -> {
                                    triggerToast("Error deleting tag: ${result.error.name}")
                                }
                            }
                        }
                    }
                    else -> {
                        triggerToast("Cannot delete tag in current state")
                    }
                }
            }
        }
    }

    override fun onTriggerUp() {
        logd("CommissionTagsViewModel: onTriggerUp called - IGNORED (not on commission screen)")
        // Disable barcode scan when not on commission screen
        // viewModelScope.launch {
        //     onEvent(CommissionEvent.OnKeyUp)
        // }
    }
    
    override fun onSideKeyUp() {
        viewModelScope.launch {
            onEvent(CommissionEvent.OnKeyUp)
        }
    }

    // BackConfirmableFeature implementation
    override fun hasUnsavedChanges(): Boolean {
        val currentFlow = uiFlow.value
        return when (currentFlow) {
            is CommissionUiFlow.LoadedAsset,
            is CommissionUiFlow.ScanningRfid,
            is CommissionUiFlow.WritingEPC,
            is CommissionUiFlow.CommissioningTags,
            is CommissionUiFlow.CommissionedTags -> true
            else -> false
        }
    }

    override fun resetState() {
        viewModelScope.launch {
            logd("Resetting commission state")
            // Stop any ongoing RFID operations
            dependencies.rfidManager.stopInventoryScan()
            dependencies.rfidManager.clearEpcFilter()
            
            // Reset to initial state
            commissionRepository.updateUiFlow(CommissionUiFlow.WaitingForBarcodeInput())
        }
    }

    override fun getUnsavedChangesDescription(): String {
        val currentFlow = uiFlow.value
        return when (currentFlow) {
            is CommissionUiFlow.LoadedAsset -> "You have a product loaded with ${currentFlow.scannedTags.size} tags"
            is CommissionUiFlow.ScanningRfid -> "You are currently scanning for RFID tags"
            is CommissionUiFlow.WritingEPC -> "You are currently writing EPC data to tags"
            is CommissionUiFlow.CommissioningTags -> "You are currently commissioning tags"
            is CommissionUiFlow.CommissionedTags -> "You have commissioned tags but haven't saved"
            else -> "You have unsaved changes"
        }
    }

}