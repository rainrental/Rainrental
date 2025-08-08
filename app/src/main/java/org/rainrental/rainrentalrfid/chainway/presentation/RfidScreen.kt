package org.rainrental.rainrentalrfid.chainway.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.rscja.deviceapi.entity.UHFTAGInfo
import org.rainrental.rainrentalrfid.chainway.data.TagWithOrientation
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme

@Composable
fun RfidScreen() {
    val rfidViewModel: RfidViewModel = hiltViewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val connectionStatus by rfidViewModel.connectionStatus.collectAsState()
    val scannedTags by rfidViewModel.scannedTags.collectAsState()
    val huntResults by rfidViewModel.huntResults.collectAsState()
    val hunting by rfidViewModel.hunting.collectAsState()

    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(rfidViewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(rfidViewModel)
        }
    }
    RfidScreen(
        modifier = Modifier,
        connectionStatus = connectionStatus,
        scannedTags = scannedTags,
        huntResults = huntResults,
        hunting = hunting,
        onScan = rfidViewModel::scanTag,
        onHunt = rfidViewModel::huntTag,
        onStop = rfidViewModel::huntTag
    )
}

@Composable
private fun RfidScreen(
    modifier: Modifier = Modifier,
    connectionStatus: Boolean,
    scannedTags: List<UHFTAGInfo> = emptyList<UHFTAGInfo>(),
    huntResults: List<TagWithOrientation> = emptyList<TagWithOrientation>(),
    hunting: Boolean,
    onScan: () -> Unit = {},
    onHunt: (String) -> Unit = {},
    onStop: (String) -> Unit = {}
){
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        AnimatedVisibility(connectionStatus) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Connected", color = MaterialTheme.colorScheme.primary)
                Button(onClick = {
                    if (hunting){
                        onStop("")
                    }else{
                        onScan()
                    }
                }) {
                    if (hunting){
                        Text("Stop")
                    }else{
                        Text("Scan a tag")
                    }
                }
                LazyColumn {
                    items(items = scannedTags){tid->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(tid.tid)
                                Text(tid.epc)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {onHunt(tid.epc)}) {
                                Icon(Icons.Default.Search, contentDescription = "Search for tag")
                            }
                        }

                    }
                }
                AnimatedVisibility(huntResults.isNotEmpty()) {
                    Column {
                        HorizontalDivider()
                        Text("Hunt Results ${huntResults.size}")
                        RadarScreen(detectedTags = huntResults)
                    }
                }
            }
        }
        AnimatedVisibility(!connectionStatus) {
            Text("Waiting for hardware", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RfidScreenPreview() {
    RainRentalRfidTheme {
        RfidScreen(
            modifier = Modifier,
            connectionStatus = true,
            hunting = false
        )
    }
}