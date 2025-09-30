package org.rainrental.rainrentalrfid.home.presentation

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.app.NavigationRoutes
import org.rainrental.rainrentalrfid.shared.presentation.composables.AppButton
import org.rainrental.rainrentalrfid.shared.presentation.composables.MaterialMenuButton
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme

@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateWithReset: (String) -> Unit = { destination -> navController.navigate(destination) }
) {
    HomeScreen(modifier = Modifier, onTap = { destination ->
        onNavigateWithReset(destination.route)
    })
}

@Composable
private fun HomeScreen(modifier: Modifier = Modifier,onTap:(NavigationRoutes) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fixed header with logo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top spacing to account for navigation bar
            Spacer(modifier = Modifier.height(32.dp))
            
            // Company logo
            Image( 
                painter = painterResource(R.drawable.companylogo), 
                contentDescription = null,
                modifier = Modifier.size(width = 240.dp, height = 80.dp)
            )

            // Space between logo and scrollable content
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Scrollable menu content with Material Design
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primary action - Commission Tags (most commonly used)
            MaterialMenuButton(
                text = NavigationRoutes.Commission.title,
                icon = Icons.Default.Label,
                description = "Commission RFID tags to assets",
                isPrimary = true
            ) { onTap(NavigationRoutes.Commission) }
            
            // Secondary actions
            MaterialMenuButton(
                text = NavigationRoutes.Inventory.title,
                icon = Icons.Default.Storage,
                description = "Perform inventory operations"
            ) { onTap(NavigationRoutes.Inventory) }
            
            MaterialMenuButton(
                text = NavigationRoutes.Hunt.title,
                icon = Icons.Default.TrackChanges,
                description = "Hunt for specific tags"
            ) { onTap(NavigationRoutes.Hunt) }
            
            MaterialMenuButton(
                text = NavigationRoutes.ContinuousScanning.title,
                icon = Icons.Default.QrCodeScanner,
                description = "Continuous RFID scanning"
            ) { onTap(NavigationRoutes.ContinuousScanning) }
            
            MaterialMenuButton(
                text = NavigationRoutes.Lookup.title,
                icon = Icons.Default.Info,
                description = "Lookup tag information"
            ) { onTap(NavigationRoutes.Lookup) }

            // Bottom spacing for scrollable content
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    RainRentalRfidTheme {
        HomeScreen(
            modifier = Modifier,
            onTap = {}
        )
    }
}