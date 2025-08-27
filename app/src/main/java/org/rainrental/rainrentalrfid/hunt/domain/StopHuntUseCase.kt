package org.rainrental.rainrentalrfid.hunt.domain

import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.hunt.data.HuntFlow
import org.rainrental.rainrentalrfid.hunt.data.HuntRepository
import org.rainrental.rainrentalrfid.shared.domain.use_case.TriggerToastUseCase
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto
import javax.inject.Inject

class StopHuntUseCase @Inject constructor(
    private val rfidManager: RfidManager,
    private val huntRepository: HuntRepository,
    private val toastUseCase: TriggerToastUseCase,
) {
    suspend operator fun invoke(asset:AssetDetailsResponseDto){
        when (rfidManager.stopTagHunt()){
            false -> toastUseCase("Error stopping rfid")
            true -> {
                // Get the current hunt results count before stopping
                val huntResultsCount = rfidManager.huntResults.value.size
                huntRepository.updateUiFlow(HuntFlow.WaitingForBarcode(previousHuntResults = huntResultsCount))
            }
        }
    }
}