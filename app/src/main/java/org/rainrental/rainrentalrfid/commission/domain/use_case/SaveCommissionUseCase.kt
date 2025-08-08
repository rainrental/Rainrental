package org.rainrental.rainrentalrfid.commission.domain.use_case

import org.rainrental.rainrentalrfid.commission.data.CommissionRepository
import org.rainrental.rainrentalrfid.commission.data.CommissionTagRequestDto
import org.rainrental.rainrentalrfid.commission.data.TagData
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiFlow
import org.rainrental.rainrentalrfid.commission.presentation.model.ScanningTagData
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.shared.domain.use_case.TriggerToastUseCase
import javax.inject.Inject
import javax.inject.Named

class SaveCommissionUseCase @Inject constructor(
    private val commissionRepository: CommissionRepository,
    private val triggerToastUseCase: TriggerToastUseCase,
    @Named("company_id") private val companyId: String,
) {
    suspend operator fun invoke(scannedTags:List<ScanningTagData>,barcode:String){
        val commissionTagRequestDto = CommissionTagRequestDto(
            barcode = barcode,
            companyId = companyId,
            tags = scannedTags.map { TagData(tidHex = it.tidHex, epcHex = it.epcHex) }
        )
        commissionRepository.setSaving(true)
        when (val commissionResponse = commissionRepository.commissionTag(commissionTagRequestDto)){
            is Result.Error -> {
                commissionRepository.setSaving(false)
                triggerToastUseCase("Error saving tags: ${commissionResponse.error.name}")
            }
            is Result.Success -> {
                if (!commissionResponse.data.success){
                    commissionRepository.setSaving(false)
                    triggerToastUseCase("Error saving tags: ${commissionResponse.data.message}")
                }else{
                    commissionRepository.setSaving(false)
                    triggerToastUseCase(commissionResponse.data.message)
                    commissionRepository.updateUiFlow(CommissionUiFlow.WaitingForBarcodeInput())
                }
            }
        }
    }
}