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
 */
@Composable
fun BackHandler(
    onBackPressed: () -> Unit = {}
) {
    BackHandler {
        onBackPressed()
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
