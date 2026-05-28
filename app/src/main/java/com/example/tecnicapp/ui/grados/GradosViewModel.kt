package com.example.tecnicapp.ui.grados

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tecnicapp.data.FirestoreRepository
import com.example.tecnicapp.model.Grado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.tecnicapp.data.importarTecnicasDesdePdf   // <-- IMPORTANTE


data class GradosUiState(
    val grados: List<Grado> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GradosViewModel(
    private val repo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradosUiState())
    val uiState: StateFlow<GradosUiState> = _uiState

    init {
        loadGrados()
    }

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

    // NUEVO: lanzar importador desde el ViewModel
  // fun importarTecnicas() {
  //     viewModelScope.launch {
  //         try {
  //             android.util.Log.d("IMPORT", "Lanzando importarTecnicasDesdePdf() desde ViewModel")
  //             _uiState.value = _uiState.value.copy(isLoading = true, error = null)
  //             importarTecnicasDesdePdf()
  //             // opcional: recargar grados o técnicas globales si te hace falta
  //             _uiState.value = _uiState.value.copy(isLoading = false)
  //         } catch (e: Exception) {
  //             _uiState.value = _uiState.value.copy(
  //                 isLoading = false,
  //                 error = e.message ?: "Error importando técnicas"
  //             )
  //         }
  //     }
  // }
}