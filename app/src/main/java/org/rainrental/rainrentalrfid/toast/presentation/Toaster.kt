package org.rainrental.rainrentalrfid.toast.presentation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.toast.data.model.ToastEvent

@Composable
fun Toaster(
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val toastViewModel : ToastViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        toastViewModel.toastEvent.collect { toastEvent ->
            when (toastEvent) {
                is ToastEvent.ShowToast -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = toastEvent.message,
                            duration = SnackbarDuration.Short,
                            actionLabel = "Dismiss",
                        )
                    }
                }
            }
        }
    }
}
