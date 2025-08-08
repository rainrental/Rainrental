package org.rainrental.rainrentalrfid.commission.presentation.composable

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Loaded Asset".uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(width = 2.dp, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AssetView(asset)
            AnimatedVisibility(!asset.tags.isNullOrEmpty()) {
                asset.tags?.let{
                    LazyColumn {
                    items(
                        items = asset.tags.sortedBy { it.tidHex }
                    ){tag ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            FieldValue(
                                header = tag.tidHex,
                                borderColour = MaterialTheme.colorScheme.secondary,
                                value = tag.epcHex,
                                size = 320,
                                iconSize = 15,
                                equalFontSize = true,
                                hasAction = false,
                            )
                        }
                    }
                }
                }
            }
            AnimatedVisibility(scannedTags.isNotEmpty()) {
                LazyColumn {
                    items(
                        items = scannedTags.sortedBy { !it.writtenEpc }
                    ){tag ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
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
//                        .width(278.dp)
//                        .border(width = 2.dp, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary)
//                        .padding(16.dp)
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
//                    .border(width = 2.dp, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary)
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
}

@Composable
fun AssetView(asset: AssetDetailsResponseDto, general:Boolean = false) {
    if (!general){

        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FieldValue(header = "BARCODE", value = asset.barcode,size = 320)
        }
    }
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        FieldValue(header = "CATEGORY", value = asset.department, size = 160)
        FieldValue(header = "CATEGORY ID", value = asset.departmentId.toString(), size = 160)
    }
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        FieldValue(header = "SKU", value = asset.sku, size = 160)
        FieldValue(header = "SKU ID", value = asset.skuId.toString(), size = 160)
    }
    if (!general) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FieldValue(header = "SERIAL", value = asset.serial, size = 160)
            FieldValue(header = "SERIAL ID", value = asset.serialId.toString(), size = 160)
        }
    }
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        FieldValue(header = "EPC", value = asset.epc, size = 320)
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