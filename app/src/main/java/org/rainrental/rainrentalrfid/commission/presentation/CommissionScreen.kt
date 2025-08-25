package org.rainrental.rainrentalrfid.commission.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.app.BackHandler
import org.rainrental.rainrentalrfid.app.BackHandlerWithCleanup
import org.rainrental.rainrentalrfid.app.LifecycleAware
import org.rainrental.rainrentalrfid.chainway.data.BarcodeHardwareState
import org.rainrental.rainrentalrfid.chainway.data.RfidHardwareState
import org.rainrental.rainrentalrfid.commission.presentation.composable.CommissionedTagsView
import org.rainrental.rainrentalrfid.commission.presentation.composable.LoadedAssetView
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionEvent
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiFlow
import org.rainrental.rainrentalrfid.commission.presentation.model.CommissionUiState
import org.rainrental.rainrentalrfid.commission.presentation.model.ScanningTagData
import org.rainrental.rainrentalrfid.commission.presentation.viewmodel.CommissionTagsViewModel
import org.rainrental.rainrentalrfid.shared.presentation.composables.InputWithIcon
import org.rainrental.rainrentalrfid.shared.presentation.composables.LoadingWithText
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto

@Composable
fun CommissionScreen() {
    val commissionTagsViewModel: CommissionTagsViewModel = hiltViewModel()
    val uiFlow by commissionTagsViewModel.uiFlow.collectAsState()
    val uiState by commissionTagsViewModel.uiState.collectAsState()
    val rfidState by commissionTagsViewModel.hardwareState.collectAsState()
    val scannerState by commissionTagsViewModel.scannerState.collectAsState()
    
    // Handle back navigation and lifecycle events
    BackHandlerWithCleanup(commissionTagsViewModel,preventDefaultNavigation = false)
    
    LifecycleAware(
        onPause = { commissionTagsViewModel.onScreenPaused() },
        onResume = { commissionTagsViewModel.onScreenResumed() },
        onDestroy = { commissionTagsViewModel.onBackPressed() }
    )
    
    CommissionScreenContent(
        modifier = Modifier,
        uiFlow = uiFlow,
        uiState = uiState,
        rfidState = rfidState,
        scannerState = scannerState,
        onEvent = commissionTagsViewModel::onEvent
    )
}

@Composable
private fun CommissionScreenContent(
    modifier: Modifier = Modifier,
    uiState: CommissionUiState,
    uiFlow: CommissionUiFlow,
    rfidState: RfidHardwareState,
    scannerState: BarcodeHardwareState,
    onEvent: (CommissionEvent) -> Unit = {},
){
    LaunchedEffect(Unit) {
        onEvent(CommissionEvent.ScanTagButton)
    }
    when (uiFlow){
        is CommissionUiFlow.WaitingForBarcodeInput -> InputWithIcon(text = "Scan a barcode to lookup asset", withError = uiFlow.withError, withResourceIcon = R.drawable.barcode)
        CommissionUiFlow.ScanningBarcode -> LoadingWithText(text = "Scanning Barcode")
        is CommissionUiFlow.LookingUpAsset -> LoadingWithText(text = "Looking up asset with barcode ${uiFlow.barcode}")
        is CommissionUiFlow.LoadedAsset -> LoadedAssetView(asset = uiFlow.asset, scannedTags = uiFlow.scannedTags, onEvent = onEvent)
        is CommissionUiFlow.CommissioningTags -> LoadingWithText(text = "Commissioning ${uiFlow.scannedTags.size} tags to barcode ${uiFlow.asset.barcode}")
        is CommissionUiFlow.CommissionedTags -> CommissionedTagsView()
        is CommissionUiFlow.ScanningRfid -> LoadingWithText(text = uiFlow.withText)
        is CommissionUiFlow.WritingEPC -> LoadingWithText(text = "Writing EPC data \n\n${uiFlow.writingEpc}\n\nto tag\n\n${uiFlow.writingTid}")
    }
    AnimatedVisibility(scannerState == BarcodeHardwareState.Busy,enter = fadeIn(), exit = fadeOut()) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            LoadingWithText(text = "Scanning barcode...")
        }
    }
    AnimatedVisibility(rfidState == RfidHardwareState.Init || rfidState == RfidHardwareState.Configuring || rfidState == RfidHardwareState.Scanning || rfidState == RfidHardwareState.ShuttingDown,
        enter = fadeIn(), exit = fadeOut()) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            when (rfidState){
                RfidHardwareState.Init -> LoadingWithText(text = "Initialising RFID...")
                RfidHardwareState.Configuring -> LoadingWithText(text = "Scanning barcode...")
                RfidHardwareState.Scanning -> LoadingWithText(text = "Scanning RFID...")
                RfidHardwareState.ShuttingDown -> LoadingWithText(text = "Shutting down RFID...")
                else -> {}
            }

        }
    }
    AnimatedVisibility(uiState.saving,enter = fadeIn(), exit = fadeOut()) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            LoadingWithText(text = "Saving")
        }
    }
}


@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CommissionScreenPreview(modifier: Modifier = Modifier) {
    CommissionScreenContent(
        uiState = CommissionUiState(),
        uiFlow = CommissionUiFlow.LoadedAsset(asset = AssetDetailsResponseDto.example(), scannedTags = listOf(
            ScanningTagData(tidHex = "E2BBCCDDEEFF112233221144", epcHex = "E2BBCCDDEEFF112233221144", epcData = "F00081000300000000000001")
        )),
        rfidState = RfidHardwareState.Ready,
        scannerState = BarcodeHardwareState.Ready,
        onEvent = {}
    )
}

