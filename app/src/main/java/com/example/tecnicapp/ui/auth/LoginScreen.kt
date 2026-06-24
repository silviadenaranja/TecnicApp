package com.example.tecnicapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val forgotState = vm.forgotState
    var showForgotDialog by remember { mutableStateOf(false) }

    if (state.isLoggedIn) {
        LaunchedEffect(Unit) {
            onLoggedIn()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = vm::onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )

            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.error ?: "", color = Color.Red)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { vm.login() }) {
                        Text("Iniciar sesión")
                    }
                    TextButton(onClick = { vm.register() }) {
                        Text("Registrarse")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = {
                    vm.clearForgotState()
                    showForgotDialog = true
                }) {
                    Text("¿Olvidaste la contraseña?")
                }
            }
        }

        if (showForgotDialog) {
            ForgotPasswordDialog(
                onDismiss = { showForgotDialog = false },
                onSendReset = { email -> vm.sendReset(email) },
                isLoading = forgotState.isLoading,
                error = forgotState.error,
                success = forgotState.success
            )
        }
    }
}
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSendReset: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    success: Boolean
) {
    var email by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recuperar contraseña") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    singleLine = true
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = Color.Red)
                }
                if (success) {
                    Spacer(Modifier.height(8.dp))
                    Text("Correo de recuperación enviado.")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isLoading && email.isNotBlank(),
                onClick = { onSendReset(email) }
            ) {
                Text("Enviar enlace")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}