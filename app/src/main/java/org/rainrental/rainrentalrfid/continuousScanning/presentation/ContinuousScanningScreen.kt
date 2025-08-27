package org.rainrental.rainrentalrfid.continuousScanning.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.rainrental.rainrentalrfid.app.BackHandler
import org.rainrental.rainrentalrfid.app.BackHandlerWithCleanup
import org.rainrental.rainrentalrfid.app.LifecycleAware
import org.rainrental.rainrentalrfid.chainway.data.RfidHardwareState
import org.rainrental.rainrentalrfid.continuousScanning.data.DeliveryConnectionState
import org.rainrental.rainrentalrfid.continuousScanning.data.ContinuousScanningState

@Composable
fun ContinuousScanningScreen() {
    val continuousScanningViewModel: ContinuousScanningViewModel = hiltViewModel()
    val state by continuousScanningViewModel.hardwareState.collectAsState()
    val continuousScanningState by continuousScanningViewModel.continuousScanningState.collectAsState()
    val deliveryState by continuousScanningViewModel.deliverState.collectAsState()
    val currentServer by continuousScanningViewModel.currentServer.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Handle back navigation and lifecycle events
    BackHandlerWithCleanup(continuousScanningViewModel,preventDefaultNavigation = false)
    
    LifecycleAware(
        onPause = { continuousScanningViewModel.onScreenPaused() },
        onResume = { continuousScanningViewModel.onScreenResumed() },
        onDestroy = { continuousScanningViewModel.onBackPressed() }
    )
    
    ContinuousScanningScreen(
        modifier = Modifier,
        state = state,
        continuousScanningState = continuousScanningState,
        deliveryState = deliveryState, 
        currentServer = currentServer,
        onStatusClick = {
            coroutineScope.launch {
                continuousScanningViewModel.restartMqttConnection()
            }
        }
    )
}

@Composable
fun ContinuousScanningScreen(
    modifier: Modifier = Modifier,
    state: RfidHardwareState,
    continuousScanningState: ContinuousScanningState = ContinuousScanningState(),
    deliveryState: DeliveryConnectionState = DeliveryConnectionState.DEAD,
    currentServer: String? = "",
    onStatusClick: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        AnimatedVisibility(state == RfidHardwareState.Ready) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Press and hold trigger to scan")
            }
        }
        AnimatedVisibility(state == RfidHardwareState.Scanning) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                PulsatingCircles(text = continuousScanningState.uniqueCount.toString(), rssi = 0.0, completion = 0f)
            }
        }
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 16.dp), contentAlignment = Alignment.BottomCenter){
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Text(
                    text = "Delivery State: ${deliveryState.name}", 
                    style = MaterialTheme.typography.labelSmall,
                    color = if (deliveryState == DeliveryConnectionState.CONNECTED) Color.Green else Color.Red,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onStatusClick() }
                )
                Text(
                    text = "Current Server: $currentServer", 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onStatusClick() }
                )
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ContinuousScanningScreenPreviewStopped() {
    ContinuousScanningScreen(
        modifier = Modifier,
        state = RfidHardwareState.Ready, 
        continuousScanningState = ContinuousScanningState()
    )
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ContinuousScanningScreenPreview() {
    ContinuousScanningScreen(
        modifier = Modifier,
        state = RfidHardwareState.Scanning, 
        continuousScanningState = ContinuousScanningState()
    )
} 