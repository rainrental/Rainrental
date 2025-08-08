package org.rainrental.rainrentalrfid.shared.presentation.composables

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.rainrental.rainrentalrfid.ui.theme.RainRentalRfidTheme

@Composable
fun LoadingWithText(
    modifier: Modifier = Modifier,
    linearProgress: Float? = null,
    text:String
){
    Box(modifier = modifier
        .fillMaxSize()
    ){
        Column(
            modifier = modifier
                .fillMaxSize()

            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(visible = linearProgress != null && linearProgress > 0f, enter = fadeIn(), exit = fadeOut()) {
                LinearProgressIndicator(
                    progress = { linearProgress ?: 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                )
            }
            AnimatedVisibility(visible = linearProgress == null || linearProgress == 0f, enter = fadeIn(), exit = fadeOut()) {
                CircularProgressIndicator(modifier = Modifier.size(50.dp))
            }
            Text(text,modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), textAlign = TextAlign.Center)
        }
    }
}

@Preview(widthDp = 360, heightDp = 640, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoadingWithTextPreview(modifier: Modifier = Modifier) {

    RainRentalRfidTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            LoadingWithText(text = "Downloading", linearProgress = 0.7f)
        }
    }
}