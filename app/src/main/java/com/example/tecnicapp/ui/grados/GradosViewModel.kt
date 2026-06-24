package com.example.tecnicapp.ui.grados

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tecnicapp.data.FirestoreRepository
import com.example.tecnicapp.data.Friend
import com.example.tecnicapp.data.FriendsRepository
import com.example.tecnicapp.model.Grado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
// import com.example.tecnicapp.data.importarTecnicasDesdePdf   // si lo usas

data class GradosUiState(
    val grados: List<Grado> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class FriendsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val friends: List<Friend> = emptyList()
)

class GradosViewModel(
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val friendsRepo: FriendsRepository = FriendsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradosUiState())
    val uiState: StateFlow<GradosUiState> = _uiState

    var friendsState by mutableStateOf(FriendsUiState())
        private set

    init {
        loadGrados()
    }

    // ---------- GRADOS ----------

    fun loadGrados() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repo.getGrados()
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    grados = result.getOrDefault(emptyList()),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun addGrado(nombre: String) {
        viewModelScope.launch {
            val trimmed = nombre.trim()
            if (trimmed.isEmpty()) return@launch

            val result = repo.addGrado(trimmed)
            if (result.isSuccess) {
                loadGrados()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun updateGrado(grado: Grado, nuevoNombre: String) {
        viewModelScope.launch {
            val trimmed = nuevoNombre.trim()
            if (trimmed.isEmpty()) return@launch

            val result = repo.updateGrado(grado.id, trimmed)
            if (result.isSuccess) {
                loadGrados()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun deleteGrado(grado: Grado) {
        viewModelScope.launch {
            val result = repo.deleteGrado(grado.id)
            if (result.isSuccess) {
                loadGrados()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Si quieres reactivar importar desde PDF:
    /*
    fun importarTecnicas() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                importarTecnicasDesdePdf()
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error importando técnicas"
                )
            }
        }
    }
    */

    // ---------- AMIGOS ----------

    fun loadFriends() {
        viewModelScope.launch {
            friendsState = friendsState.copy(isLoading = true, error = null)
            val result = friendsRepo.getFriends()
            friendsState = if (result.isSuccess) {
                friendsState.copy(
                    isLoading = false,
                    friends = result.getOrNull().orEmpty()
                )
            } else {
                friendsState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun addFriend(uidAmigo: String, nombre: String) {
        viewModelScope.launch {
            val trimmedUid = uidAmigo.trim()
            val trimmedName = nombre.trim()
            if (trimmedUid.isEmpty() || trimmedName.isEmpty()) return@launch

            val result = friendsRepo.addFriend(trimmedUid, trimmedName)
            if (result.isSuccess) {
                loadFriends()
            } else {
                friendsState = friendsState.copy(
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun sendMyDataToFriend(friendUid: String) {
        viewModelScope.launch {
            friendsState = friendsState.copy(isLoading = true, error = null)
            val currentUid = repo.currentUserUid()
            if (currentUid.isBlank()) {
                friendsState = friendsState.copy(
                    isLoading = false,
                    error = "No hay usuario autenticado"
                )
                return@launch
            }

            val result = repo.copyGradosAndTecnicasToUser(currentUid, friendUid)
            friendsState = if (result.isSuccess) {
                friendsState.copy(isLoading = false)
            } else {
                friendsState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }
}