package org.rainrental.rainrentalrfid.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.rainrental.rainrentalrfid.R
import org.rainrental.rainrentalrfid.auth.AuthState
import org.rainrental.rainrentalrfid.auth.AuthViewModel
import org.rainrental.rainrentalrfid.inputmanager.domain.use_case.ScanInvitationCodeUseCase
import org.rainrental.rainrentalrfid.result.InputError
import org.rainrental.rainrentalrfid.result.Result

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showScanner by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf("") }
    
    // Handle authentication state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                onAuthenticated()
            }
            is AuthState.Error -> {
                // Error is handled by showing error message
            }
            else -> {
                // Loading or NotAuthenticated - stay on this screen
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Title
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Instructions
        Text(
            text = "Scan the invitation QR code to authenticate this device",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Device Serial Info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Device Information",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Serial: ${org.rainrental.rainrentalrfid.app.deviceSerial.ifEmpty { "Unknown" }}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Manual Entry Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Manual Entry",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = scannedCode,
                    onValueChange = { scannedCode = it },
                    label = { Text("Enter invitation code") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                Button(
                    onClick = {
                        if (scannedCode.isNotBlank()) {
                            viewModel.validateInvitation(scannedCode)
                        }
                    },
                    enabled = scannedCode.isNotBlank() && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Authenticate")
                }
            }
        }
        
        // Scan QR Code Button
        Button(
            onClick = { showScanner = true },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan QR Code")
        }
        
        // Hardware trigger hint
        Text(
            text = "Or press the trigger or side buttons to scan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Error Message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Authentication Failed",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    TextButton(
                        onClick = { 
                            viewModel.resetAuth()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
    
    // QR Scanner Dialog
    if (showScanner) {
        QRScannerDialog(
            onCodeScanned = { code ->
                scannedCode = code
                showScanner = false
                viewModel.validateInvitation(code)
            },
            onDismiss = { showScanner = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun QRScannerDialog(
    onCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: AuthViewModel
) {
    var isScanning by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            when (val result = viewModel.scanInvitationCode()) {
                is Result.Success -> {
                    onCodeScanned(result.data)
                }
                is Result.Error -> {
                    when (result.error) {
                        InputError.NoBarcode -> errorMessage = "No QR code detected"
                        InputError.HardwareError -> errorMessage = "Scanner hardware error"
                        InputError.LifecycleError -> errorMessage = "Scanner lifecycle error"
                        else -> errorMessage = "Scanner error: ${result.error}"
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Scanner error: ${e.message}"
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan QR Code") },
        text = {
            Column {
                if (isScanning) {
                    Text("Position the QR code within the scanner view")
                }
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

 