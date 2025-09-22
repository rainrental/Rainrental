package org.rainrental.rainrentalrfid.inventory.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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
//    BackHandler {
//        inventoryViewModel.onBackPressed()
//    }
    
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
    saving: Boolean = false,
    isInventoryEmpty: Boolean = false,
    onEvent: (InventoryEvent) -> Unit
) {

    when(uiFlow){
        is InventoryFlow.WaitingForBarcode -> {
            org.rainrental.rainrentalrfid.logging.LogUtils.logd("RainRental", "InventoryScreen - Showing WaitingForBarcodeView")
            WaitingForBarcodeView(uiFlow = uiFlow, onEvent = onEvent)
        }
        is InventoryFlow.ManualBarcodeEntry -> ManualBarcodeEntryView(uiFlow = uiFlow, onEvent = onEvent)
        is InventoryFlow.LookingUpAsset -> LookingUpAssetView(uiFlow = uiFlow)
        is InventoryFlow.ReadyToCount -> ReadyToCountView(uiFlow = uiFlow, isInventoryEmpty = isInventoryEmpty, onEvent = onEvent)
        is InventoryFlow.Counting -> CountingView(uiFlow = uiFlow, inventory = inventory, onEvent = onEvent)
        is InventoryFlow.FinishedCounting -> FinishedCountingView(uiFlow = uiFlow, isInventoryEmpty = isInventoryEmpty, onEvent = onEvent)
        is InventoryFlow.InventoryAll -> InventoryAllView(uiFlow = uiFlow, onEvent = onEvent)
        is InventoryFlow.InventoryAllCounting -> InventoryAllCountingView(uiFlow = uiFlow, inventory = inventory, onEvent = onEvent)
        is InventoryFlow.InventoryAllFinished -> InventoryAllFinishedView(uiFlow = uiFlow, isInventoryEmpty = isInventoryEmpty, onEvent = onEvent)
        is InventoryFlow.GeneralInventory -> GeneralInventoryView(uiFlow = uiFlow, onEvent = onEvent)
        is InventoryFlow.GeneralInventoryCounting -> GeneralInventoryCountingView(uiFlow = uiFlow, inventory = inventory, onEvent = onEvent)
        is InventoryFlow.GeneralInventoryFinished -> GeneralInventoryFinishedView(uiFlow = uiFlow, isInventoryEmpty = isInventoryEmpty, onEvent = onEvent)
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
    uiFlow: InventoryFlow.WaitingForBarcode,
    onEvent: (InventoryEvent) -> Unit
) {
    var barcodeText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
//        InputWithIcon(
//            text = "Scan a barcode to lookup asset sku".uppercase(),
//            withResourceIcon = R.drawable.barcode,
//            withError = uiFlow.withError,
//            fill = false
//        )

        Text(
            text = "Scan a barcode to lookup asset sku".uppercase(),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Image(painter = painterResource(id = R.drawable.barcode), contentDescription = null, modifier = Modifier.size(30.dp))
        Spacer(modifier = Modifier.height(16.dp))
        // Manual barcode entry section

        Text(
            text = "Or enter barcode manually:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = barcodeText,
            onValueChange = { barcodeText = it },
            label = { Text("Barcode") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (barcodeText.isNotBlank()) {
                        onEvent(InventoryEvent.ManualBarcodeSubmitted(barcodeText.trim()))
                    }
                }
            ),
            singleLine = true
        )

        // Action buttons with clear spacing
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppButton(
                text = "Lookup",
                onClick = {
                    if (barcodeText.isNotBlank()) {
                        onEvent(InventoryEvent.ManualBarcodeSubmitted(barcodeText.trim()))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            AppButton(
                text = "INVENTORY ALL",
                onClick = { onEvent(InventoryEvent.InventoryAll) },
                modifier = Modifier.fillMaxWidth()
            )
            
            AppButton(
                text = "GENERAL INVENTORY",
                onClick = { onEvent(InventoryEvent.GeneralInventory) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ManualBarcodeEntryView(
    modifier: Modifier = Modifier,
    uiFlow: InventoryFlow.ManualBarcodeEntry,
    onEvent: (InventoryEvent) -> Unit
) {
    var barcodeText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Manual Barcode Entry",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        OutlinedTextField(
            value = barcodeText,
            onValueChange = { barcodeText = it },
            label = { Text("Enter barcode") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppButton(
                text = "Cancel",
                modifier = Modifier.weight(1f)
            ) { 
                onEvent(InventoryEvent.OnKeyUp) // Return to waiting state
            }
            
            AppButton(
                text = "Submit",
                modifier = Modifier.weight(1f)
            ) { 
                onEvent(InventoryEvent.ManualBarcodeSubmitted(barcodeText))
            }
        }
        
        uiFlow.withError?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
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

@Composable
fun InventoryAllView(
    modifier: Modifier = Modifier,
    uiFlow: InventoryFlow.InventoryAll,
    onEvent: (InventoryEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Inventory All Assets",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "This will scan for all RFID tags in range and collect their EPC and TID values.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )
        
        AppButton(
            text = "Start Inventory All",
            modifier = Modifier.fillMaxWidth()
        ) { onEvent(InventoryEvent.OnKeyUp) }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AppButton(
            text = "Cancel",
            modifier = Modifier.fillMaxWidth()
        ) { 
            onEvent(InventoryEvent.OnKeyUp) // Return to waiting state
        }
        
        uiFlow.withError?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun InventoryAllCountingView(
    modifier: Modifier = Modifier,
    uiFlow: InventoryFlow.InventoryAllCounting,
    inventory: Int = 0,
    onEvent: (InventoryEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Inventory All - Scanning",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        CircularProgressIndicator(modifier = Modifier.size(50.dp))
        
        Text(
            text = inventory.toString(),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = "Tags Found",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        HintText(text = "Press trigger to stop scanning", modifier = Modifier.padding(vertical = 32.dp))
    }
}

@Composable
fun InventoryAllFinishedView(
    modifier: Modifier = Modifier,
    uiFlow: InventoryFlow.InventoryAllFinished,
    isInventoryEmpty: Boolean = false,
    onEvent: (InventoryEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Inventory All Complete",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Found ${uiFlow.count} tags",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        if (isInventoryEmpty) {
            AppButton(text = "Finish".uppercase()) { onEvent(InventoryEvent.Finish) }
        } else {
            AppButton(text = "Save and finish".uppercase()) { onEvent(InventoryEvent.Save) }
        }
    }
}

@Composable
fun GeneralInventoryView(
    modifier: Modifier = Modifier,
    uiFlow: InventoryFlow.GeneralInventory,
    onEvent: (InventoryEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "General Inventory",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Scan all company assets",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (uiFlow.withError != null) {
            Text(
                text = uiFlow.withError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        HintText(text = "Press trigger to start scanning", modifier = Modifier.padding(vertical = 32.dp))
    }
}

@Composable
fun GeneralInventoryCountingView(
    modifier: Modifier = Modifier,
    uiFlow: InventoryFlow.GeneralInventoryCounting,
    inventory: Int = 0,
    onEvent: (InventoryEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "General Inventory - Scanning",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Company Assets Found: $inventory",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Text(
            text = "Scanning all company tags...",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        HintText(text = "Press trigger to stop scanning", modifier = Modifier.padding(vertical = 32.dp))
    }
}

@Composable
fun GeneralInventoryFinishedView(
    modifier: Modifier = Modifier,
    uiFlow: InventoryFlow.GeneralInventoryFinished,
    isInventoryEmpty: Boolean = false,
    onEvent: (InventoryEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "General Inventory Complete",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Company Assets Found: ${uiFlow.count}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Text(
            text = "All company tags have been collected and sent to backend for processing.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        AppButton(text = "Finish".uppercase()) { onEvent(InventoryEvent.Finish) }
    }
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
