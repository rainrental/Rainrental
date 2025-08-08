package org.rainrental.rainrentalrfid.toast.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.rainrental.rainrentalrfid.toast.data.model.ToastEvent
import javax.inject.Inject

interface ToastRepository {
    val toastEvent : SharedFlow<ToastEvent>
    suspend fun sendToast(text:String)
}

class DefaultToastRepository @Inject constructor(

): ToastRepository {
    private val _toastEvent = MutableSharedFlow<ToastEvent>()
    override val toastEvent = _toastEvent.asSharedFlow()
    override suspend fun sendToast(text: String) {
        _toastEvent.emit(ToastEvent.ShowToast(text))
    }
}

