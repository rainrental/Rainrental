package org.rainrental.rainrentalrfid.settings.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

enum class SettingsTab(val titleResId: Int, val icon: ImageVector) {
    GENERAL(org.rainrental.rainrentalrfid.R.string.settings_general, Icons.Default.Settings),
    AUTHENTICATION(org.rainrental.rainrentalrfid.R.string.settings_auth, Icons.Default.Person),
    HARDWARE(org.rainrental.rainrentalrfid.R.string.settings_hardware, Icons.Default.Build),
    MQTT(org.rainrental.rainrentalrfid.R.string.settings_mqtt, Icons.Default.Wifi),
    UPDATES(org.rainrental.rainrentalrfid.R.string.settings_updates, Icons.Default.Update)
}
