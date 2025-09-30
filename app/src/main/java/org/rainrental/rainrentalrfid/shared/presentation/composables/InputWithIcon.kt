package org.rainrental.rainrentalrfid.shared.presentation.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme


@Composable
fun InputWithIcon(
    modifier: Modifier = Modifier,
    withError: String? = null,
    withIcon: ImageVector? = null,
    withResourceIcon: Int? = null,
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fill: Boolean = true,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    iconSize: Int = 64,
    isElevated: Boolean = true
){
    Column(
        modifier = modifier
            .then(if (fill) Modifier.fillMaxSize() else Modifier)
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Material Design 3 Card container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isElevated) 8.dp else 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon with Material Design styling
                withIcon?.let {
                    Icon(
                        imageVector = withIcon, 
                        contentDescription = text, 
                        tint = iconColor, 
                        modifier = Modifier.size(iconSize.dp)
                    )
                }
                withResourceIcon?.let {
                    Icon(
                        painter = painterResource(id = withResourceIcon), 
                        contentDescription = text, 
                        tint = iconColor,
                        modifier = Modifier.size(iconSize.dp)
                    )
                }

                // Error message with Material Design styling
                withError?.let {
                    Text(
                        text = it, 
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }

                // Main instruction text with Material Design typography
                Text(
                    text = text, 
                    textAlign = TextAlign.Center, 
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun RequestInputPreview(){
    RainRentalRfidTheme {
        Column {
            InputWithIcon(
                text = "RFID input",
                withIcon = Icons.Default.Sensors,
            )

        }
    }
}

@Composable
@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun RequestInputPreviewBarcode(){
    RainRentalRfidTheme {
        Column {
            InputWithIcon(
                text = "Barcode input",
                withResourceIcon = R.drawable.barcode,
            )

        }
    }
}