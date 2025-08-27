package org.rainrental.rainrentalrfid.hunt.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.rainrental.rainrentalrfid.shared.presentation.composables.AppButton

@Composable
fun ManualBarcodeEntry(
    onLookup: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var barcodeText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Or enter barcode manually:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = barcodeText,
            onValueChange = { barcodeText = it },
            label = { Text("Barcode") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (barcodeText.isNotBlank()) {
                        onLookup(barcodeText.trim())
                    }
                }
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AppButton(
            text = "Lookup",
            onClick = {
                if (barcodeText.isNotBlank()) {
                    onLookup(barcodeText.trim())
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
