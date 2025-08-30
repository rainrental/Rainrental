package org.rainrental.rainrentalrfid.settings.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.settings.presentation.SettingsTab
import org.rainrental.rainrentalrfid.auth.AuthState
import org.rainrental.rainrentalrfid.auth.AuthViewModel

@Composable
fun SettingsScreen() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val mqttServerIp by settingsViewModel.mqttServerIp.collectAsState()
    val ignoreRightSideKey by settingsViewModel.ignoreRightSideKey.collectAsState()
    val updateStatus by settingsViewModel.updateStatus.collectAsState()
    val updateProgress by settingsViewModel.updateProgress.collectAsState()
    val isUpdateInProgress by settingsViewModel.isUpdateInProgress.collectAsState()
    
    // Button test states
    val triggerState by settingsViewModel.triggerState.collectAsState()
    val sideState by settingsViewModel.sideState.collectAsState()
    val auxState by settingsViewModel.auxState.collectAsState()
    
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
        updateStatus = updateStatus,
        updateProgress = updateProgress,
        isUpdateInProgress = isUpdateInProgress,
        onMqttServerIpChange = settingsViewModel::setMqttServerIp,
        onIgnoreRightSideKeyChange = settingsViewModel::setIgnoreRightSideKey,
        onCheckForUpdates = { companyId, forceCheck ->
            settingsViewModel.checkForUpdates(companyId, forceCheck)
        },
        onClearUpdateStatus = settingsViewModel::clearUpdateStatus,
        triggerState = triggerState,
        sideState = sideState,
        auxState = auxState,
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
    updateStatus: String? = null,
    updateProgress: Float = 0f,
    isUpdateInProgress: Boolean = false,
    onMqttServerIpChange: (String) -> Unit = {},
    onIgnoreRightSideKeyChange: (Boolean) -> Unit = {},
    onCheckForUpdates: (String, Boolean) -> Unit = { _, _ -> },
    onClearUpdateStatus: () -> Unit = {},
    triggerState: ButtonState = ButtonState.UP,
    sideState: ButtonState = ButtonState.UP,
    auxState: ButtonState = ButtonState.UP,
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
        // Header
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(stringResource(tab.titleResId)) }
                )
            }
        }
        
        // Tab Content
        when (selectedTab) {
            SettingsTab.GENERAL -> GeneralTab(
                mqttServerIp = mqttServerIp,
                onMqttServerIpChange = onMqttServerIpChange
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
                auxState = auxState
            )
            SettingsTab.UPDATES -> UpdatesTab(
                updateStatus = updateStatus,
                updateProgress = updateProgress,
                isUpdateInProgress = isUpdateInProgress,
                onCheckForUpdates = onCheckForUpdates,
                onClearUpdateStatus = onClearUpdateStatus
            )
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
    val backgroundColor = when (state) {
        ButtonState.UP -> Color.LightGray
        ButtonState.DOWN -> color
    }

    val borderColor = when (state) {
        ButtonState.UP -> Color.Gray
        ButtonState.DOWN -> color
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (state == ButtonState.DOWN) Color.White else Color.Black
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = state.name,
            fontSize = 12.sp,
            color = if (state == ButtonState.DOWN) Color.White else Color.Black
        )
    }
}

enum class ButtonState {
    UP, DOWN
}

@Composable
fun GeneralTab(
    mqttServerIp: String,
    onMqttServerIpChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MQTT Server Configuration
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "MQTT Server",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = mqttServerIp,
                onValueChange = onMqttServerIpChange,
                label = { Text("Server IP/Hostname") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_authentication),
                    style = MaterialTheme.typography.titleMedium
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
    auxState: ButtonState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hardware Key Configuration
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Hardware Keys",
                    style = MaterialTheme.typography.titleMedium
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Button Test",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Trigger Button
                ButtonBox(
                    title = "Trigger Button",
                    state = triggerState,
                    color = Color.Red
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Side Button
                ButtonBox(
                    title = "Side Button",
                    state = sideState,
                    color = Color.Blue
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Aux Button
                ButtonBox(
                    title = "Aux Button",
                    state = auxState,
                    color = Color.Green
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Press the hardware buttons to see visual feedback",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
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
    onCheckForUpdates: (String, Boolean) -> Unit,
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
                        text = "1.0.0", // This should come from BuildConfig
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
                
                // Update buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onCheckForUpdates("default", false) },
                        enabled = !isUpdateInProgress,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Check for Updates")
                    }
                    
                    Button(
                        onClick = { onCheckForUpdates("default", true) },
                        enabled = !isUpdateInProgress,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Force Check")
                    }
                }
                
                if (updateStatus != null && !isUpdateInProgress) {
                    Button(
                        onClick = onClearUpdateStatus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Status")
                    }
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
        ignoreRightSideKey = false
    )
}
