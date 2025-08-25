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
import org.rainrental.rainrentalrfid.app.BackHandler
import org.rainrental.rainrentalrfid.app.LifecycleAware
import org.rainrental.rainrentalrfid.chainway.data.TagWithOrientation
import org.rainrental.rainrentalrfid.commission.presentation.composable.AssetView
import org.rainrental.rainrentalrfid.hunt.data.HuntEvent
import org.rainrental.rainrentalrfid.hunt.data.HuntFlow
import org.rainrental.rainrentalrfid.hunt.presentation.DBMeter
import org.rainrental.rainrentalrfid.inventory.presentation.HintText
import org.rainrental.rainrentalrfid.shared.presentation.composables.LoadingWithText
import org.rainrental.rainrentalrfid.shared.presentation.composables.InputWithIcon
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto

@Composable
fun HuntScreen() {
    val huntViewModel: HuntViewModel = hiltViewModel()
    val uiFlow by huntViewModel.uiFlow.collectAsState()
    val huntResults by huntViewModel.huntResults.collectAsState()
    
    // Handle back navigation and lifecycle events
    BackHandler {
        huntViewModel.onBackPressed()
    }
    
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
        verticalArrangement = Arrangement.Center
    ) {
        when (uiFlow) {
            is HuntFlow.WaitingForBarcode -> {
                Text(
                    text = "Scan barcode to start hunt",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            HuntFlow.ScanningBarcode -> {
                Text(
                    text = "Scanning barcode...",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            is HuntFlow.LookingUpAsset -> {
                Text(
                    text = "Looking up asset: ${uiFlow.barcode}",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            is HuntFlow.LoadedAsset -> {
                Text(
                    text = "Asset loaded: ${uiFlow.asset.epc}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Press trigger to start hunting",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is HuntFlow.Hunting -> {
                Text(
                    text = "Hunting for: ${uiFlow.asset.epc}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Found ${huntResults.size} tags",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (huntResults.isNotEmpty()) {
                    val lastRssi = huntResults.lastOrNull()?.tag?.rssi?.toDouble() ?: 0.0
                    DBMeter(lastRssi = lastRssi)
                }
            }
            is HuntFlow.FinishedHunting -> {
                Text(
                    text = "Hunt finished",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Found ${huntResults.size} tags",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun LoadedHuntAssetView(asset:AssetDetailsResponseDto) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AssetView(asset)
        HintText(text = "Press trigger to start hunting".uppercase())
    }
}

@Composable
fun HuntingView(
    huntResults: List<TagWithOrientation>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (huntResults.isNotEmpty()){
            Column {
                val lastRssi = huntResults.last().tag.rssi
                DBMeter(
                    lastRssi = lastRssi.toDoubleOrNull()?:Double.NEGATIVE_INFINITY,
                    width = 400.dp,
                    height = 40.dp
                )
            }
            Text(huntResults.size.toString())
            Text(huntResults.last().tag.tid)
            Text(huntResults.last().tag.epc)
            Text("${huntResults.last().tag.rssi} dB")
        }else{
            // This part of the code is no longer directly used in HuntScreen,
            // but keeping it as it was not explicitly removed by the new_code.
            // It might be intended for a different context or removed later.
            // For now, it will be replaced by the new HuntScreen's logic.
        }
    }
}

@Composable
fun FinishedHuntingView(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Finished Hunting View")
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HuntScreenPreview() {
    HuntScreen(
        modifier = Modifier,
        uiFlow = HuntFlow.WaitingForBarcode()
    )
}