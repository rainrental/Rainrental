package org.rainrental.rainrentalrfid.home.presentation

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Settings
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
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme

@Composable
fun HomeScreen(
    navController: NavController,
) {
    HomeScreen(modifier = Modifier, onTap = { destination ->
        navController.navigate(destination.route)
    })
}

@Composable
private fun HomeScreen(modifier: Modifier = Modifier,onTap:(NavigationRoutes) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)

        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val buttonSpacing = 26.dp
        Spacer(modifier = Modifier.height(16.dp))
        Image( painter = painterResource(R.drawable.companylogo), contentDescription = null,modifier = Modifier.size(width = 240.dp, height = 80.dp))

        Spacer(modifier = Modifier.weight(1f))

        AppButton(text = NavigationRoutes.Commission.title.uppercase(), icon = Icons.Default.Tag ) { onTap(NavigationRoutes.Commission) }
        Spacer(modifier = Modifier.height(buttonSpacing))
        AppButton(text = NavigationRoutes.Inventory.title.uppercase(), icon = Icons.Default.Numbers ) { onTap(NavigationRoutes.Inventory) }
        Spacer(modifier = Modifier.height(buttonSpacing))
        AppButton(text = NavigationRoutes.Hunt.title.uppercase(), icon = Icons.Default.Radar ) { onTap(NavigationRoutes.Hunt) }
        Spacer(modifier = Modifier.height(buttonSpacing))
        AppButton(text = NavigationRoutes.ContinuousScanning.title.uppercase(), icon = Icons.Default.Search ) { onTap(NavigationRoutes.ContinuousScanning) }
        Spacer(modifier = Modifier.height(buttonSpacing))
        AppButton(text = NavigationRoutes.Lookup.title.uppercase(), icon = Icons.Default.Search ) { onTap(NavigationRoutes.Lookup) }

        Spacer(modifier = Modifier.weight(1f))
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