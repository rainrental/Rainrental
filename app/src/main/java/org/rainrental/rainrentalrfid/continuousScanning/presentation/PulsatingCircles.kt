package org.rainrental.rainrentalrfid.continuousScanning.presentation

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme


@Composable
fun PulsatingCircles(text: String, rssi: Double, completion: Float, tid: String? = null) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val size by infiniteTransition.animateValue(
        initialValue = 220.dp,
        targetValue = 190.dp,
        Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Reverse"
    )
    val smallCircle by infiniteTransition.animateValue(
        initialValue = 150.dp,
        targetValue = 180.dp,
        Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulsatingCircles"
    )

    val animatedCompletion by animateFloatAsState(
        targetValue = completion,
        animationSpec = tween(durationMillis = 300), label = "Animate continuous scanning completion" // was 500
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularCountdownRing(
            size = size, // Outer ring size
            completion = animatedCompletion,
            color = MaterialTheme.colorScheme.primary
        )
        SimpleCircleShape2(
            size = size,
            color = MaterialTheme.colorScheme.background
        )
        SimpleCircleShape2(
            size = size,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
        SimpleCircleShape2(
            size = smallCircle,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
        SimpleCircleShape2(
            size = 130.dp,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
    tid?.let {

        Box(modifier = Modifier, contentAlignment = Alignment.BottomCenter) {
            Text(
                modifier = Modifier.padding(bottom = 20.dp),
                text = tid,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CircularCountdownRing(
    size: Dp,
    completion: Float,
    color: Color,
    strokeWidth: Dp = 10.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .wrapContentSize(Alignment.Center)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360 * completion,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun SimpleCircleShape2(
    size: Dp,
    color: Color = Color.White,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.LightGray.copy(alpha = 0.0f),
) {
    Column(
        modifier = Modifier
            .wrapContentSize(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    color
                )
                .border(borderWidth, borderColor)
        )
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewPulsatingCircles() {
    RainRentalRfidTheme {
        PulsatingCircles(
            text = "12",
            rssi = -54.3,
            completion = 0.31f,
            tid = "E243FFEEDDEEE243FFEEDDEE"
        )
    }
} 