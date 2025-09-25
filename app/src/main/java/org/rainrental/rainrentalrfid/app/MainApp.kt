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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
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
import org.rainrental.rainrentalrfid.components.ui.BackConfirmationDialog
import org.rainrental.rainrentalrfid.navigation.BackConfirmableFeature
import org.rainrental.rainrentalrfid.inventory.presentation.InventoryViewModel
import org.rainrental.rainrentalrfid.hardware.HardwareEventBus
import org.rainrental.rainrentalrfid.hardware.HardwareEventListener
import org.rainrental.rainrentalrfid.commission.presentation.viewmodel.CommissionTagsViewModel
import org.rainrental.rainrentalrfid.hunt.presentation.HuntViewModel
import org.rainrental.rainrentalrfid.continuousScanning.presentation.ContinuousScanningViewModel
import org.rainrental.rainrentalrfid.settings.presentation.SettingsViewModel
import org.rainrental.rainrentalrfid.logging.LogUtils

@Composable
fun CompactHardwareIndicator(
    rfidState: RfidHardwareState,
    barcodeState: BarcodeHardwareState,
    deliveryState: DeliveryConnectionState,
    modifier: Modifier = Modifier
) {
    var showPopup by remember { mutableStateOf(false) }
    
    Box {
        // Main indicator (clickable)
        Column(
            modifier = modifier.clickable { showPopup = !showPopup },
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
        
        // Popup overlay
        if (showPopup) {
            // Background overlay with reduced saturation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { showPopup = false }
                    .zIndex(1f)
            )
            
            // Popup content
            Box(
                modifier = Modifier
                    .offset(x = (-40).dp, y = 20.dp) // Position closer to center and below the indicator
                    .width(200.dp) // Ensure minimum width for visibility
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
                    .zIndex(2f)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Hardware Status",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // RFID Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "RFID Status",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "RFID Scanner",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = rfidState.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = when (rfidState) {
                                    RfidHardwareState.Ready -> Color.Green
                                    RfidHardwareState.Scanning, RfidHardwareState.Writing -> Color(0xFFFFA500) // Orange
                                    RfidHardwareState.Error, RfidHardwareState.ShuttingDown -> Color.Red
                                    RfidHardwareState.Init, RfidHardwareState.Configuring -> Color(0xFFFFA500) // Orange
                                    else -> Color.Gray
                                }
                            )
                        }
                    }
                    
                    // Barcode Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.barcode),
                            contentDescription = "Barcode Status",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Barcode Scanner",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = barcodeState.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = when (barcodeState) {
                                    BarcodeHardwareState.Ready -> Color.Green
                                    BarcodeHardwareState.Busy -> Color(0xFFFFA500) // Orange
                                    BarcodeHardwareState.Error, BarcodeHardwareState.TimedOut -> Color.Red
                                    BarcodeHardwareState.Startup, BarcodeHardwareState.Initialising -> Color(0xFFFFA500) // Orange
                                    else -> Color.Gray
                                }
                            )
                        }
                    }
                    
                    // Delivery Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = "Delivery Status",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "MQTT Delivery",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = deliveryState.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = when (deliveryState) {
                                    DeliveryConnectionState.CONNECTED -> Color.Green
                                    DeliveryConnectionState.CONNECTING, DeliveryConnectionState.INIT -> Color(0xFFFFA500) // Orange
                                    DeliveryConnectionState.ERROR, DeliveryConnectionState.DEAD -> Color.Red
                                    DeliveryConnectionState.WAITING_FOR_IP -> Color(0xFFFFA500) // Orange
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainApp(modifier: Modifier = Modifier) {
    LogUtils.logd("MainApp", "🔥 MainApp composable called")
    
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val mainAppViewModel: MainAppViewModel = hiltViewModel()
    val deliveryState by mainAppViewModel.deliveryState.collectAsState()
    val watchdogState by mainAppViewModel.watchdogState.collectAsState()
    
    // ViewModels for back confirmation and hardware events
    val inventoryViewModel: InventoryViewModel = hiltViewModel()
    val commissionViewModel: CommissionTagsViewModel = hiltViewModel()
    val huntViewModel: HuntViewModel = hiltViewModel()
    val continuousScanningViewModel: ContinuousScanningViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    
    // Hardware event bus for managing active listener
    val hardwareEventBus: HardwareEventBus = (context.applicationContext as RainRentalRfidApp).hardwareEventBus
    
    // Back confirmation state
    var showBackConfirmation by remember { mutableStateOf(false) }
    var pendingNavigation by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    // Helper function to get current feature
    fun getCurrentFeature(currentRoute: String?): BackConfirmableFeature? {
        return when (currentRoute) {
            NavigationRoutes.Inventory.route -> inventoryViewModel
            NavigationRoutes.Commission.route -> commissionViewModel
            NavigationRoutes.Hunt.route -> huntViewModel
            NavigationRoutes.ContinuousScanning.route -> continuousScanningViewModel
            else -> null
        }
    }
    
    // Helper function to get current hardware event listener
    fun getCurrentHardwareListener(currentRoute: String?): HardwareEventListener? {
        LogUtils.logd("MainApp", "🔥 getCurrentHardwareListener called with route: $currentRoute")
        val result = when (currentRoute) {
            NavigationRoutes.Home.route -> {
                LogUtils.logd("MainApp", "🔥 Home route - no hardware listener needed")
                null // Home screen doesn't need hardware events
            }
            NavigationRoutes.Inventory.route -> {
                LogUtils.logd("MainApp", "🔥 Inventory route - returning inventoryViewModel")
                inventoryViewModel
            }
            NavigationRoutes.Commission.route -> {
                LogUtils.logd("MainApp", "🔥 Commission route - returning commissionViewModel")
                commissionViewModel
            }
            NavigationRoutes.Hunt.route -> {
                LogUtils.logd("MainApp", "🔥 Hunt route - returning huntViewModel")
                huntViewModel
            }
            NavigationRoutes.ContinuousScanning.route -> {
                LogUtils.logd("MainApp", "🔥 ContinuousScanning route - returning continuousScanningViewModel")
                continuousScanningViewModel
            }
            NavigationRoutes.Settings.route -> {
                LogUtils.logd("MainApp", "🔥 Settings route - returning settingsViewModel")
                settingsViewModel
            }
            else -> {
                LogUtils.logd("MainApp", "🔥 Unknown route: $currentRoute, no hardware listener set")
                null
            }
        }
        LogUtils.logd("MainApp", "🔥 getCurrentHardwareListener returning: ${result?.javaClass?.simpleName ?: "null"}")
        return result
    }
    
    // Helper function to handle back press with confirmation
    fun handleBackPress() {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        val currentFeature = getCurrentFeature(currentRoute)
        
        if (currentFeature?.hasUnsavedChanges() == true) {
            showBackConfirmation = true
            pendingNavigation = { 
                currentFeature.resetState()
                navController.popBackStack()
            }
        } else {
            navController.popBackStack()
        }
    }
    
    // Helper function to handle navigation to home (reset all features)
    fun handleNavigateToHome() {
        // Reset all features when going to home
        inventoryViewModel.resetState()
        commissionViewModel.resetState()
        huntViewModel.resetState()
        continuousScanningViewModel.resetState()
        navController.navigate(NavigationRoutes.Home.route)
    }

    Toaster(snackbarHostState = snackbarHostState)
    
    // Start MQTT watchdog when authenticated
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            mainAppViewModel.startMqttWatchdog(context)
        }
    }
    
    // Manage active hardware event listener based on current route
    LaunchedEffect(navController.currentBackStackEntry) {
        LogUtils.logd("MainApp", "🔥 LaunchedEffect(navController.currentBackStackEntry) triggered")
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        val activeListener = getCurrentHardwareListener(currentRoute)
        LogUtils.logd("MainApp", "🔥 Route changed to: $currentRoute, setting active listener: ${activeListener?.javaClass?.simpleName ?: "none"}")
        hardwareEventBus.setActiveListener(activeListener)
    }
    
    // Also set initial listener when MainApp first loads
    LaunchedEffect(Unit) {
        LogUtils.logd("MainApp", "🔥 LaunchedEffect(Unit) triggered")
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        val activeListener = getCurrentHardwareListener(currentRoute)
        LogUtils.logd("MainApp", "🔥 Initial load - Route: $currentRoute, setting active listener: ${activeListener?.javaClass?.simpleName ?: "none"}")
        hardwareEventBus.setActiveListener(activeListener)
    }
    
    // Debug: Log current route changes
    LaunchedEffect(navController.currentBackStackEntry) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        LogUtils.logd("MainApp", "🔥 Route changed to: $currentRoute")
    }

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
                        val currentFeatureName = currentRoute?.destination?.route?.let { route ->
                            NavigationRoutes.values().find { it.route == route }?.title
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .height(72.dp)
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
                            
                            // App Title and Feature Name or Settings Title (center)
                            if (isSettingsScreen) {
                                // Settings title for settings screen
                                Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            } else {
                                // App Title and Feature Name
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (currentFeatureName != null) {
                                        Text(
                                            text = currentFeatureName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            // Right side button - Settings icon for home, Back arrow for others
                            val isHomeScreen = currentRoute?.destination?.route == NavigationRoutes.Home.route
                            if (isHomeScreen) {
                                // Settings icon for home screen
                                IconButton(
                                    onClick = { navController.navigate(NavigationRoutes.Settings.route) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            } else {
                                // Back arrow for all other screens
                                IconButton(
                                    onClick = { handleBackPress() }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
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
                            composable(NavigationRoutes.Home.route) { 
                                HomeScreen(
                                    navController = navController,
                                    onNavigateWithReset = { destination ->
                                        // Reset all features when navigating from home
                                        inventoryViewModel.resetState()
                                        commissionViewModel.resetState()
                                        huntViewModel.resetState()
                                        continuousScanningViewModel.resetState()
                                        navController.navigate(destination)
                                    }
                                )
                            }
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
    
    // Back confirmation dialog
    BackConfirmationDialog(
        isVisible = showBackConfirmation,
        message = getCurrentFeature(navController.currentBackStackEntry?.destination?.route)?.getUnsavedChangesDescription() ?: "You have unsaved changes",
        onConfirm = {
            showBackConfirmation = false
            pendingNavigation?.invoke()
            pendingNavigation = null
        },
        onCancel = {
            showBackConfirmation = false
            pendingNavigation = null
        }
    )
}

@Preview
@Composable
fun IconPreview(){
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CompactHardwareIndicator(rfidState = RfidHardwareState.Ready, barcodeState = BarcodeHardwareState.Ready, deliveryState = DeliveryConnectionState.CONNECTED)
    }

}


