package org.rainrental.rainrentalrfid.commission.domain.use_case

import org.rainrental.rainrentalrfid.commission.data.CommissionRepository
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionEvent
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiFlow
import org.rainrental.rainrentalrfid.commission.presentation.model.ScanningTagData
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.WriteEpcUseCase
import org.rainrental.rainrentalrfid.result.Result
import org.rainrental.rainrentalrfid.shared.domain.use_case.TriggerToastUseCase
import javax.inject.Inject

class EncodeRequestUseCase @Inject constructor(
    private val commissionRepository: CommissionRepository,
    private val writeEpcUseCase: WriteEpcUseCase,
    private val triggerToastUseCase: TriggerToastUseCase,
) {
    suspend operator fun invoke(event:CommissionEvent.EncodeEpcButtonPressed,oldFlow: CommissionUiFlow.LoadedAsset){

            if (event.scannedTagData.epcData !== null){
                when (val writeOutcome = writeEpcUseCase(tid = event.scannedTagData.tidHex, epc = event.scannedTagData.epcData)){
                    is Result.Error -> {
                        commissionRepository.updateUiFlow(CommissionUiFlow.LoadedAsset(asset = oldFlow.asset, scannedTags = oldFlow.scannedTags, withError = "Error encoding EPC: ${writeOutcome.error.name}"))
                    }
                    is Result.Success -> {
                        val newTags = oldFlow.scannedTags.filter { it.tidHex != event.scannedTagData.tidHex } + ScanningTagData(tidHex = event.scannedTagData.tidHex, epcHex = event.scannedTagData.epcData, epcData = event.scannedTagData.epcData, writtenEpc = true)

                        commissionRepository.updateUiFlow(CommissionUiFlow.LoadedAsset(asset = oldFlow.asset, scannedTags = newTags, withError = null))
                    }
                }
            }else{
                triggerToastUseCase("Tag did not have EPC Data supplied")
            }

    }
}