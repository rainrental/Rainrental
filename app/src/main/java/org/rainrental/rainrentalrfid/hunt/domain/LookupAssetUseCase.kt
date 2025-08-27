package org.rainrental.rainrentalrfid.hunt.domain

import org.rainrental.rainrentalrfid.hunt.data.HuntFlow
import org.rainrental.rainrentalrfid.hunt.data.HuntRepository
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

class LookupAssetUseCase @Inject constructor(
    private val huntRepository: HuntRepository,
) {
    suspend operator fun invoke(barcode: String) {
        huntRepository.updateUiFlow(HuntFlow.LookingUpAsset(barcode = barcode))
        when (val lookupResult = huntRepository.getAsset(barcode)) {
            is Result.Error -> {
                huntRepository.updateUiFlow(HuntFlow.WaitingForBarcode(withError = lookupResult.error.name))
            }
            is Result.Success -> {
                huntRepository.updateUiFlow(HuntFlow.LoadedAsset(asset = lookupResult.data, withError = null))
            }
        }
    }
}
