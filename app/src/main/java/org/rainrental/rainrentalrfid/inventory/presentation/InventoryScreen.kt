package org.rainrental.rainrentalrfid.inventory.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.app.BackHandler
import org.rainrental.rainrentalrfid.app.LifecycleAware
import org.rainrental.rainrentalrfid.commission.presentation.composable.AssetView
import org.rainrental.rainrentalrfid.inventory.data.InventoryEvent
import org.rainrental.rainrentalrfid.inventory.data.InventoryFlow
import org.rainrental.rainrentalrfid.shared.presentation.composables.AppButton
import org.rainrental.rainrentalrfid.shared.presentation.composables.LoadingWithText
import org.rainrental.rainrentalrfid.shared.presentation.composables.InputWithIcon
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto

@Composable
fun InventoryScreen() {
    val inventoryViewModel: InventoryViewModel = hiltViewModel()
    val uiFlow by inventoryViewModel.uiFlow.collectAsState()
    val inventory by inventoryViewModel.inventory.collectAsState()
    val saving by inventoryViewModel.saving.collectAsState()
    val isInventoryEmpty by inventoryViewModel.isInventoryEmpty.collectAsState()
    
    // Handle back navigation and lifecycle events
    BackHandler {
        inventoryViewModel.onBackPressed()
    }
    
    LifecycleAware(
        onPause = { inventoryViewModel.onScreenPaused() },
        onResume = { inventoryViewModel.onScreenResumed() },
        onDestroy = { inventoryViewModel.onBackPressed() }
    )
    
    InventoryScreen(
        modifier = Modifier,
        uiFlow = uiFlow,
        inventory = inventory,
        saving = saving,
        isInventoryEmpty = isInventoryEmpty,
        onEvent = inventoryViewModel::onEvent
    )
}

@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier,
    uiFlow: InventoryFlow,
    inventory: Int = 0,
    saving: Boolean = true,
    isInventoryEmpty: Boolean = false,
    onEvent: (InventoryEvent) -> Unit
) {
    when(uiFlow){
        is InventoryFlow.WaitingForBarcode -> WaitingForBarcodeView(uiFlow = uiFlow)
        is InventoryFlow.LookingUpAsset -> LookingUpAssetView(uiFlow = uiFlow)
        is InventoryFlow.ReadyToCount -> ReadyToCountView(uiFlow = uiFlow, isInventoryEmpty = isInventoryEmpty, onEvent = onEvent)
        is InventoryFlow.Counting -> CountingView(uiFlow = uiFlow, inventory = inventory, onEvent = onEvent)
        is InventoryFlow.FinishedCounting -> FinishedCountingView(uiFlow = uiFlow, isInventoryEmpty = isInventoryEmpty, onEvent = onEvent)
    }

    AnimatedVisibility(saving, enter = fadeIn(), exit = fadeOut()) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            LoadingWithText(text = "Saving")
        }
    }
}

@Composable
fun WaitingForBarcodeView(
    modifier: Modifier = Modifier,
    uiFlow: InventoryFlow.WaitingForBarcode
) {
    InputWithIcon(text = "Scan a barcode to lookup asset sku".uppercase(), withResourceIcon = R.drawable.barcode, withError = uiFlow.withError)
}

@Composable
fun LookingUpAssetView(modifier: Modifier = Modifier, uiFlow: InventoryFlow.LookingUpAsset) {
    LoadingWithText(text = "Looking up ${uiFlow.barcode}")
}

@Composable
fun ReadyToCountView(modifier: Modifier = Modifier, uiFlow: InventoryFlow.ReadyToCount, isInventoryEmpty: Boolean = false, onEvent: (InventoryEvent) -> Unit) {
    InventoryAssetView(asset = uiFlow.asset, uiFlow = uiFlow, isInventoryEmpty = isInventoryEmpty, onEvent = onEvent)
}


@Composable
fun CountingView(modifier: Modifier = Modifier, uiFlow: InventoryFlow.Counting, inventory: Int = 0, isInventoryEmpty: Boolean = false, onEvent: (InventoryEvent) -> Unit) {
            InventoryAssetView(asset = uiFlow.asset, uiFlow = uiFlow, inventory = inventory, isInventoryEmpty = isInventoryEmpty, onEvent = onEvent)
}

@Composable
fun FinishedCountingView(modifier: Modifier = Modifier, uiFlow: InventoryFlow.FinishedCounting, isInventoryEmpty: Boolean = false, onEvent: (InventoryEvent) -> Unit) {

    InventoryAssetView(asset = uiFlow.asset, uiFlow = uiFlow, isInventoryEmpty = isInventoryEmpty, onEvent = onEvent)

}



@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InventoryScreenPreview() {
    InventoryScreen(
        modifier = Modifier,
        uiFlow = InventoryFlow.WaitingForBarcode(withError = "There was an error"),
        onEvent = {}
    )
}
@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InventoryScreenPreviewLoadedAsset() {
    InventoryScreen(
        modifier = Modifier,
        uiFlow = InventoryFlow.ReadyToCount(asset = AssetDetailsResponseDto.example()),
        onEvent = {}
    )
}

@Composable
fun InventoryAssetView(modifier: Modifier = Modifier, asset:AssetDetailsResponseDto, uiFlow: InventoryFlow, inventory: Int = 0, isInventoryEmpty: Boolean = false, onEvent: (InventoryEvent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Loaded SKU ${asset.sku}".uppercase(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 16.dp))
        AssetView(asset = asset, general = true)

        when (uiFlow){
            is InventoryFlow.ReadyToCount -> {
                uiFlow.withError?.let{
                    Text(uiFlow.withError, color = MaterialTheme.colorScheme.error)
                }
                HintText(text = "Press trigger to start count", modifier = Modifier.padding(vertical = 32.dp))
            }
            is InventoryFlow.Counting -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                    Text(inventory.toString())
                    HintText(text = "Press trigger to stop count", modifier = Modifier.padding(vertical = 32.dp))
                }
            }

            is InventoryFlow.FinishedCounting -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Counted ${uiFlow.count} assets")
                    if (isInventoryEmpty) {
                        AppButton(text = "Finish".uppercase()) { onEvent(InventoryEvent.Finish) }
                    } else {
                        AppButton(text = "Save and finish".uppercase()) { onEvent(InventoryEvent.Save) }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun HintText(
    modifier: Modifier = Modifier,
    text:String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
        ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = color
        )
        Text(text = text.uppercase(), modifier = Modifier.padding(16.dp),color = color)
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = color
        )
    }
}
