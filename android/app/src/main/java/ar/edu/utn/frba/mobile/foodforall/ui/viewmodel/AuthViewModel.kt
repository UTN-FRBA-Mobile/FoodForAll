package ar.edu.utn.frba.mobile.foodforall.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.frba.mobile.foodforall.domain.model.User
import ar.edu.utn.frba.mobile.foodforall.repository.AuthRepository
import ar.edu.utn.frba.mobile.foodforall.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val validationError: ValidationError? = null
)

data class ValidationError(
    val emailError: String? = null,
    val passwordError: String? = null,
    val fullNameError: String? = null,
    val usernameError: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val validation = validateLogin(email, password)
            if (validation != null) {
                _uiState.value = AuthUiState(validationError = validation)
                return@launch
            }

            when (val result = authRepository.login(email.trim(), password)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState()
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(error = result.message)
                }
            }
        }
    }

    fun register(
        email: String,
        password: String,
        fullName: String,
        username: String,
        dietaryRestrictions: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val validation = validateRegistration(email, password, fullName, username)
            if (validation != null) {
                _uiState.value = AuthUiState(validationError = validation)
                return@launch
            }

            when (val result = authRepository.register(
                email = email.trim(),
                password = password,
                fullName = fullName.trim(),
                username = username.trim(),
                dietaryRestrictions = dietaryRestrictions
            )) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState()
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(error = result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, validationError = null)
    }

    fun isAuthenticated(): Boolean {
        return currentUser.value != null
    }

    private fun validateLogin(email: String, password: String): ValidationError? {
        val emailError = when {
            email.isBlank() -> "El email es requerido"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
            else -> null
        }

        val passwordError = when {
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        return if (emailError != null || passwordError != null) {
            ValidationError(emailError = emailError, passwordError = passwordError)
        } else {
            null
        }
    }

    private fun validateRegistration(
        email: String,
        password: String,
        fullName: String,
        username: String
    ): ValidationError? {
        val emailError = when {
            email.isBlank() -> "El email es requerido"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
            else -> null
        }

        val passwordError = when {
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        val fullNameError = when {
            fullName.isBlank() -> "El nombre completo es requerido"
            fullName.trim().length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        }

        val usernameError = when {
            username.isBlank() -> "El username es requerido"
            !username.startsWith("@") -> "El username debe comenzar con @"
            username.length < 2 -> "El username debe tener al menos 2 caracteres (incluyendo @)"
            username.contains(" ") -> "El username no puede contener espacios"
            !username.matches(Regex("@[a-zA-Z0-9_]+")) -> "El username solo puede contener letras, números y guiones bajos"
            else -> null
        }

        return if (emailError != null || passwordError != null || fullNameError != null || usernameError != null) {
            ValidationError(
                emailError = emailError,
                passwordError = passwordError,
                fullNameError = fullNameError,
                usernameError = usernameError
            )
        } else {
            null
        }
    }
}
