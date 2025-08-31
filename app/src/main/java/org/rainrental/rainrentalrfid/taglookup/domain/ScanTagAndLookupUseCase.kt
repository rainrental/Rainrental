package org.rainrental.rainrentalrfid.taglookup.domain

import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.GetSingleRfidTagUseCase
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupRepository
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupUiFlow
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

class ScanTagAndLookupUseCase @Inject constructor(
    private val getSingleRfidTagUseCase: GetSingleRfidTagUseCase,
    private val tagLookupRepository: TagLookupRepository
) {
    suspend operator fun invoke() {
        tagLookupRepository.updateUiFlow(TagLookupUiFlow.ScanningTag)
        
        when (val tagResult = getSingleRfidTagUseCase()) {
            is Result.Error -> {
                tagLookupRepository.updateUiFlow(
                    TagLookupUiFlow.AssetNotFound(
                        tidHex = "",
                        withError = "Failed to scan tag: ${tagResult.error.name}"
                    )
                )
            }
            is Result.Success -> {
                val tidHex = tagResult.data.tid
                tagLookupRepository.updateUiFlow(TagLookupUiFlow.LookingUpAsset(tidHex = tidHex))
                
                when (val assetResult = tagLookupRepository.getAssetByTid(tidHex)) {
                    is Result.Error -> {
                        tagLookupRepository.updateUiFlow(
                            TagLookupUiFlow.AssetNotFound(
                                tidHex = tidHex,
                                withError = "Failed to lookup asset: ${assetResult.error.name}"
                            )
                        )
                    }
                    is Result.Success -> {
                        tagLookupRepository.updateUiFlow(
                            TagLookupUiFlow.AssetFound(
                                asset = assetResult.data,
                                tidHex = tidHex
                            )
                        )
                    }
                }
            }
        }
    }
}
