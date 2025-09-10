package org.rainrental.rainrentalrfid.commission.presentation

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme

@Composable
fun FieldValue(
    modifier: Modifier = Modifier,
    header:String,
    value:String,
    size: Int = 130,
    iconSize: Int = 20,
    hasAction: Boolean = false,
    equalFontSize: Boolean = false,
    actionIcon: ImageVector = Icons.Default.Build,
    actionIconColour: Color = MaterialTheme.colorScheme.primary,
    borderColour: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .width(size.dp)
            .padding(horizontal = 4.dp)
            .border(
                color = borderColour,
                shape = RoundedCornerShape(5.dp),
                width = 2.dp
            )
            .padding(2.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {

            Column(
//                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    header,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    value,
                    style = if (equalFontSize) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (hasAction){
                IconButton(onClick = {onClick()}) {
                    Icon(imageVector = actionIcon, contentDescription = null, tint = actionIconColour, modifier = Modifier.size(iconSize.dp))
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FieldValuePreview(modifier: Modifier = Modifier) {
    RainRentalRfidTheme {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        FieldValue(header = "Something", value = "asdkjad", hasAction = true)
        FieldValue(header = "Something", value = "asdkjad", hasAction = false)
    }
    }
}