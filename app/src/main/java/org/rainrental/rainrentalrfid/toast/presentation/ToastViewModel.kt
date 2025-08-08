package org.rainrental.rainrentalrfid.toast.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.toast.data.repository.ToastRepository
import javax.inject.Inject

@HiltViewModel
class ToastViewModel @Inject constructor(
    private val toastRepository: ToastRepository
) : ViewModel(), Logger {

    val toastEvent = toastRepository.toastEvent
}