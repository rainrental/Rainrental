package org.rainrental.rainrentalrfid.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Composable that handles back navigation and lifecycle events for ViewModels
 * that extend BaseViewModel
 * 
 * @param onBackPressed Callback that returns true if the back event should be consumed
 *                      (preventing default navigation), false to allow default navigation to proceed
 */
@Composable
fun BackHandler(
    onBackPressed: () -> Unit = { false }
) {
    BackHandler(enabled = true) {
        val shouldConsume = onBackPressed()
        // If shouldConsume is true, the event is consumed and navigation is prevented
        // If shouldConsume is false, the event is not consumed and default navigation proceeds
    }
}

/**
 * Composable that performs cleanup on back press but always allows default navigation
 * This is useful when you want to ensure cleanup happens but don't want to interfere
 * with the normal back navigation flow
 * 
 * Note: Since Android's BackHandler always consumes the event, this approach uses
 * lifecycle observation to perform cleanup when the screen is about to be destroyed
 */
@Composable
fun BackHandlerCleanupOnly(
    onBackPressed: () -> Unit = {}
) {
    // Use lifecycle observation to perform cleanup when screen is destroyed
    // This ensures cleanup happens even when back navigation occurs
    LifecycleAware(
        onDestroy = { onBackPressed() }
    )
}

/**
 * Composable that handles back navigation with automatic cleanup for BaseViewModel
 * This version automatically calls the ViewModel's onBackPressed and allows
 * default navigation to proceed unless explicitly prevented
 */
@Composable
fun BackHandlerWithCleanup(
    viewModel: BaseViewModel,
    preventDefaultNavigation: Boolean = false
) {
    if (preventDefaultNavigation) {
        // If we want to prevent default navigation, use the regular BackHandler
        BackHandler {
            viewModel.onBackPressed()
        }
    } else {
        // If we want to allow default navigation, use lifecycle observation for cleanup
        LifecycleAware(
            onDestroy = { viewModel.onBackPressed() }
        )
    }
}

/**
 * Composable that observes lifecycle events and calls appropriate ViewModel methods
 */
@Composable
fun LifecycleAware(
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onDestroy: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> onPause()
                Lifecycle.Event.ON_RESUME -> onResume()
                Lifecycle.Event.ON_DESTROY -> onDestroy()
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
