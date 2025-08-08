package org.rainrental.rainrentalrfid.hunt.domain


import org.rainrental.rainrentalrfid.hunt.data.HuntFlow
import org.rainrental.rainrentalrfid.hunt.data.HuntRepository
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.ScanBarcodeUseCase
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

class GetBarcodeAndLookupAssetUseCase @Inject constructor(
    private val huntRepository: HuntRepository,
    private val scanBarcodeUseCase: ScanBarcodeUseCase,
)  {
    suspend operator fun invoke(){
        when (val barcode = scanBarcodeUseCase()){
            is Result.Error -> {
                huntRepository.updateUiFlow(HuntFlow.WaitingForBarcode(withError = barcode.error.name))
            }
            is Result.Success -> {
                huntRepository.updateUiFlow(HuntFlow.LookingUpAsset(barcode = barcode.data))
                when (val lookupResult = huntRepository.getAsset(barcode.data)){
                    is Result.Error -> {
                        huntRepository.updateUiFlow(HuntFlow.WaitingForBarcode(withError = lookupResult.error.name))
                    }
                    is Result.Success -> {
                        huntRepository.updateUiFlow(HuntFlow.LoadedAsset(asset = lookupResult.data, withError = null))
                    }
                }
            }
        }
    }
}