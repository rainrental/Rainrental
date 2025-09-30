package org.rainrental.rainrentalrfid.settings.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.auth.AuthState
import org.rainrental.rainrentalrfid.logging.LogUtils
import org.rainrental.rainrentalrfid.auth.AuthViewModel

@Composable
fun SettingsScreen() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val mqttServerIp by settingsViewModel.mqttServerIp.collectAsState()
    val ignoreRightSideKey by settingsViewModel.ignoreRightSideKey.collectAsState()
    val systemVolume by settingsViewModel.systemVolume.collectAsState()
    val maxSystemVolume by settingsViewModel.maxSystemVolume.collectAsState()
    val updateStatus by settingsViewModel.updateStatus.collectAsState()
    val updateProgress by settingsViewModel.updateProgress.collectAsState()
    val isUpdateInProgress by settingsViewModel.isUpdateInProgress.collectAsState()
    val installedVersion by settingsViewModel.installedVersion.collectAsState()
    
    // MQTT Watchdog state
    val watchdogState by settingsViewModel.watchdogState.collectAsState()
    val consecutiveFailures by settingsViewModel.consecutiveFailures.collectAsState()
    val lastCheckTime by settingsViewModel.lastCheckTime.collectAsState()
    val nextCheckDelay by settingsViewModel.nextCheckDelay.collectAsState()
    val isPaused by settingsViewModel.isPaused.collectAsState()
    
    // Button test states - using collectAsState (guaranteed to work)
    val triggerState by settingsViewModel.triggerState.collectAsState()
    val sideState by settingsViewModel.sideState.collectAsState()
    val auxState by settingsViewModel.auxState.collectAsState()
    val testCounter by settingsViewModel.testCounter.collectAsState()
    
    // Authentication state
    val authState by authViewModel.authState.collectAsState()
    val showRevokeConfirmation by settingsViewModel.showRevokeConfirmation.collectAsState()
    
    // Update settings auth state when auth state changes
    LaunchedEffect(authState) {
        settingsViewModel.setAuthState(authState)
    }
    
    SettingsScreen(
        modifier = Modifier,
        mqttServerIp = mqttServerIp,
        ignoreRightSideKey = ignoreRightSideKey,
        systemVolume = systemVolume,
        maxSystemVolume = maxSystemVolume,
        updateStatus = updateStatus,
        updateProgress = updateProgress,
        isUpdateInProgress = isUpdateInProgress,
        installedVersion = installedVersion,
        onMqttServerIpChange = settingsViewModel::setMqttServerIp,
        onIgnoreRightSideKeyChange = settingsViewModel::setIgnoreRightSideKey,
        onSystemVolumeChange = settingsViewModel::setSystemVolume,
        onCheckForUpdates = { forceCheck ->
            settingsViewModel.checkForUpdates(forceCheck)
        },
        onClearUpdateStatus = settingsViewModel::clearUpdateStatus,
        watchdogState = watchdogState,
        consecutiveFailures = consecutiveFailures,
        lastCheckTime = lastCheckTime,
        nextCheckDelay = nextCheckDelay,
        isPaused = isPaused,
        onForceMqttConnectionCheck = settingsViewModel::forceMqttConnectionCheck,
        triggerState = triggerState,
        sideState = sideState,
        auxState = auxState,
        testCounter = testCounter,
        authState = authState,
        showRevokeConfirmation = showRevokeConfirmation,
        onShowRevokeConfirmation = settingsViewModel::showRevokeConfirmation,
        onHideRevokeConfirmation = settingsViewModel::hideRevokeConfirmation,
        onRevokeAuthentication = {
            authViewModel.signOut()
            settingsViewModel.revokeAuthentication()
        }
    )
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    mqttServerIp: String = "",
    ignoreRightSideKey: Boolean = false,
    systemVolume: Int = 0,
    maxSystemVolume: Int = 15,
    updateStatus: String? = null,
    updateProgress: Float = 0f,
    isUpdateInProgress: Boolean = false,
    installedVersion: String = "",
    onMqttServerIpChange: (String) -> Unit = {},
    onIgnoreRightSideKeyChange: (Boolean) -> Unit = {},
    onSystemVolumeChange: (Int) -> Unit = {},
    onCheckForUpdates: (Boolean) -> Unit = { _ -> },
    onClearUpdateStatus: () -> Unit = {},
    watchdogState: org.rainrental.rainrentalrfid.mqtt.WatchdogState = org.rainrental.rainrentalrfid.mqtt.WatchdogState.STOPPED,
    consecutiveFailures: Int = 0,
    lastCheckTime: Long = 0L,
    nextCheckDelay: Long = 30000L,
    isPaused: Boolean = false,
    onForceMqttConnectionCheck: () -> Unit = {},
    triggerState: ButtonState = ButtonState.UP,
    sideState: ButtonState = ButtonState.UP,
    auxState: ButtonState = ButtonState.UP,
    testCounter: Int = 0,
    authState: AuthState = AuthState.Loading,
    showRevokeConfirmation: Boolean = false,
    onShowRevokeConfirmation: () -> Unit = {},
    onHideRevokeConfirmation: () -> Unit = {},
    onRevokeAuthentication: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(SettingsTab.GENERAL) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Tab Content - with weight to take available space
        Box(
            modifier = Modifier.weight(1f)
        ) {
            when (selectedTab) {
                SettingsTab.GENERAL -> GeneralTab(
                    systemVolume = systemVolume,
                    maxSystemVolume = maxSystemVolume,
                    onSystemVolumeChange = onSystemVolumeChange
                )
                SettingsTab.AUTHENTICATION -> AuthenticationTab(
                    authState = authState,
                    onShowRevokeConfirmation = onShowRevokeConfirmation
                )
                SettingsTab.HARDWARE -> HardwareTab(
                    ignoreRightSideKey = ignoreRightSideKey,
                    onIgnoreRightSideKeyChange = onIgnoreRightSideKeyChange,
                    triggerState = triggerState,
                    sideState = sideState,
                    auxState = auxState,
                    testCounter = testCounter
                )
                SettingsTab.MQTT -> MqttTab(
                    mqttServerIp = mqttServerIp,
                    watchdogState = watchdogState,
                    consecutiveFailures = consecutiveFailures,
                    lastCheckTime = lastCheckTime,
                    nextCheckDelay = nextCheckDelay,
                    isPaused = isPaused,
                    onMqttServerIpChange = onMqttServerIpChange,
                    onForceMqttConnectionCheck = onForceMqttConnectionCheck
                )
                SettingsTab.UPDATES -> UpdatesTab(
                    updateStatus = updateStatus,
                    updateProgress = updateProgress,
                    isUpdateInProgress = isUpdateInProgress,
                    installedVersion = installedVersion,
                    onCheckForUpdates = onCheckForUpdates,
                    onClearUpdateStatus = onClearUpdateStatus
                )
            }
        }
        
        // Tab Row at bottom
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = stringResource(tab.titleResId),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = { 
                        Text(
                            text = stringResource(tab.titleResId),
                            fontSize = 10.sp
                        ) 
                    }
                )
            }
        }
    }
    
    // Revoke Confirmation Dialog
    if (showRevokeConfirmation) {
        AlertDialog(
            onDismissRequest = onHideRevokeConfirmation,
            title = { Text(stringResource(R.string.auth_revoke_confirmation_title)) },
            text = { Text(stringResource(R.string.auth_revoke_confirmation_message)) },
            confirmButton = {
                Button(
                    onClick = onRevokeAuthentication,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.auth_revoke_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onHideRevokeConfirmation) {
                    Text(stringResource(R.string.auth_revoke_cancel))
                }
            }
        )
    }
}

@Composable
private fun ButtonBox(
    title: String,
    state: ButtonState,
    color: Color
) {
    LogUtils.logd("ButtonBox", "🔥 $title state changed to: $state")
    
    val backgroundColor = when (state) {
        ButtonState.UP -> MaterialTheme.colorScheme.surface
        ButtonState.DOWN -> color
    }

    val borderColor = when (state) {
        ButtonState.UP -> MaterialTheme.colorScheme.outline
        ButtonState.DOWN -> color
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp) // Less rounded for Material Design
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (state == ButtonState.DOWN) Color.White else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = state.name,
            fontSize = 10.sp,
            color = if (state == ButtonState.DOWN) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

enum class ButtonState {
    UP, DOWN
}

@Composable
fun GeneralTab(
    systemVolume: Int,
    maxSystemVolume: Int,
    onSystemVolumeChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // System Volume Control
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "System Volume",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Controls all system volumes (Media, Ring, Alarm, Notification, Call)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = systemVolume.toFloat(),
                    onValueChange = { onSystemVolumeChange(it.toInt()) },
                    valueRange = 0f..maxSystemVolume.toFloat(),
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline
                        )
                )
                Text(
                    text = maxSystemVolume.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Current Volume: $systemVolume",
                style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            }
        }
    }
}

@Composable
fun AuthenticationTab(
    authState: AuthState,
    onShowRevokeConfirmation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_auth),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                when (authState) {
                    is AuthState.Authenticated -> {
                        // Current User
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.auth_current_user),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = authState.user.email ?: stringResource(R.string.auth_unknown),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        // Location
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.auth_location),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = authState.locationName ?: stringResource(R.string.auth_unknown),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        // Company ID
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.auth_company_id),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = authState.companyId ?: stringResource(R.string.auth_unknown),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
                        // Revoke Button
                        Button(
                            onClick = onShowRevokeConfirmation,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.auth_revoke_authentication))
                        }
                    }
                    is AuthState.NotAuthenticated -> {
                        Text(
                            text = stringResource(R.string.auth_not_authenticated),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is AuthState.Loading -> {
                        Text(
                            text = "Loading authentication status...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is AuthState.Error -> {
                        Text(
                            text = "Authentication error: ${authState.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HardwareTab(
    ignoreRightSideKey: Boolean,
    onIgnoreRightSideKeyChange: (Boolean) -> Unit,
    triggerState: ButtonState,
    sideState: ButtonState,
    auxState: ButtonState,
    testCounter: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hardware Key Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Hardware Keys",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ignore Right Side Key",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = ignoreRightSideKey,
                    onCheckedChange = onIgnoreRightSideKeyChange
                )
                }
            }
        }
        
        // Button Test Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Button Test",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$testCounter clicks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                }

                
                // Trigger Button
                ButtonBox(
                    title = "Trigger Button",
                    state = triggerState,
                    color = Color.Red
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Side Button
                ButtonBox(
                    title = "Side Button",
                    state = sideState,
                    color = Color.Blue
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Aux Button
                ButtonBox(
                    title = "Aux Button",
                    state = auxState,
                    color = Color.Green
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Press the hardware buttons to see visual feedback",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun UpdatesTab(
    updateStatus: String?,
    updateProgress: Float,
    isUpdateInProgress: Boolean,
    installedVersion: String,
    onCheckForUpdates: (Boolean) -> Unit,
    onClearUpdateStatus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "App Updates",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Current version info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current Version",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = installedVersion,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                // Update status
                if (updateStatus != null) {
                    Text(
                        text = updateStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (updateProgress > 0f) {
                        LinearProgressIndicator(
                            progress = { updateProgress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Update button
                    Button(
                        onClick = { onCheckForUpdates(false) },
                        enabled = !isUpdateInProgress,
                    modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Check for Updates")
                }
                
                
                if (updateStatus != null && !isUpdateInProgress) {
                    Button(
                        onClick = onClearUpdateStatus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Status")
                    }
                }
                
                // Test Crash Button (for Crashlytics verification)
                Button(
                    onClick = {
                        // Trigger a test crash for Crashlytics verification
                        throw RuntimeException("Test crash for Crashlytics verification")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Crash (Crashlytics)")
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        modifier = Modifier,
        mqttServerIp = "192.168.1.100",
        ignoreRightSideKey = true,
        triggerState = ButtonState.UP,
        sideState = ButtonState.UP,
        auxState = ButtonState.UP
    )
}

@Composable
fun MqttTab(
    mqttServerIp: String,
    watchdogState: org.rainrental.rainrentalrfid.mqtt.WatchdogState,
    consecutiveFailures: Int,
    lastCheckTime: Long,
    nextCheckDelay: Long,
    isPaused: Boolean,
    onMqttServerIpChange: (String) -> Unit,
    onForceMqttConnectionCheck: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MQTT Server Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "MQTT Server Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = mqttServerIp,
                    onValueChange = onMqttServerIpChange,
                    label = { Text("Server IP/Hostname") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
        
        Text(
            text = "MQTT Connection Status",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Watchdog Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Connection Watchdog",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (watchdogState) {
                            org.rainrental.rainrentalrfid.mqtt.WatchdogState.STOPPED -> "Stopped"
                            org.rainrental.rainrentalrfid.mqtt.WatchdogState.STARTING -> "Starting"
                            org.rainrental.rainrentalrfid.mqtt.WatchdogState.RUNNING -> if (isPaused) "Paused" else "Running"
                            org.rainrental.rainrentalrfid.mqtt.WatchdogState.STOPPING -> "Stopping"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = when (watchdogState) {
                            org.rainrental.rainrentalrfid.mqtt.WatchdogState.RUNNING -> if (isPaused) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            org.rainrental.rainrentalrfid.mqtt.WatchdogState.STOPPED -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
                
                if (consecutiveFailures > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Consecutive Failures:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = consecutiveFailures.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            color = if (consecutiveFailures > 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                
                if (lastCheckTime > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last Check:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                .format(java.util.Date(lastCheckTime)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (nextCheckDelay > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Check:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${nextCheckDelay / 1000}s",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Force Check Button - only show when not running
        if (watchdogState != org.rainrental.rainrentalrfid.mqtt.WatchdogState.RUNNING || isPaused) {
            Button(
                onClick = onForceMqttConnectionCheck,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp) // Less rounded
            ) {
                Text("Force Connection Check")
            }
        }
        
        // Information
//        Text(
//            text = "The MQTT watchdog monitors connection and reconnects automatically. Pauses when app is backgrounded to save battery.",
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant,
//            modifier = Modifier.padding(top = 8.dp)
//        )
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HardwarePreview(){
    HardwareTab(ignoreRightSideKey = true, onIgnoreRightSideKeyChange = {}, triggerState = ButtonState.UP, sideState = ButtonState.UP, auxState = ButtonState.UP, testCounter = 0)
}