package org.rainrental.rainrentalrfid.shared.presentation.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme

@Composable
fun MaterialMenuButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    description: String? = null,
    subtitle: String? = null,
    isPrimary: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (subtitle != null) 88.dp else if (isPrimary) 80.dp else 72.dp),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPrimary) 8.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = description ?: text,
                    modifier = Modifier.size(24.dp),
                    tint = if (isPrimary) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isPrimary) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPrimary) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MaterialMenuButtonPreview() {
    RainRentalRfidTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MaterialMenuButton(
                text = "Commission Tags",
                icon = Icons.Default.Tag,
                subtitle = "Attach RFID tags to assets",
                isPrimary = true,
                onClick = {}
            )
            
            MaterialMenuButton(
                text = "Inventory",
                icon = Icons.Default.Tag,
                subtitle = "Manage and track inventory",
                onClick = {}
            )
            
            MaterialMenuButton(
                text = "Hunt Tags",
                icon = Icons.Default.Tag,
                subtitle = "Find specific RFID tags",
                onClick = {}
            )
        }
    }
}
