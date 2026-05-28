package com.example.tecnicapp.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tecnicapp.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        val currentUser = repo.currentUser()
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(isLoggedIn = true)
        }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail, error = null)
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword, error = null)
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Email y contraseña son obligatorios")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repo.login(email, password)
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun register() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Email y contraseña son obligatorios")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repo.register(email, password)
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "Error al registrarse"
                )
                _uiState.value
            }
        }
    }

    // ---------- Recuperar contraseña ----------

    data class ForgotPasswordUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    )

    var forgotState by mutableStateOf(ForgotPasswordUiState())
        private set

    fun sendReset(email: String) {
        viewModelScope.launch {
            forgotState = forgotState.copy(isLoading = true, error = null, success = false)
            val result = repo.sendPasswordReset(email)
            forgotState = if (result.isSuccess) {
                forgotState.copy(isLoading = false, success = true)
            } else {
                forgotState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Error enviando correo"
                )
            }
        }
    }

    fun clearForgotState() {
        forgotState = ForgotPasswordUiState()
    }

    // ---------- Cambio de contraseña ----------

    data class ChangePasswordUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    )

    var changePassState by mutableStateOf(ChangePasswordUiState())
        private set

    fun changePassword(current: String, nuevo: String) {
        viewModelScope.launch {
            changePassState = ChangePasswordUiState(isLoading = true)
            val result = repo.changePassword(current, nuevo)
            changePassState = if (result.isSuccess) {
                ChangePasswordUiState(success = true)
            } else {
                ChangePasswordUiState(
                    error = result.exceptionOrNull()?.localizedMessage ?: "Error cambiando contraseña"
                )
            }
        }
    }

    fun clearChangePasswordState() {
        changePassState = ChangePasswordUiState()
    }

    // ---------- Logout y limpieza ----------

    fun logout() {
        repo.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}