package org.rainrental.rainrentalrfid.taglookup.presentation

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupEvent
import org.rainrental.rainrentalrfid.taglookup.data.TagLookupUiFlow
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto

@Composable
fun TagLookupScreen() {
    val tagLookupViewModel: TagLookupViewModel = hiltViewModel()
    val uiFlow by tagLookupViewModel.uiFlow.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var tagToDelete by remember { mutableStateOf("") }
    
    LifecycleAware(
        onPause = { tagLookupViewModel.onScreenPaused() },
        onResume = { tagLookupViewModel.onScreenResumed() },
        onDestroy = { tagLookupViewModel.onBackPressed() }
    )
    
    TagLookupScreen(
        modifier = Modifier,
        uiFlow = uiFlow,
        onDeleteTag = { tidHex -> 
            tagToDelete = tidHex
            showDeleteConfirmation = true
        },
        showDeleteConfirmation = showDeleteConfirmation,
        onConfirmDelete = { 
            showDeleteConfirmation = false
            tagLookupViewModel.onEvent(org.rainrental.rainrentalrfid.taglookup.data.TagLookupEvent.ConfirmDeleteTag(tagToDelete))
        },
        onCancelDelete = { 
            showDeleteConfirmation = false
            tagLookupViewModel.onEvent(org.rainrental.rainrentalrfid.taglookup.data.TagLookupEvent.CancelDeleteTag)
        },
        onEvent = tagLookupViewModel::onEvent
    )
}

@Composable
fun TagLookupScreen(
    modifier: Modifier = Modifier,
    uiFlow: TagLookupUiFlow,
    onDeleteTag: (String) -> Unit = {},
    showDeleteConfirmation: Boolean = false,
    onConfirmDelete: () -> Unit = {},
    onCancelDelete: () -> Unit = {},
    onEvent: (TagLookupEvent) -> Unit = {}
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
                    
                    Button(
                        onClick = { onDeleteTag(uiFlow.tidHex) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Delete Tag")
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
            
            is TagLookupUiFlow.TagDeleted -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tag Deleted",
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
                        text = "This RFID tag has been deleted and cannot be reused",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    uiFlow.deletedFrom?.let { deletedFrom ->
                        Text(
                            text = "Previously associated with: $deletedFrom",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            
            is TagLookupUiFlow.ClearingEpc -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Clear EPC Memory",
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
                    
                    if (uiFlow.scannedEpc.isNotEmpty()) {
                        Text(
                            text = "Scanned EPC: ${uiFlow.scannedEpc}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    Text(
                        text = "Position the tag near the reader and press trigger to clear EPC memory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
            
            is TagLookupUiFlow.EpcCleared -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓ EPC Cleared Successfully",
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
                        text = "Deleting tag from backend...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
            
            is TagLookupUiFlow.DeletingTag -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Text(
                        text = "Deleting Tag",
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
                        text = "Removing tag from backend...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
            
            is TagLookupUiFlow.TagDeletedSuccessfully -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓ Tag Deleted Successfully",
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
                        text = "The tag has been removed from the system",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Button(
                        onClick = { onEvent(TagLookupEvent.ContinueAfterSuccess) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue")
                    }
                }
            }
            
            is TagLookupUiFlow.EpcClearFailed -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EPC Clear Failed",
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
                    
                    Text(
                        text = "Could not clear EPC memory:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = uiFlow.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onEvent(TagLookupEvent.RetryEpcClear) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry EPC Clear")
                        }
                        Button(
                            onClick = { onEvent(TagLookupEvent.DeleteFromBackendOnly) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete from Backend Only")
                        }
                        TextButton(
                            onClick = { onEvent(TagLookupEvent.CancelDeleteProcess) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
            
            is TagLookupUiFlow.DeleteFailed -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Delete Failed",
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
                    
                    Text(
                        text = "Could not delete tag from backend:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = uiFlow.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Button(
                        onClick = { onEvent(TagLookupEvent.CancelDeleteProcess) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
        
        // Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = onCancelDelete,
                title = { Text("Delete Tag") },
                text = { Text("Are you sure you want to delete this tag? This will clear its EPC memory and remove it from the system.") },
                confirmButton = {
                    TextButton(onClick = onConfirmDelete) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCancelDelete) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TagLookupScreenPreview() {
    RainRentalRfidTheme {
        TagLookupScreen(
            modifier = Modifier,
            uiFlow = TagLookupUiFlow.AssetFound(asset = AssetDetailsResponseDto.example(), tidHex = "E2BBCCDDEEFF112233221144", scannedEpc = "E2BBCCDDEEFF112233221144"),
            onDeleteTag = {},
            showDeleteConfirmation = false,
            onConfirmDelete = {},
            onCancelDelete = {}
        )
    }
}
