package org.rainrental.rainrentalrfid.continuousScanning.presentation

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme

@Composable
fun RfidScanningAnimation(
    text: String, 
    rssi: Double, 
    completion: Float, 
    tid: String? = null,
    epcFilter: String? = null,
    receivedEpc: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    
    // Animate the scanning pulse - made faster (reduced from 1500ms to 800ms)
    val scanPulse by infiniteTransition.animateValue(
        initialValue = 0.3f,
        targetValue = 1.0f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scanPulse"
    )
    
    // Animate the tag detection pulse - made faster (reduced from 800ms to 400ms)
    val tagPulse by infiniteTransition.animateValue(
        initialValue = 0.5f,
        targetValue = 1.0f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "tagPulse"
    )

    val animatedCompletion by animateFloatAsState(
        targetValue = completion,
        animationSpec = tween(durationMillis = 300), label = "Animate continuous scanning completion"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // Add vertical padding instead of fixed height
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // Add spacing between elements
    ) {
        // Scanning waves - made larger
        Box(
            modifier = Modifier.size(200.dp), // Increased from 160dp to 200dp
            contentAlignment = Alignment.Center
        ) {
            // Outer scanning ring - made larger
            Box(
                modifier = Modifier
                    .size(180.dp) // Increased from 140dp to 180dp
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f * scanPulse),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
            )
            
            // Middle scanning ring - made larger
            Box(
                modifier = Modifier
                    .size(130.dp) // Increased from 100dp to 130dp
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f * scanPulse),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
            )
            
            // Inner scanning ring - made larger
            Box(
                modifier = Modifier
                    .size(80.dp) // Increased from 60dp to 80dp
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f * scanPulse),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
            )
            
            // Center RFID icon - made larger
            Box(
                modifier = Modifier
                    .size(50.dp) // Increased from 40dp to 50dp
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f * tagPulse),
                        shape = CircleShape
                    )
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = "RFID Scanner",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(30.dp) // Increased from 24dp to 30dp
                )
            }
        }
        
        // Tag count display - made larger
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium, // Changed from displaySmall to displayMedium
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        
        // RSSI indicator - made larger
        Text(
            text = "RSSI: ${String.format("%.1f", rssi)} dBm",
            style = MaterialTheme.typography.bodyMedium, // Changed from bodySmall to bodyMedium
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
        
        // TID display if available - made larger
        tid?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall, // Changed from labelSmall to bodySmall
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp, // Increased from 12sp to 14sp
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // EPC Filter display if available
        epcFilter?.let { filter ->
            Text(
                text = "Filter: $filter",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Received EPC display if available - shown underneath filter in different color
        receivedEpc?.let { epc ->
            Text(
                text = "EPC: $epc",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RfidScanningAnimationPreview() {
    RainRentalRfidTheme {
        RfidScanningAnimation(
            text = "42",
            rssi = -45.5,
            completion = 0.75f,
            tid = "E2003412010200000000000001",
            epcFilter = "11110000000000001100",
            receivedEpc = "111100000000000011001234567890ABCD"
        )
    }
}
