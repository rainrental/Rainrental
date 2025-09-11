package org.rainrental.rainrentalrfid.taglookup.presentation

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.app.LifecycleAware
import org.rainrental.rainrentalrfid.commission.presentation.composable.AssetView
import org.rainrental.rainrentalrfid.shared.presentation.composables.InputWithIcon
import org.rainrental.rainrentalrfid.shared.presentation.composables.LoadingWithText
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupUiFlow
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme

@Composable
fun TagLookupScreen() {
    val tagLookupViewModel: TagLookupViewModel = hiltViewModel()
    val uiFlow by tagLookupViewModel.uiFlow.collectAsState()
    
    LifecycleAware(
        onPause = { tagLookupViewModel.onScreenPaused() },
        onResume = { tagLookupViewModel.onScreenResumed() },
        onDestroy = { tagLookupViewModel.onBackPressed() }
    )
    
    TagLookupScreen(
        modifier = Modifier,
        uiFlow = uiFlow
    )
}

@Composable
fun TagLookupScreen(
    modifier: Modifier = Modifier,
    uiFlow: TagLookupUiFlow
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiFlow) {
            TagLookupUiFlow.WaitingForTag -> {
                InputWithIcon(
                    text = "Press trigger to scan RFID tag",
                    withResourceIcon = R.drawable.barcode
                )
            }
            
            TagLookupUiFlow.ScanningTag -> {
                LoadingWithText(text = "Scanning RFID tag...")
            }
            
            is TagLookupUiFlow.LookingUpAsset -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LoadingWithText(text = "Looking up asset for tag: ${uiFlow.tidHex}")
                    Text(
                        text = "Scanned EPC: ${uiFlow.scannedEpc}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            is TagLookupUiFlow.AssetFound -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Asset Found",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Tag ID: ${uiFlow.tidHex}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "Scanned EPC: ${uiFlow.scannedEpc}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AssetView(asset = uiFlow.asset, scannedEpc = uiFlow.scannedEpc)
                    }
                    
                    Text(
                        text = "Press trigger to scan another tag",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            
            is TagLookupUiFlow.AssetNotFound -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Asset Not Found",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Tag ID: ${uiFlow.tidHex}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (uiFlow.scannedEpc.isNotEmpty()) {
                        Text(
                            text = "Scanned EPC: ${uiFlow.scannedEpc}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    Text(
                        text = "This RFID tag is not associated with any asset",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    uiFlow.withError?.let { error ->
                        Text(
                            text = "Error: $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    Text(
                        text = "Press trigger to scan another tag",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TagLookupScreenPreview() {
    RainRentalRfidTheme {
        TagLookupScreen(
            modifier = Modifier,
            uiFlow = TagLookupUiFlow.WaitingForTag
        )
    }
}
