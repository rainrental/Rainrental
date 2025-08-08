package org.rainrental.rainrentalrfid.hunt.domain

import org.rainrental.rainrentalrfid.chainway.data.RfidManager
import org.rainrental.rainrentalrfid.hunt.data.HuntFlow
import org.rainrental.rainrentalrfid.hunt.data.HuntRepository
import org.rainrental.rainrentalrfid.shared.domain.use_case.TriggerToastUseCase
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto
import javax.inject.Inject

class StartTagHuntUseCase @Inject constructor(
    private val rfidManager: RfidManager,
    private val huntRepository: HuntRepository,
    private val toastUseCase: TriggerToastUseCase,
) {
    suspend operator fun invoke(asset:AssetDetailsResponseDto){
        when (rfidManager.startTagHunt(asset.epc)){
            false -> toastUseCase("Error starting hunt")
            true -> {
                huntRepository.updateUiFlow(HuntFlow.Hunting(asset = asset))
            }
        }
    }
}