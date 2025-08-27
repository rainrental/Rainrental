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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val mqttServerIp by settingsViewModel.mqttServerIp.collectAsState()
    val ignoreRightSideKey by settingsViewModel.ignoreRightSideKey.collectAsState()
    val updateStatus by settingsViewModel.updateStatus.collectAsState()
    val updateProgress by settingsViewModel.updateProgress.collectAsState()
    val isUpdateInProgress by settingsViewModel.isUpdateInProgress.collectAsState()
    
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
        onClearUpdateStatus = settingsViewModel::clearUpdateStatus
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
    onClearUpdateStatus: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Hardware Key Configuration
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App Updates Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
        
        Spacer(modifier = Modifier.weight(1f))
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
