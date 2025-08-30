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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.R
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.rainrental.rainrentalrfid.chainway.data.BarcodeHardwareState
import org.rainrental.rainrentalrfid.chainway.data.RfidHardwareState
import org.rainrental.rainrentalrfid.chainway.presentation.RfidScreen
import org.rainrental.rainrentalrfid.chainway.presentation.RfidViewModel
import org.rainrental.rainrentalrfid.commission.presentation.CommissionScreen
import org.rainrental.rainrentalrfid.home.presentation.HomeScreen
import org.rainrental.rainrentalrfid.hunt.presentation.HuntScreen
import org.rainrental.rainrentalrfid.inventory.presentation.InventoryScreen
import org.rainrental.rainrentalrfid.continuousScanning.presentation.ContinuousScanningScreen
import org.rainrental.rainrentalrfid.test.presentation.ButtonTestScreen
import org.rainrental.rainrentalrfid.settings.presentation.SettingsScreen
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
                    bottomBar = {
//                        BottomBar(modifier = Modifier.height(60.dp))
                    },
                    topBar = {
                        BottomBar(modifier = Modifier.height(60.dp))
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
                            composable(NavigationRoutes.ButtonTest.route) { ButtonTestScreen() }
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

@Composable
fun BottomBar(modifier: Modifier = Modifier) {
    val rfidViewModel: RfidViewModel = hiltViewModel()
    val scannerState by rfidViewModel.scannerState.collectAsState()
    val rfidHardwareState by rfidViewModel.hardwareState.collectAsState()
    BottomAppBar(
        modifier = modifier
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(0.5f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.barcode), style = MaterialTheme.typography.labelSmall)
                AnimatedVisibility(scannerState != BarcodeHardwareState.Ready) {
                    Text(rfidHardwareState.name, style = MaterialTheme.typography.labelSmall)
                }
            }
            Column(modifier = Modifier.weight(0.5f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.rfid), style = MaterialTheme.typography.labelSmall)
                AnimatedVisibility(rfidHardwareState != RfidHardwareState.Ready) {
                    Text(rfidHardwareState.name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
