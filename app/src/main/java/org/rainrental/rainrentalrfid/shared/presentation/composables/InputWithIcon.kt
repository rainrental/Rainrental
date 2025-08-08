package org.rainrental.rainrentalrfid.shared.presentation.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
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
    text:String,
    textColor: Color = MaterialTheme.colorScheme.primary,
    fill:Boolean = true,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    iconSize: Int = 50,
){
    Column(
        modifier = modifier
            .then(if (fill) Modifier.fillMaxSize() else Modifier)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        withIcon?.let {
            Icon(imageVector = withIcon, contentDescription = text, tint = iconColor, modifier = Modifier
                .padding(top = 8.dp)
                .size(iconSize.dp))
        }
        withResourceIcon?.let {
            Icon(painter = painterResource(id = withResourceIcon), contentDescription = text , tint = iconColor,modifier = Modifier
                .padding(top = 8.dp)
                .size(iconSize.dp))
        }
        withError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontStyle = FontStyle.Italic)
        }

        Text(text, textAlign = TextAlign.Center, color = textColor, modifier = Modifier.padding(bottom = 8.dp))

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