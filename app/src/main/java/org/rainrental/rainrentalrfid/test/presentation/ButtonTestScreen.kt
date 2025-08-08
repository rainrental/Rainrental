package org.rainrental.rainrentalrfid.test.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ButtonTestScreen(
    viewModel: ButtonTestViewModel = hiltViewModel()
) {
    val triggerState by viewModel.triggerState.collectAsState()
    val sideState by viewModel.sideState.collectAsState()
    val auxState by viewModel.auxState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Button Test Screen",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Trigger Button
        ButtonBox(
            title = "Trigger Button",
            state = triggerState,
            color = Color.Red
        )

        // Side Button
        ButtonBox(
            title = "Side Button",
            state = sideState,
            color = Color.Blue
        )

        // Aux Button
        ButtonBox(
            title = "Aux Button",
            state = auxState,
            color = Color.Green
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Press the hardware buttons to see visual feedback",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ButtonBox(
    title: String,
    state: ButtonState,
    color: Color
) {
    val backgroundColor = when (state) {
        ButtonState.UP -> Color.LightGray
        ButtonState.DOWN -> color
    }

    val borderColor = when (state) {
        ButtonState.UP -> Color.Gray
        ButtonState.DOWN -> color
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (state == ButtonState.DOWN) Color.White else Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = state.name,
            fontSize = 14.sp,
            color = if (state == ButtonState.DOWN) Color.White else Color.Black
        )
    }
} 