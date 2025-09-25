package org.rainrental.rainrentalrfid.taglookup.domain

import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.GetSingleRfidTagForLookupUseCase
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupRepository
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupUiFlow
import org.rainrental.rainrentalrfid.result.Result
import javax.inject.Inject

class ScanTagAndLookupUseCase @Inject constructor(
    private val getSingleRfidTagForLookupUseCase: GetSingleRfidTagForLookupUseCase,
    private val tagLookupRepository: TagLookupRepository
) {
    suspend operator fun invoke() {
        tagLookupRepository.updateUiFlow(TagLookupUiFlow.ScanningTag)
        
        when (val tagResult = getSingleRfidTagForLookupUseCase()) {
            is Result.Error -> {
                tagLookupRepository.updateUiFlow(
                    TagLookupUiFlow.AssetNotFound(
                        tidHex = "",
                        scannedEpc = "",
                        withError = "Failed to scan tag: ${tagResult.error.name}"
                    )
                )
            }
            is Result.Success -> {
                val tidHex = tagResult.data.tid
                val scannedEpc = tagResult.data.epc
                tagLookupRepository.updateUiFlow(TagLookupUiFlow.LookingUpAsset(tidHex = tidHex, scannedEpc = scannedEpc))
                
                when (val assetResult = tagLookupRepository.getAssetByTid(tidHex)) {
                    is Result.Error -> {
                        when (assetResult.error.apiErrorType) {
                            org.rainrental.rainrentalrfid.result.ApiError.TagDeleted -> {
                                // Extract deletedFrom from error response if available
                                val deletedFrom = assetResult.error.errorJson?.get("deletedFrom")?.toString()?.trim('"')
                                tagLookupRepository.updateUiFlow(
                                    TagLookupUiFlow.TagDeleted(
                                        tidHex = tidHex,
                                        scannedEpc = scannedEpc,
                                        deletedFrom = deletedFrom
                                    )
                                )
                            }
                            else -> {
                                tagLookupRepository.updateUiFlow(
                                    TagLookupUiFlow.AssetNotFound(
                                        tidHex = tidHex,
                                        scannedEpc = scannedEpc,
                                        withError = "Failed to lookup asset: ${assetResult.error.apiErrorType.name}"
                                    )
                                )
                            }
                        }
                    }
                    is Result.Success -> {
                        tagLookupRepository.updateUiFlow(
                            TagLookupUiFlow.AssetFound(
                                asset = assetResult.data,
                                tidHex = tidHex,
                                scannedEpc = scannedEpc
                            )
                        )
                    }
                }
            }
        }
    }
}
