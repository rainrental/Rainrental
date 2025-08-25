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
import javax.inject.Inject

@HiltViewModel
class HuntViewModel @Inject constructor(
    private val huntRepository: HuntRepository,
    private val getBarcodeAndLookupAssetUseCase: GetBarcodeAndLookupAssetUseCase,
    private val lookupAssetUseCase: LookupAssetUseCase,
    private val startTagHuntUseCase: StartTagHuntUseCase,
    private val stopHuntUseCase: StopHuntUseCase,
    dependencies: BaseViewModelDependencies,
) : BaseViewModel(dependencies) {



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
        onEvent(HuntEvent.OnKeyUp)
    }
    
    override fun onSideKeyUp() {
        onEvent(HuntEvent.OnKeyUp)
    }

}