package org.rainrental.rainrentalrfid.hunt.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.chainway.data.TagWithOrientation
import org.rainrental.rainrentalrfid.commission.presentation.composable.AssetView
import org.rainrental.rainrentalrfid.hunt.data.HuntFlow
import org.rainrental.rainrentalrfid.inventory.presentation.HintText
import org.rainrental.rainrentalrfid.shared.presentation.composables.LoadingWithText
import org.rainrental.rainrentalrfid.shared.presentation.composables.InputWithIcon
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto

@Composable
fun HuntScreen() {
    val huntViewModel : HuntViewModel = hiltViewModel()
    val uiFlow by huntViewModel.uiFlow.collectAsState()
    val huntResults by huntViewModel.huntResults.collectAsState()
    HuntScreen(
        uiFlow = uiFlow,
        huntResults = huntResults
    )
}

@Composable
fun HuntScreen(uiFlow:HuntFlow, huntResults: List<TagWithOrientation>) {
    when (uiFlow){
        is HuntFlow.WaitingForBarcode -> InputWithIcon(text = "Scan a barcode to lookup asset EPC", withError = uiFlow.withError ,withResourceIcon = R.drawable.barcode)
        HuntFlow.ScanningBarcode -> LoadingWithText(text = "Scanning barcode")
        is HuntFlow.LookingUpAsset -> LoadingWithText(text = "Looking up ${uiFlow.barcode}")
        is HuntFlow.LoadedAsset -> LoadedHuntAssetView(asset = uiFlow.asset)
        is HuntFlow.Hunting -> HuntingView(huntResults)
        is HuntFlow.FinishedHunting -> FinishedHuntingView()
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
            CircularProgressIndicator(modifier = Modifier.size(40.dp))
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
        uiFlow = HuntFlow.LoadedAsset(asset = AssetDetailsResponseDto.example()),
        huntResults = emptyList()
    )
}