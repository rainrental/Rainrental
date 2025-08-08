package org.rainrental.rainrentalrfid.chainway.presentation

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin


data class RfidEvent(
    val tagId: String,
    val rssi: Double,
    val timestamp: Long
)

@Composable
fun RfidPlotScreen(
    events: List<RfidEvent>,
    deviceRotation: Float,
    modifier: Modifier = Modifier,
    fadeDurationMillis: Int = 3000,
    dwellTimeMillis: Int = 5000
) {
    val currentTime = System.currentTimeMillis()

    // Filter and fade events
    val fadedEvents = remember(events, currentTime) {
        events.filter { currentTime - it.timestamp <= dwellTimeMillis }
    }

    // Canvas to draw the plot
    Canvas(modifier = modifier) {
        val radiusStep = size.minDimension / 2 / 96 // Step size for each dB ring
        val center = Offset(size.width / 2, size.height / 2)

        // Draw concentric rings
        for (i in 0..96 step 16) {
            drawCircle(
                color = Color.Gray,
                radius = radiusStep * i,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw events
        fadedEvents.forEach { event ->
            val fadeProgress = (currentTime - event.timestamp).toFloat() / fadeDurationMillis
            val alpha = 1f - fadeProgress.coerceIn(0f, 1f)
            val distance = (1 - event.rssi / -96) * size.minDimension / 2
            val angle = Math.toRadians(deviceRotation.toDouble())
            val x = center.x + cos(angle) * distance
            val y = center.y - sin(angle) * distance

            drawCircle(
                color = Color.Red.copy(alpha = alpha),
                radius = 5.dp.toPx(),
                center = Offset(x.toFloat(), y.toFloat())
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RfidPlotScreenPreview(

) {
    RfidPlotScreen(
        events = listOf(
            RfidEvent(
                tagId = "test",
                rssi = -34.3,
                timestamp = System.currentTimeMillis()
            )
        ),
        deviceRotation = 0.5f
    )
}