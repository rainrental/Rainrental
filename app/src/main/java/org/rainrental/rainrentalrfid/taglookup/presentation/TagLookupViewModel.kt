package org.rainrental.rainrentalrfid.taglookup.presentation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.app.BaseViewModel
import org.rainrental.rainrentalrfid.app.BaseViewModelDependencies
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupEvent
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupRepository
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupUiFlow
import org.rainrental.rainrentalrfid.taglookup.domain.ScanTagAndLookupUseCase
import javax.inject.Inject

@HiltViewModel
class TagLookupViewModel @Inject constructor(
    private val tagLookupRepository: TagLookupRepository,
    private val scanTagAndLookupUseCase: ScanTagAndLookupUseCase,
    dependencies: BaseViewModelDependencies,
) : BaseViewModel(dependencies) {

    val uiFlow = tagLookupRepository.uiFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = TagLookupUiFlow.WaitingForTag
    )

    val uiState = tagLookupRepository.uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = tagLookupRepository.uiState.value
    )

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
                }
            }
            TagLookupEvent.OnSideKeyUp -> {
                // Same as trigger up
                onEvent(TagLookupEvent.OnTriggerUp)
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
