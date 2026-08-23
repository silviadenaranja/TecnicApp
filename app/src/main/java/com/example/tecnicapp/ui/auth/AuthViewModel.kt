package com.example.tecnicapp.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tecnicapp.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val uid: String = "",
    val error: String? = null
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isLoggedIn = auth.currentUser != null,
            uid = auth.currentUser?.uid.orEmpty()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        android.util.Log.d(
            "TECNIAPP_AUTH",
            "Auth cambiado: loggedIn=${user != null}, uid=${user?.uid}"
        )
        _uiState.value = _uiState.value.copy(
            isLoggedIn = user != null,
            uid = user?.uid.orEmpty(),
            isLoading = false
        )
    }

    data class ForgotPasswordUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    )

    var forgotState by mutableStateOf(ForgotPasswordUiState())
        private set

    data class ChangePasswordUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    )

    var changePassState by mutableStateOf(ChangePasswordUiState())
        private set

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authStateListener)
        super.onCleared()
    }

    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(
            email = newEmail,
            error = null
        )
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(
            password = newPassword,
            error = null
        )
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Email y contraseña son obligatorios"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val result = repo.login(email, password)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "Error al iniciar sesión"
                )
            }

            /*
             * Si funciona, AuthStateListener hará isLoggedIn = true.
             * No debemos forzarlo aquí.
             */
        }
    }

    fun register() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Email y contraseña son obligatorios"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val result = repo.register(email, password)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "Error al registrarse"
                )
            }

            /*
             * Si funciona, AuthStateListener hará isLoggedIn = true.
             */
        }
    }

    fun sendReset(email: String) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank()) {
            forgotState = ForgotPasswordUiState(
                error = "Introduce un correo electrónico"
            )
            return
        }

        viewModelScope.launch {
            forgotState = ForgotPasswordUiState(isLoading = true)

            val result = repo.sendPasswordReset(cleanEmail)

            forgotState = if (result.isSuccess) {
                ForgotPasswordUiState(success = true)
            } else {
                ForgotPasswordUiState(
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "Error enviando el correo"
                )
            }
        }
    }

    fun clearForgotState() {
        forgotState = ForgotPasswordUiState()
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            changePassState = ChangePasswordUiState(
                error = "Debes completar todos los campos"
            )
            return
        }

        viewModelScope.launch {
            changePassState = ChangePasswordUiState(isLoading = true)

            val result = repo.changePassword(
                currentPassword = currentPassword,
                newPassword = newPassword
            )

            changePassState = if (result.isSuccess) {
                ChangePasswordUiState(success = true)
            } else {
                ChangePasswordUiState(
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "Error cambiando contraseña"
                )
            }
        }
    }

    fun clearChangePasswordState() {
        changePassState = ChangePasswordUiState()
    }

    fun logout() {
        repo.logout()

        /*
         * AuthStateListener recibe currentUser = null y pondrá
         * isLoggedIn = false.
         */
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}