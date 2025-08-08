package org.rainrental.rainrentalrfid.shared.domain.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.rainrental.rainrentalrfid.toast.data.repository.ToastRepository

import javax.inject.Inject

class TriggerToastUseCase @Inject constructor(
    private val toastRepository: ToastRepository,
) {
    suspend operator fun invoke(message:String){
        withContext(Dispatchers.Main) {
            launch {
                toastRepository.sendToast(message)
            }
        }
    }
}