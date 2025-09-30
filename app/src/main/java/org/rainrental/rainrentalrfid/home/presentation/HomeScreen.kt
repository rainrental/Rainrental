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
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            // Commission Tags
            MaterialMenuButton(
                text = NavigationRoutes.Commission.title,
                icon = Icons.Default.Label,
                description = "Commission RFID tags to assets",
                subtitle = "Attach RFID tags to assets"
            ) { onTap(NavigationRoutes.Commission) }
            
            // Other features
            MaterialMenuButton(
                text = NavigationRoutes.Inventory.title,
                icon = Icons.Default.Storage,
                description = "Perform inventory operations",
                subtitle = "Enter a barcode, count that SKU"
            ) { onTap(NavigationRoutes.Inventory) }
            
            MaterialMenuButton(
                text = NavigationRoutes.Hunt.title,
                icon = Icons.Default.TrackChanges,
                description = "Hunt for specific tags",
                subtitle = "Find specific RFID tags"
            ) { onTap(NavigationRoutes.Hunt) }
            
            MaterialMenuButton(
                text = NavigationRoutes.ContinuousScanning.title,
                icon = Icons.Default.Send,
                description = "Report all detected tags to server via MQTT",
                subtitle = "Report all tags to server"
            ) { onTap(NavigationRoutes.ContinuousScanning) }
            
            MaterialMenuButton(
                text = NavigationRoutes.Lookup.title,
                icon = Icons.Default.Info,
                description = "Lookup tag information",
                subtitle = "Get tag details and status"
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

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun HomeScreenLightPreview() {
    RainRentalRfidTheme(darkTheme = false) {
        HomeScreen(
            modifier = Modifier,
            onTap = {}
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenBlueThemePreview() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF2196F3),
            primaryContainer = Color(0xFF1976D2),
            secondary = Color(0xFF03DAC6),
            tertiary = Color(0xFFBB86FC),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onPrimaryContainer = Color.White,
            onSecondary = Color.Black,
            onTertiary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        HomeScreen(
            modifier = Modifier,
            onTap = {}
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenGreenThemePreview() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF4CAF50),
            primaryContainer = Color(0xFF388E3C),
            secondary = Color(0xFF8BC34A),
            tertiary = Color(0xFFCDDC39),
            background = Color(0xFF0D1B0D),
            surface = Color(0xFF1A2E1A),
            onPrimary = Color.White,
            onPrimaryContainer = Color.White,
            onSecondary = Color.Black,
            onTertiary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        HomeScreen(
            modifier = Modifier,
            onTap = {}
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPurpleThemePreview() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF9C27B0),
            primaryContainer = Color(0xFF7B1FA2),
            secondary = Color(0xFFE91E63),
            tertiary = Color(0xFFFF9800),
            background = Color(0xFF1A0D1A),
            surface = Color(0xFF2E1A2E),
            onPrimary = Color.White,
            onPrimaryContainer = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        HomeScreen(
            modifier = Modifier,
            onTap = {}
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenOrangeThemePreview() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF9800),
            primaryContainer = Color(0xFFF57C00),
            secondary = Color(0xFFFF5722),
            tertiary = Color(0xFFE91E63),
            background = Color(0xFF1A0D0D),
            surface = Color(0xFF2E1A1A),
            onPrimary = Color.Black,
            onPrimaryContainer = Color.Black,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        HomeScreen(
            modifier = Modifier,
            onTap = {}
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun HomeScreenLightBluePreview() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1976D2),
            primaryContainer = Color(0xFFBBDEFB),
            secondary = Color(0xFF03DAC6),
            tertiary = Color(0xFFBB86FC),
            background = Color(0xFFF5F5F5),
            surface = Color.White,
            onPrimary = Color.White,
            onPrimaryContainer = Color(0xFF0D47A1),
            onSecondary = Color.Black,
            onTertiary = Color.Black,
            onBackground = Color.Black,
            onSurface = Color.Black
        )
    ) {
        HomeScreen(
            modifier = Modifier,
            onTap = {}
        )
    }
}