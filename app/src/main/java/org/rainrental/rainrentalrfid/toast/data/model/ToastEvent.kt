package org.rainrental.rainrentalrfid.toast.data.model

sealed class ToastEvent {
    data class ShowToast(val message: String) : ToastEvent()
}