package org.rainrental.rainrentalrfid.taglookup.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.app.BaseViewModel
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupEvent
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupRepository
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupUiFlow
import org.rainrental.rainrentalrfid.taglookup.domain.DeleteTagUseCase
import org.rainrental.rainrentalrfid.taglookup.domain.ScanTagAndLookupUseCase
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.WriteEpcUseCase
import javax.inject.Inject

@HiltViewModel
class TagLookupViewModel @Inject constructor(
    private val tagLookupRepository: TagLookupRepository,
    private val scanTagAndLookupUseCase: ScanTagAndLookupUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val writeEpcUseCase: WriteEpcUseCase,
    dependencies: BaseViewModelDependencies,
) : BaseViewModel(dependencies) {

    val uiFlow: StateFlow<TagLookupUiFlow> = tagLookupRepository.uiFlow

    val uiState = tagLookupRepository.uiState

    fun onEvent(event: TagLookupEvent) {
        when (event) {
            TagLookupEvent.OnTriggerUp -> {
                when (uiFlow.value) {
                    TagLookupUiFlow.WaitingForTag -> {
                        viewModelScope.launch { scanTagAndLookupUseCase() }
                    }
                    TagLookupUiFlow.ScanningTag -> {
                        // Already scanning, ignore
                    }
                    is TagLookupUiFlow.LookingUpAsset -> {
                        // Already looking up, ignore
                    }
                    is TagLookupUiFlow.AssetFound -> {
                        // Reset to waiting state for next scan
                        viewModelScope.launch { 
                            tagLookupRepository.updateUiFlow(TagLookupUiFlow.WaitingForTag)
                        }
                    }
                    is TagLookupUiFlow.AssetNotFound -> {
                        // Reset to waiting state for next scan
                        viewModelScope.launch { 
                            tagLookupRepository.updateUiFlow(TagLookupUiFlow.WaitingForTag)
                        }
                    }
                    is TagLookupUiFlow.TagDeleted -> {
                        // Reset to waiting state for next scan
                        viewModelScope.launch { 
                            tagLookupRepository.updateUiFlow(TagLookupUiFlow.WaitingForTag)
                        }
                    }
                    is TagLookupUiFlow.ClearingEpc -> {
                        // User pressed trigger to clear EPC, start the deletion process
                        viewModelScope.launch {
                            val currentFlow = uiFlow.value as TagLookupUiFlow.ClearingEpc
                            
                            // First, try to clear the EPC
                            when (val epcResult = writeEpcUseCase(tid = currentFlow.tidHex, epc = currentFlow.tidHex)) {
                                is org.rainrental.rainrentalrfid.result.Result.Success -> {
                                    // EPC cleared successfully, show confirmation and proceed to delete
                                    tagLookupRepository.updateUiFlow(
                                        TagLookupUiFlow.EpcCleared(
                                            tidHex = currentFlow.tidHex,
                                            scannedEpc = currentFlow.scannedEpc
                                        )
                                    )
                                }
                                is org.rainrental.rainrentalrfid.result.Result.Error -> {
                                    // EPC clear failed, show error with options
                                    tagLookupRepository.updateUiFlow(
                                        TagLookupUiFlow.EpcClearFailed(
                                            tidHex = currentFlow.tidHex,
                                            scannedEpc = currentFlow.scannedEpc,
                                            error = epcResult.error.name
                                        )
                                    )
                                }
                            }
                        }
                    }
                    is TagLookupUiFlow.EpcCleared -> {
                        // EPC cleared successfully, automatically proceed to delete from backend
                        viewModelScope.launch {
                            val currentFlow = uiFlow.value as TagLookupUiFlow.EpcCleared
                            
                            // Show deleting state
                            tagLookupRepository.updateUiFlow(
                                TagLookupUiFlow.DeletingTag(
                                    tidHex = currentFlow.tidHex,
                                    scannedEpc = currentFlow.scannedEpc
                                )
                            )
                            
                            // Delete from backend
                            when (val result = deleteTagUseCase.deleteFromBackendOnly(currentFlow.tidHex)) {
                                is org.rainrental.rainrentalrfid.result.Result.Success -> {
                                    tagLookupRepository.updateUiFlow(
                                        TagLookupUiFlow.TagDeletedSuccessfully(
                                            tidHex = currentFlow.tidHex,
                                            scannedEpc = currentFlow.scannedEpc
                                        )
                                    )
                                }
                                is org.rainrental.rainrentalrfid.result.Result.Error -> {
                                    tagLookupRepository.updateUiFlow(
                                        TagLookupUiFlow.DeleteFailed(
                                            tidHex = currentFlow.tidHex,
                                            scannedEpc = currentFlow.scannedEpc,
                                            error = result.error.name
                                        )
                                    )
                                }
                            }
                        }
                    }
                    is TagLookupUiFlow.DeletingTag -> {
                        // Already in deleting state, do nothing
                    }
                    is TagLookupUiFlow.TagDeletedSuccessfully -> {
                        // Success state, do nothing
                    }
                    is TagLookupUiFlow.EpcClearFailed -> {
                        // Error state, do nothing
                    }
                    is TagLookupUiFlow.DeleteFailed -> {
                        // Error state, do nothing
                    }
                }
            }
            TagLookupEvent.OnSideKeyUp -> {
                // Same as trigger up
                onEvent(TagLookupEvent.OnTriggerUp)
            }
            is TagLookupEvent.DeleteTag -> {
                // Show confirmation dialog - this will be handled by the UI
                // The UI will call ConfirmDeleteTag or CancelDeleteTag based on user choice
            }
            is TagLookupEvent.ConfirmDeleteTag -> {
                // User confirmed deletion, show EPC clearing instructions
                val currentFlow = uiFlow.value
                if (currentFlow is TagLookupUiFlow.AssetFound) {
                    viewModelScope.launch {
                        tagLookupRepository.updateUiFlow(
                            TagLookupUiFlow.ClearingEpc(
                                tidHex = currentFlow.tidHex,
                                scannedEpc = currentFlow.scannedEpc
                            )
                        )
                    }
                }
            }
            TagLookupEvent.CancelDeleteTag -> {
                // User cancelled deletion, stay on current screen
                // No action needed - UI will handle dismissing the dialog
            }
            TagLookupEvent.RetryEpcClear -> {
                // User wants to retry EPC clearing
                val currentFlow = uiFlow.value
                if (currentFlow is TagLookupUiFlow.EpcClearFailed) {
                    viewModelScope.launch {
                        tagLookupRepository.updateUiFlow(
                            TagLookupUiFlow.ClearingEpc(
                                tidHex = currentFlow.tidHex,
                                scannedEpc = currentFlow.scannedEpc
                            )
                        )
                    }
                }
            }
            TagLookupEvent.DeleteFromBackendOnly -> {
                // User wants to skip EPC clearing and just delete from backend
                val currentFlow = uiFlow.value
                if (currentFlow is TagLookupUiFlow.EpcClearFailed) {
                    viewModelScope.launch {
                        tagLookupRepository.updateUiFlow(
                            TagLookupUiFlow.DeletingTag(
                                tidHex = currentFlow.tidHex,
                                scannedEpc = currentFlow.scannedEpc
                            )
                        )
                        
                        when (val result = deleteTagUseCase.deleteFromBackendOnly(currentFlow.tidHex)) {
                            is org.rainrental.rainrentalrfid.result.Result.Success -> {
                                tagLookupRepository.updateUiFlow(
                                    TagLookupUiFlow.TagDeletedSuccessfully(
                                        tidHex = currentFlow.tidHex,
                                        scannedEpc = currentFlow.scannedEpc
                                    )
                                )
                            }
                            is org.rainrental.rainrentalrfid.result.Result.Error -> {
                                tagLookupRepository.updateUiFlow(
                                    TagLookupUiFlow.DeleteFailed(
                                        tidHex = currentFlow.tidHex,
                                        scannedEpc = currentFlow.scannedEpc,
                                        error = result.error.name
                                    )
                                )
                            }
                        }
                    }
                }
            }
            TagLookupEvent.CancelDeleteProcess -> {
                // User wants to cancel the entire delete process
                viewModelScope.launch {
                    tagLookupRepository.updateUiFlow(TagLookupUiFlow.WaitingForTag)
                }
            }
            TagLookupEvent.ContinueAfterSuccess -> {
                // User acknowledged the success, return to waiting state
                viewModelScope.launch {
                    tagLookupRepository.updateUiFlow(TagLookupUiFlow.WaitingForTag)
                }
            }
        }
    }

    override fun onTriggerUp() {
        onEvent(TagLookupEvent.OnTriggerUp)
    }
    
    override fun onSideKeyUp() {
        onEvent(TagLookupEvent.OnSideKeyUp)
    }
}
