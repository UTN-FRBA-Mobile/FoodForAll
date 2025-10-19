package ar.edu.utn.frba.mobile.foodforall.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ar.edu.utn.frba.mobile.foodforall.ui.viewmodel.AuthViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class AuthMode {
    LOGIN,
    REGISTER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    initialMode: AuthMode = AuthMode.LOGIN
) {
    var authMode by remember { mutableStateOf(initialMode) }
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onDismiss()
        }
    }

    LaunchedEffect(authMode) {
        authViewModel.clearError()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        when (authMode) {
            AuthMode.LOGIN -> {
                LoginScreen(
                    uiState = uiState,
                    onLoginClick = { email, password ->
                        authViewModel.login(email, password)
                    },
                    onRegisterClick = {
                        authMode = AuthMode.REGISTER
                    },
                    onDismiss = onDismiss
                )
            }
            AuthMode.REGISTER -> {
                RegisterScreen(
                    uiState = uiState,
                    onRegisterClick = { email, password, fullName, username, restrictions ->
                        authViewModel.register(email, password, fullName, username, restrictions)
                    },
                    onLoginClick = {
                        authMode = AuthMode.LOGIN
                    },
                    onDismiss = onDismiss
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
