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
import org.rainrental.rainrentalrfid.navigation.BackConfirmableFeature
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

@HiltViewModel
class TagLookupViewModel @Inject constructor(
    private val tagLookupRepository: TagLookupRepository,
    private val scanTagAndLookupUseCase: ScanTagAndLookupUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val writeEpcUseCase: WriteEpcUseCase,
    dependencies: BaseViewModelDependencies,
) : BaseViewModel(dependencies), Logger, BackConfirmableFeature {

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

    // BackConfirmableFeature implementation
    override fun hasUnsavedChanges(): Boolean {
        val currentFlow = uiFlow.value
        val hasChanges = when (currentFlow) {
            is TagLookupUiFlow.AssetFound,
            is TagLookupUiFlow.AssetNotFound,
            is TagLookupUiFlow.TagDeleted,
            is TagLookupUiFlow.ClearingEpc,
            is TagLookupUiFlow.EpcCleared,
            is TagLookupUiFlow.DeletingTag,
            is TagLookupUiFlow.TagDeletedSuccessfully,
            is TagLookupUiFlow.EpcClearFailed,
            is TagLookupUiFlow.DeleteFailed -> true
            else -> false
        }
        logd("🔥 TagLookupViewModel: hasUnsavedChanges() called - current state: $currentFlow, hasChanges: $hasChanges")
        return hasChanges
    }

    override fun resetState() {
        viewModelScope.launch {
            logd("🔥 TagLookupViewModel: resetState() called - current state: ${uiFlow.value}")
            // Reset to initial state
            tagLookupRepository.updateUiFlow(TagLookupUiFlow.WaitingForTag)
            logd("🔥 TagLookupViewModel: resetState() completed - new state: ${uiFlow.value}")
        }
    }

    override fun getUnsavedChangesDescription(): String {
        val currentFlow = uiFlow.value
        return when (currentFlow) {
            is TagLookupUiFlow.AssetFound -> "You have a loaded asset that will be lost"
            is TagLookupUiFlow.AssetNotFound -> "You have scanned a tag that will be lost"
            is TagLookupUiFlow.TagDeleted -> "You have viewed a deleted tag that will be lost"
            is TagLookupUiFlow.ClearingEpc -> "You are in the process of clearing EPC memory"
            is TagLookupUiFlow.EpcCleared -> "You have cleared EPC memory and are deleting the tag"
            is TagLookupUiFlow.DeletingTag -> "You are in the process of deleting a tag"
            is TagLookupUiFlow.TagDeletedSuccessfully -> "You have successfully deleted a tag"
            is TagLookupUiFlow.EpcClearFailed -> "You have an EPC clear failure that needs attention"
            is TagLookupUiFlow.DeleteFailed -> "You have a delete failure that needs attention"
            else -> "You have unsaved changes that will be lost"
        }
    }
}
