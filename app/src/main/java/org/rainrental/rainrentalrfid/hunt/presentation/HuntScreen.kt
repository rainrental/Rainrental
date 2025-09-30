package org.rainrental.rainrentalrfid.hunt.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.app.BackHandlerWithCleanup
import org.rainrental.rainrentalrfid.app.LifecycleAware
import org.rainrental.rainrentalrfid.chainway.data.TagWithOrientation
import org.rainrental.rainrentalrfid.commission.presentation.composable.AssetView
import org.rainrental.rainrentalrfid.hunt.data.HuntEvent
import org.rainrental.rainrentalrfid.hunt.data.HuntFlow
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme

@Composable
fun HuntScreen() {
    val huntViewModel: HuntViewModel = hiltViewModel()
    val uiFlow by huntViewModel.uiFlow.collectAsState()
    val huntResults by huntViewModel.huntResults.collectAsState()
    
    // Handle back navigation and lifecycle events
    BackHandlerWithCleanup(huntViewModel,preventDefaultNavigation = false)
    
    LifecycleAware(
        onPause = { huntViewModel.onScreenPaused() },
        onResume = { huntViewModel.onScreenResumed() },
        onDestroy = { huntViewModel.onBackPressed() }
    )
    
    HuntScreen(
        modifier = Modifier,
        uiFlow = uiFlow,
        huntResults = huntResults,
        onEvent = huntViewModel::onEvent
    )
}

@Composable
fun HuntScreen(
    modifier: Modifier = Modifier,
    uiFlow: HuntFlow,
    huntResults: List<TagWithOrientation> = emptyList(),
    onEvent: (HuntEvent) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (uiFlow) {
            is HuntFlow.WaitingForBarcode -> {
                Text(
                    text = "Scan barcode to start hunt",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                // Show previous hunt results if available
                uiFlow.previousHuntResults?.let { results ->
                    Text(
                        text = "Detected $results times",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Show error if present
                uiFlow.withError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                ManualBarcodeEntry(
                    onLookup = { barcode ->
                        onEvent(HuntEvent.OnManualBarcodeEntry(barcode))
                    }
                )
            }
            HuntFlow.ScanningBarcode -> {
                Text(
                    text = "Scanning barcode...",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            is HuntFlow.LookingUpAsset -> {
                Text(
                    text = "Looking up asset: ${uiFlow.barcode}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            is HuntFlow.LoadedAsset -> {
                AssetView(asset = uiFlow.asset)
                Text(
                    text = "Press trigger to start hunting",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is HuntFlow.Hunting -> {
                Text(
                    text = "Hunting for: ${uiFlow.asset.barcode}",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (huntResults.isNotEmpty()) {
                    val lastRssi = huntResults.lastOrNull()?.tag?.rssi?.toDouble() ?: 0.0
                    DBMeter(lastRssi = lastRssi, width = 440.dp, height = 80.dp)
                }
            }

        }
    }
}



@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HuntScreenPreview() {
    RainRentalRfidTheme {

        HuntScreen(
            modifier = Modifier,
            uiFlow = HuntFlow.WaitingForBarcode()
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HuntScreenPreviewWithResults() {
    RainRentalRfidTheme {
        HuntScreen(
            modifier = Modifier,
            uiFlow = HuntFlow.WaitingForBarcode(previousHuntResults = 5)
        )
    }
}