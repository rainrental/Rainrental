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
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.R
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.rainrental.rainrentalrfid.chainway.data.BarcodeHardwareState
import org.rainrental.rainrentalrfid.chainway.data.RfidHardwareState
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
fun MainApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

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
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // App Title or Back Button
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
                            
                            // Status Icons and Settings
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // RFID Status Icon
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = "RFID Status",
                                        tint = if (rfidHardwareState == RfidHardwareState.Ready) 
                                            MaterialTheme.colorScheme.onSurface 
                                        else 
                                            MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    if (rfidHardwareState != RfidHardwareState.Ready) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    color = when (rfidHardwareState) {
                                                        RfidHardwareState.Init -> Color.Yellow
                                                        RfidHardwareState.Configuring -> Color.Blue
                                                        RfidHardwareState.Scanning -> Color.Green
                                                        RfidHardwareState.ShuttingDown -> Color.Red
                                                        else -> Color.Gray
                                                    },
                                                    shape = CircleShape
                                                )
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                                
                                // Barcode Status Icon
                                Box {
                                    Icon(
                                        painter = painterResource(R.drawable.barcode),
                                        contentDescription = "Barcode Status",
                                        tint = if (scannerState == BarcodeHardwareState.Ready) 
                                            MaterialTheme.colorScheme.onSurface 
                                        else 
                                            MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    if (scannerState != BarcodeHardwareState.Ready) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    color = when (scannerState) {
                                                        BarcodeHardwareState.Busy -> Color.Green
                                                        else -> Color.Gray
                                                    },
                                                    shape = CircleShape
                                                )
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                                
                                // Settings Button (only show if not on settings screen)
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


