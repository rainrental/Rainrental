package org.rainrental.rainrentalrfid.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.R
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.rainrental.rainrentalrfid.chainway.data.BarcodeHardwareState
import org.rainrental.rainrentalrfid.chainway.data.RfidHardwareState
import org.rainrental.rainrentalrfid.continuousScanning.data.DeliveryConnectionState
import org.rainrental.rainrentalrfid.chainway.presentation.RfidScreen
import org.rainrental.rainrentalrfid.chainway.presentation.RfidViewModel
import org.rainrental.rainrentalrfid.commission.presentation.CommissionScreen
import org.rainrental.rainrentalrfid.home.presentation.HomeScreen
import org.rainrental.rainrentalrfid.hunt.presentation.HuntScreen
import org.rainrental.rainrentalrfid.inventory.presentation.InventoryScreen
import org.rainrental.rainrentalrfid.continuousScanning.presentation.ContinuousScanningScreen
import org.rainrental.rainrentalrfid.settings.presentation.SettingsScreen
import org.rainrental.rainrentalrfid.taglookup.presentation.TagLookupScreen
import org.rainrental.rainrentalrfid.toast.presentation.Toaster
import org.rainrental.rainrentalrfid.auth.presentation.AuthScreen
import org.rainrental.rainrentalrfid.auth.AuthState
import org.rainrental.rainrentalrfid.auth.AuthViewModel

@Composable
fun CompactHardwareIndicator(
    rfidState: RfidHardwareState,
    barcodeState: BarcodeHardwareState,
    deliveryState: DeliveryConnectionState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // RFID Status Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = "RFID Status",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(12.dp)
            )
            // Status dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = when (rfidState) {
                            RfidHardwareState.Ready -> Color.Green
                            RfidHardwareState.Scanning, RfidHardwareState.Writing -> Color.Yellow
                            RfidHardwareState.Error, RfidHardwareState.ShuttingDown -> Color.Red
                            RfidHardwareState.Init, RfidHardwareState.Configuring -> Color.Yellow
                            else -> Color.Gray
                        },
                        shape = CircleShape
                    )
            )
        }
        
        // Barcode Status Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.barcode),
                contentDescription = "Barcode Status",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(12.dp)
            )
            // Status dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = when (barcodeState) {
                            BarcodeHardwareState.Ready -> Color.Green
                            BarcodeHardwareState.Busy -> Color.Yellow
                            BarcodeHardwareState.Error, BarcodeHardwareState.TimedOut -> Color.Red
                            BarcodeHardwareState.Startup, BarcodeHardwareState.Initialising -> Color.Yellow
                            else -> Color.Gray
                        },
                        shape = CircleShape
                    )
            )
        }
        
        // Delivery Status Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mail,
                contentDescription = "Delivery Status",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(12.dp)
            )
            // Status dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = when (deliveryState) {
                            DeliveryConnectionState.CONNECTED -> Color.Green
                            DeliveryConnectionState.CONNECTING, DeliveryConnectionState.INIT -> Color.Yellow
                            DeliveryConnectionState.ERROR, DeliveryConnectionState.DEAD -> Color.Red
                            DeliveryConnectionState.WAITING_FOR_IP -> Color.Yellow
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun MainApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val mainAppViewModel: MainAppViewModel = hiltViewModel()
    val deliveryState by mainAppViewModel.deliveryState.collectAsState()

    Toaster(snackbarHostState = snackbarHostState)

    // Show authentication screen if not authenticated
    when (authState) {
        is AuthState.Loading -> {
            // Show loading screen
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.checking_authentication),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
        is AuthState.NotAuthenticated -> {
            // Show authentication screen
            AuthScreen(
                onAuthenticated = {
                    // Authentication successful, will trigger state change
                }
            )
        }
        is AuthState.Authenticated -> {
            // Show main app
            ModalNavigationDrawer(
                drawerContent = { },
                drawerState = drawerState
            ){
                Scaffold(
                    topBar = {
                        val rfidViewModel: RfidViewModel = hiltViewModel()
                        val scannerState by rfidViewModel.scannerState.collectAsState()
                        val rfidHardwareState by rfidViewModel.hardwareState.collectAsState()
                        val currentRoute by navController.currentBackStackEntryAsState()
                        val isSettingsScreen = currentRoute?.destination?.route == NavigationRoutes.Settings.route
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .height(56.dp)
                                .background(MaterialTheme.colorScheme.surface)
//                                .padding(horizontal = 16.dp)
                                ,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Compact Hardware Status Indicators (leftmost)
                            CompactHardwareIndicator(
                                rfidState = rfidHardwareState,
                                barcodeState = scannerState,
                                deliveryState = deliveryState,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            // App Title or Back Button (center)
                            if (isSettingsScreen) {
                                // Back button for settings screen
                                IconButton(
                                    onClick = { navController.popBackStack() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            } else {
                                // App Title
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            
                            // Settings Button (rightmost)
                            if (!isSettingsScreen) {
                                IconButton(
                                    onClick = { navController.navigate(NavigationRoutes.Settings.route) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = paddingValues),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        NavHost(
                            navController,
                            startDestination = NavigationRoutes.Home.route
                        ) {
                            composable(NavigationRoutes.Home.route) { HomeScreen(navController) }
                            composable(NavigationRoutes.Commission.route) { CommissionScreen() }
                            composable(NavigationRoutes.Hunt.route) { HuntScreen() }
                            composable(NavigationRoutes.Radar.route) { RfidScreen() }
                            composable(NavigationRoutes.ContinuousScanning.route) { ContinuousScanningScreen() }
                            composable(NavigationRoutes.Inventory.route) { InventoryScreen() }
                            composable(NavigationRoutes.Lookup.route) { TagLookupScreen() }
                            composable(NavigationRoutes.Settings.route) { SettingsScreen() }
                        }
                    }
                }
            }
        }
        is AuthState.Error -> {
            val errorMessage = (authState as AuthState.Error).message
            // Show error state with retry option
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.authentication_error),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { authViewModel.clearError() }
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@Preview
@Composable
fun IconPreview(){
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CompactHardwareIndicator(rfidState = RfidHardwareState.Ready, barcodeState = BarcodeHardwareState.Ready, deliveryState = DeliveryConnectionState.CONNECTED)
    }

}


