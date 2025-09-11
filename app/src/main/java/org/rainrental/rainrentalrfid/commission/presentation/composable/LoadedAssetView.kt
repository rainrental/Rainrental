package org.rainrental.rainrentalrfid.commission.presentation.composable

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.rainrental.rainrentalrfid.commission.presentation.FieldValue
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionEvent
import org.rainrental.rainrentalrfid.commission.presentation.model.ScanningTagData
import org.rainrental.rainrentalrfid.shared.presentation.composables.AppButton
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto

@Composable
fun LoadedAssetView(
    asset: AssetDetailsResponseDto,
    scannedTags: List<ScanningTagData> = emptyList(),
    onEvent: (CommissionEvent) -> Unit
) {
    var showTagsPopup by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var tagToDelete by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair of (barcode, tidHex)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
//        Text("Loaded Asset".uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .padding(8.dp)
                .border(width = 2.dp, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary)
                .padding(8.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AssetView(asset, showFull = scannedTags.isEmpty())
            Row(
                modifier = Modifier.padding(vertical = 4.dp).clickable{
                    if (asset.tags.isNotEmpty()) {
                        showTagsPopup = true
                    }
                }
            ) {
                FieldValue(
                    header = "Existing Tags",
                    borderColour = MaterialTheme.colorScheme.secondary,
                    value = asset.tags.size.toString(),
                    size = 320,
                    iconSize = 15,
                    equalFontSize = true,
                    hasAction = false,
                )
            }


        }
        AnimatedVisibility(scannedTags.isNotEmpty()) {

            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()

                    .border(width = 2.dp, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary)
                    .padding(8.dp)
                ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Scanned Tags".uppercase(), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                LazyColumn {
                    items(
                        items = scannedTags.sortedBy { !it.writtenEpc }
                    ){tag ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
//                            .fillMaxWidth()
                        ) {
                            FieldValue(
                                header = tag.tidHex,
                                borderColour = MaterialTheme.colorScheme.tertiary,
                                value = tag.epcHex,
                                size = 320,
                                iconSize = 15,
                                equalFontSize = true,
                                hasAction = true,
                                actionIcon = if (tag.writtenEpc) Icons.Default.Done else Icons.Default.Build,
                                actionIconColour = if (tag.writtenEpc) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                onClick = {
                                    if (!tag.writtenEpc){
                                        onEvent(CommissionEvent.EncodeEpcButtonPressed(
                                            ScanningTagData(tidHex = tag.tidHex, epcHex = tag.epcHex, epcData = asset.epc)
                                        ))
                                    }
                                }
                            )
                        }
                    }
                }
            }

        }

        AnimatedVisibility(scannedTags.none { !it.writtenEpc }) {
            val anotherText = if (scannedTags.isNotEmpty()) "another" else "a"
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                ,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Press trigger to scan $anotherText tag".uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.ExtraBold)
            }
        }
        AnimatedVisibility(scannedTags.any { !it.writtenEpc }) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()

                ,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Press trigger to write EPC".uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.ExtraBold)
            }
        }
        AnimatedVisibility(scannedTags.none { !it.writtenEpc } && scannedTags.isNotEmpty()) {
            AppButton(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
                text = "Save".uppercase()
            ) {
                onEvent(CommissionEvent.SaveButtonPressed)
            }
        }

    }
    
    // Tags popup dialog
    if (showTagsPopup) {
        AlertDialog(
            onDismissRequest = { showTagsPopup = false },
            title = {
                Text(
                    text = "Existing Tags (${asset.tags.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(
                        items = asset.tags.sortedBy { it.tidHex }
                    ) { tag ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            FieldValue(
                                header = tag.tidHex,
                                borderColour = MaterialTheme.colorScheme.secondary,
                                value = tag.epcHex,
                                size = 280,
                                iconSize = 15,
                                equalFontSize = true,
                                hasAction = true,
                                actionIcon = Icons.Default.Delete,
                                actionIconColour = MaterialTheme.colorScheme.error,
                                onClick = {
                                    tagToDelete = Pair(asset.barcode, tag.tidHex)
                                    showDeleteConfirmation = true
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTagsPopup = false }
                ) {
                    Text("Close")
                }
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation && tagToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmation = false
                tagToDelete = null
            },
            title = {
                Text(
                    text = "Delete Tag",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete the tag with TID: ${tagToDelete?.second}?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tagToDelete?.let { (barcode, tidHex) ->
                            onEvent(CommissionEvent.DeleteTagPressed(barcode, tidHex))
                        }
                        showDeleteConfirmation = false
                        tagToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmation = false
                        tagToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AssetView(asset: AssetDetailsResponseDto, general:Boolean = false, showFull: Boolean = true, scannedEpc: String? = null) {
    Box {
        val verticalPadding = 2.dp
        if (!general){

            Row(
                modifier = Modifier.padding(vertical = verticalPadding),
                horizontalArrangement = Arrangement.Center
            ) {
                FieldValue(header = "BARCODE", value = asset.barcode,size = 320)
            }
        }
    asset.productName?.let{ productName ->
        Row(
            modifier = Modifier.padding(vertical = verticalPadding),
            horizontalArrangement = Arrangement.Center
        ) {
            FieldValue(header = "Product Name".uppercase(), value = productName,size = 320)
        }
    }

    if (!showFull) {
        Text(
            text = "Some fields are hidden whilst adding tags",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(vertical = verticalPadding)
        )
    }

    if (showFull) {
        Row(
            modifier = Modifier.padding(vertical = verticalPadding),
            horizontalArrangement = Arrangement.Center
        ) {
            FieldValue(header = "CATEGORY", value = asset.department, size = 160)
            FieldValue(header = "CATEGORY ID", value = asset.departmentId.toString(), size = 160)
        }
        Row(
            modifier = Modifier.padding(vertical = verticalPadding),
            horizontalArrangement = Arrangement.Center
        ) {
            FieldValue(header = "SKU", value = asset.sku, size = 160)
            FieldValue(header = "SKU ID", value = asset.skuId.toString(), size = 160)
        }
        if (!general) {
            Row(
                modifier = Modifier.padding(vertical = verticalPadding),
                horizontalArrangement = Arrangement.Center
            ) {
                FieldValue(header = "SERIAL", value = asset.serial, size = 160)
                FieldValue(header = "SERIAL ID", value = asset.serialId.toString(), size = 160)
            }
        }
    }
    Row(
        modifier = Modifier.padding(vertical = verticalPadding),
        horizontalArrangement = Arrangement.Center
    ) {
        FieldValue(header = "EPC", value = asset.epc, size = 320)
    }
    
    // EPC Match/Mismatch Overlay Indicator (only shown when scannedEpc is provided)
    scannedEpc?.let { scanned ->
        val epcMatch = scanned.equals(asset.epc, ignoreCase = true)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(
                    color = if (epcMatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (epcMatch) "✓" else "⚠",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoadedAssetPreview() {
    RainRentalRfidTheme {
        LoadedAssetView(
            asset = AssetDetailsResponseDto.example(),
            scannedTags = listOf(
//            ScanningTagData(tidHex = "E2BBCCDDEEFF112233221144", epcHex = "E2BBCCDDEEFF112233221144", epcData = "F00081000300000000000001"),
                ScanningTagData(tidHex = "E2BBCCDDEEFF112233221144", epcHex = "E2BBCCDDEEFF112233221144", epcData = "F00081000300000000000001", writtenEpc = true),
            )
        ) { }
    }
}