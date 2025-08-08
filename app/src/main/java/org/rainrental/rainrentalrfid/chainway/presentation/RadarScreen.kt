package org.rainrental.rainrentalrfid.chainway.presentation


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.rainrental.rainrentalrfid.chainway.data.TagWithOrientation
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarScreen(detectedTags: List<TagWithOrientation>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f

        // Draw concentric circles for range
        for (i in 1..4) {
            drawCircle(
                color = Color.Gray,
                radius = radius * i / 4,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Plot each detected tag
        detectedTags.forEach { tagWithOrientation ->
            val relativeDistance = calculateDistanceFromRssi(tagWithOrientation.tag.rssi.toDouble())
            val angle = tagWithOrientation.orientation.azimuth

            if (angle in -10f..10f) { // Limit to a 20-degree range
                val x = center.x + relativeDistance * cos(Math.toRadians(angle.toDouble())).toFloat()
                val y = center.y + relativeDistance * sin(Math.toRadians(angle.toDouble())).toFloat()

                drawCircle(
                    color = Color.Red,
                    radius = 10f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

fun calculateDistanceFromRssi(rssi: Double): Float {
    // Convert RSSI to a relative distance for plotting on radar
    return (100 - rssi.toInt()) / 2f // Example conversion; adjust as needed
}