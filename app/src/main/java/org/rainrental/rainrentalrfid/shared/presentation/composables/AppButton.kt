package org.rainrental.rainrentalrfid.shared.presentation.composables

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme
import org.rainrental.rainrentalrfid.ui.theme.cornerRadius

@Composable
fun AppButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp)
            .border(width = 3.dp,shape = RoundedCornerShape(cornerRadius), color = MaterialTheme.colorScheme.primary)
            .clickable { onClick() }
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            icon?.let{
                Icon(imageVector = icon, tint = MaterialTheme.colorScheme.primary, contentDescription = null, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.weight(1f))
            }

            Text(text = text.uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            icon?.let{
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = icon, tint = MaterialTheme.colorScheme.primary, contentDescription = null, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}


@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppButtonPreview(modifier: Modifier = Modifier) {
    RainRentalRfidTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            AppButton("A button"){

            }
            AppButton("A button", modifier = Modifier.padding(vertical = 8.dp), icon = Icons.Default.Done){

            }
        }
    }
}