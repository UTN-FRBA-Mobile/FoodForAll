package ar.edu.utn.frba.mobile.foodforall.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction
import ar.edu.utn.frba.mobile.foodforall.ui.viewmodel.AuthUiState

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegisterClick: (email: String, password: String, fullName: String, username: String, dietaryRestrictions: List<String>) -> Unit,
    onLoginClick: () -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("@") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRestrictions by remember { mutableStateOf(setOf<DietaryRestriction>()) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = uiState.validationError?.emailError != null,
            supportingText = {
                uiState.validationError?.emailError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            enabled = !uiState.isLoading
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            isError = uiState.validationError?.passwordError != null,
            supportingText = {
                uiState.validationError?.passwordError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            enabled = !uiState.isLoading
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            isError = uiState.validationError?.fullNameError != null,
            supportingText = {
                uiState.validationError?.fullNameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            enabled = !uiState.isLoading
        )

        OutlinedTextField(
            value = username,
            onValueChange = { newValue ->
                if (newValue.isEmpty()) {
                    username = "@"
                } else if (!newValue.startsWith("@")) {
                    username = "@$newValue"
                } else {
                    username = newValue
                }
            },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            isError = uiState.validationError?.usernameError != null,
            supportingText = {
                uiState.validationError?.usernameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            enabled = !uiState.isLoading
        )

        Text(
            text = "Restricciones Dietéticas (Opcional)",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DietaryRestriction.entries.filter { it != DietaryRestriction.GENERAL }.forEach { restriction ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedRestrictions.contains(restriction),
                        onCheckedChange = { checked ->
                            selectedRestrictions = if (checked) {
                                selectedRestrictions + restriction
                            } else {
                                selectedRestrictions - restriction
                            }
                        },
                        enabled = !uiState.isLoading
                    )
                    Text(
                        text = "${restriction.emoji} ${restriction.description}",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                onRegisterClick(
                    email,
                    password,
                    fullName,
                    username,
                    selectedRestrictions.map { it.key }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Registrarse")
            }
        }

        TextButton(
            onClick = onLoginClick,
            enabled = !uiState.isLoading
        ) {
            Text("¿Ya tenés cuenta? Inicia sesión")
        }

        TextButton(
            onClick = onDismiss,
            enabled = !uiState.isLoading
        ) {
            Text("Cancelar")
        }
    }
}
