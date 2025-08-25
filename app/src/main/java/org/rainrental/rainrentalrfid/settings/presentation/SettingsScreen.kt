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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val mqttServerIp by settingsViewModel.mqttServerIp.collectAsState()
    val ignoreRightSideKey by settingsViewModel.ignoreRightSideKey.collectAsState()
    
    SettingsScreen(
        modifier = Modifier,
        mqttServerIp = mqttServerIp,
        ignoreRightSideKey = ignoreRightSideKey,
        onMqttServerIpChange = settingsViewModel::setMqttServerIp,
        onIgnoreRightSideKeyChange = settingsViewModel::setIgnoreRightSideKey
    )
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    mqttServerIp: String = "",
    ignoreRightSideKey: Boolean = false,
    onMqttServerIpChange: (String) -> Unit = {},
    onIgnoreRightSideKeyChange: (Boolean) -> Unit = {}
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
