// app/src/main/java/com/example/tecnicapp/ui/tecnicas/TodasTecnicasViewModel.kt

package com.example.tecnicapp.ui.tecnicas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tecnicapp.data.TecnicasGlobalRepository
import com.example.tecnicapp.data.TecnicasPorGrado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TodasTecnicasUiState(
    val bloques: List<TecnicasPorGrado> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TodasTecnicasViewModel(
    private val repo: TecnicasGlobalRepository = TecnicasGlobalRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodasTecnicasUiState())
    val uiState: StateFlow<TodasTecnicasUiState> = _uiState

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repo.getTodasTecnicas()
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    bloques = result.getOrDefault(emptyList()),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Error cargando técnicas"
                )
            }
        }
    }
}