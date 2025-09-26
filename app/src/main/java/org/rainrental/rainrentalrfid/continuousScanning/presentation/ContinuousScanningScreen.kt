package org.rainrental.rainrentalrfid.continuousScanning.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
import org.rainrental.rainrentalrfid.continuousScanning.presentation.RfidScanningAnimation

@Composable
fun ContinuousScanningScreen() {
    val continuousScanningViewModel: ContinuousScanningViewModel = hiltViewModel()
    val state by continuousScanningViewModel.hardwareState.collectAsState()
    val continuousScanningState by continuousScanningViewModel.continuousScanningState.collectAsState()
    val deliveryState by continuousScanningViewModel.deliverState.collectAsState()
    val currentServer by continuousScanningViewModel.currentServer.collectAsState()
    val currentEpcFilter by continuousScanningViewModel.currentEpcFilter.collectAsState()
    val currentRainCompanyId by continuousScanningViewModel.currentRainCompanyId.collectAsState()
    val epcFilterEnabled by continuousScanningViewModel.epcFilterEnabled.collectAsState()
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
        currentEpcFilter = currentEpcFilter,
        rainCompanyId = currentRainCompanyId,
        epcFilterEnabled = epcFilterEnabled,
        onStatusClick = {
            coroutineScope.launch {
                continuousScanningViewModel.restartMqttConnection()
            }
        },
        onToggleFilter = {
            continuousScanningViewModel.toggleEpcFilter()
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
    currentEpcFilter: String = "",
    rainCompanyId: Int = 0,
    epcFilterEnabled: Boolean = true,
    onStatusClick: () -> Unit = {},
    onToggleFilter: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        // Show "Not connected" message when delivery server is not connected
        AnimatedVisibility(
            state == RfidHardwareState.Ready && deliveryState != DeliveryConnectionState.CONNECTED,
            enter = fadeIn(), 
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(), 
                horizontalAlignment = Alignment.CenterHorizontally, 
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Not connected to delivery server",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Press left side button for barcode check-in", 
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Show normal ready state when connected to delivery server
        AnimatedVisibility(
            state == RfidHardwareState.Ready && deliveryState == DeliveryConnectionState.CONNECTED,
            enter = fadeIn(), 
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Press and hold trigger for RFID scanning")
                Text("Press left side button for barcode check-in", style = MaterialTheme.typography.bodySmall)
            }
        }
        AnimatedVisibility(state == RfidHardwareState.Scanning,enter = fadeIn(), exit = fadeOut()) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                RfidScanningAnimation(
                    text = continuousScanningState.uniqueCount.toString(),
                    rssi = continuousScanningState.lastRssi,
                    completion = 0f,
                    tid = continuousScanningState.lastTagEvent?.tid
                )
            }
        }
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
//            contentAlignment = Alignment.BottomCenter
//        ){
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                // Last scanned tag information
//                continuousScanningState.lastTagEvent?.let { lastTag ->
//                    Text(
//                        text = "Last Tag TID: ${lastTag.tid}",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.tertiary,
//                        textDecoration = TextDecoration.Underline,
//                        textAlign = TextAlign.Center,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    Text(
//                        text = "Last Tag EPC: ${lastTag.epc}",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.tertiary,
//                        textDecoration = TextDecoration.Underline,
//                        textAlign = TextAlign.Center,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//
//                Text(
//                    text = "EPC Filter: ${if (epcFilterEnabled) currentEpcFilter else "DISABLED"}",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = if (epcFilterEnabled) MaterialTheme.colorScheme.secondary else Color.Red,
//                    textDecoration = TextDecoration.Underline,
//                    textAlign = TextAlign.Center,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ContinuousScanningScreenPreviewStopped() {
    ContinuousScanningScreen(
        modifier = Modifier,
        state = RfidHardwareState.Ready, 
        continuousScanningState = ContinuousScanningState(),
        currentEpcFilter = "11110000000000001100",
        rainCompanyId = 12,
        epcFilterEnabled = true
    )
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ContinuousScanningScreenPreview() {
    ContinuousScanningScreen(
        modifier = Modifier,
        state = RfidHardwareState.Scanning, 
        continuousScanningState = ContinuousScanningState(),
        currentEpcFilter = "11110000000000001100",
        rainCompanyId = 12,
        epcFilterEnabled = true
    )
} 