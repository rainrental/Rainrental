package org.rainrental.rainrentalrfid.hunt.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme

@Composable
fun DBMeter(
    lastRssi: Double,
    width : Dp = 200.dp,
    height: Dp = 20.dp,
    minRssi: Int = -90,
    maxRssi: Int = -20,
    featherSize: Float = 0.1f,
    rotated: Boolean = true,
    autoHide : Boolean = true
) {
    val rssi = lastRssi
    val signalStrength = if (rssi <= minRssi) 0f else ((rssi - minRssi).toFloat() / (maxRssi - minRssi)).coerceIn(0f, 1f)
    var isVisible by remember { mutableStateOf(true) }
    val opacity = remember { Animatable(1f) }

    LaunchedEffect(lastRssi) {
        isVisible = true
        opacity.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 20) // Adjust fade-out duration as needed
        )

        kotlinx.coroutines.delay(100) // Delay before starting to fade
        opacity.animateTo(
            targetValue = if (autoHide) 0f else 1f,
            animationSpec = tween(durationMillis = 1000) // Adjust fade-out duration as needed
        )
        isVisible = !autoHide
    }

    AnimatedVisibility(visible = isVisible) {
        Canvas(
            modifier = Modifier
                .width(if (rotated) height else width)
                .height(if (rotated) width else height)
                .graphicsLayer { alpha = opacity.value }
                .clip(shape = RoundedCornerShape(8.dp))
        ) {
            val peakMarkerBrush = Brush.verticalGradient(
                colors = listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Blue,
                    Color.Cyan,
                    Color.Magenta
                )
            )
            val fullGradientBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Yellow,
                    Color.Green,
                    Color.Green,
                    Color.Green
                )
            )
            val fullGradientBrushRotated = Brush.verticalGradient(
                colors = listOf(
                    Color.Green,
                    Color.Green,
                    Color.Green,
                    Color.Yellow,
                    Color.Yellow,
                    Color.Red
                )
            )
            drawRect(
                brush = if (rotated) fullGradientBrushRotated else fullGradientBrush,
                size = Size(size.width, size.height)
            )

            // Calculate feathering start point
            val featherStart =
                if (rotated) ((size.height * signalStrength)).coerceAtLeast(0f) else (size.width * signalStrength).coerceAtLeast(
                    0f
                )
            val featherEnd =
                if (rotated) (featherStart + size.height * featherSize).coerceAtMost(size.height) else (featherStart + size.width * featherSize).coerceAtMost(
                    size.width
                )


            if (signalStrength < 1f) {
                val blackOverlayBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black
                    ),
                    startX = featherStart,
                    endX = featherEnd
                )
                val blackOverlayBrushRotated = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black,
                        Color.Black
                    ),
                    startY = size.height,
                    endY = featherEnd
                )
                if (rotated) {
                    drawRect(
                        brush = blackOverlayBrushRotated,
                        size = Size(size.width, size.height - featherStart),
                        topLeft = Offset(0f, 0f)
                    )
                    drawRect(
                        brush = Brush.verticalGradient(colors = listOf(Color.White,Color.White)),
                        size = Size(size.width - 4,2f),
                        topLeft = Offset(2f,size.height - featherStart)
                    )

                } else {
                    drawRect(
                        brush = blackOverlayBrush,
                        size = Size(size.width - featherStart, size.height),
                        topLeft = Offset(featherStart, 0f)
                    )
                    drawRect(
                        brush = Brush.verticalGradient(colors = listOf(Color.White,Color.White)),
                        size = Size(2f,size.height),
                        topLeft = Offset(featherStart,0f)

                    )

                }
            }
        }
    }
}

fun Modifier.switchHeightAndWidth(originalWidth: Dp, originalHeight: Dp): Modifier {
    // Remove the original size modifier and add a new one with switched dimensions
    return this.then(Modifier.size(width = originalHeight, height = originalWidth))
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DBMeterPreview(){

    RainRentalRfidTheme {
        DBMeter(
            lastRssi = -30.0,
            rotated = true,
            width = 140.dp,
            height = 10.dp
        )
    }
}