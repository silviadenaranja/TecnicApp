package com.example.tecnicapp.ui.tecnicas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tecnicapp.model.Tecnica
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TecnicasUiState(
    val isLoading: Boolean = true,
    val tecnicas: List<Tecnica> = emptyList(),
    val error: String? = null
)

class TecnicasViewModel(
    private val gradoId: String,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private fun tecnicasCollection() =
        auth.currentUser?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("grados")
                .document(gradoId)
                .collection("tecnicas")
        } ?: throw IllegalStateException("Usuario no autenticado")

    private val _uiState = MutableStateFlow(TecnicasUiState())
    val uiState: StateFlow<TecnicasUiState> = _uiState

    init {
        cargarTecnicas()
    }

    private fun cargarTecnicas() {
        viewModelScope.launch {
            try {
                android.util.Log.d("TEC", "gradoId=$gradoId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val snapshot = tecnicasCollection()
                    .orderBy("orden")
                    .get()
                    .await()

                val lista = snapshot.documents.map { doc ->
                    Tecnica(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        alias = doc.getString("alias") ?: "",
                        posicion = doc.getString("posicion") ?: "",
                        atacante = doc.getString("atacante") ?: "",
                        defensor = doc.getString("defensor") ?: "",
                        altura = doc.getString("altura") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        orden = (doc.getLong("orden") ?: 0L).toInt()
                    )
                }

                _uiState.value = TecnicasUiState(
                    isLoading = false,
                    tecnicas = lista,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = TecnicasUiState(
                    isLoading = false,
                    tecnicas = emptyList(),
                    error = e.message ?: "Error cargando técnicas"
                )
            }
        }
    }

    fun addTecnica(
        nombre: String,
        alias: String,
        posicion: String,
        atacante: String,
        defensor: String,
        altura: String,
        descripcion: String
    ) {
        viewModelScope.launch {
            try {
                val nuevaOrden = (_uiState.value.tecnicas.maxOfOrNull { it.orden } ?: -1) + 1

                val data = hashMapOf(
                    "nombre" to nombre,
                    "alias" to alias,
                    "posicion" to posicion,
                    "atacante" to atacante,
                    "defensor" to defensor,
                    "altura" to altura,
                    "descripcion" to descripcion,
                    "orden" to nuevaOrden
                )

                val docRef = tecnicasCollection()
                    .add(data)
                    .await()

                val nueva = Tecnica(
                    id = docRef.id,
                    nombre = nombre,
                    alias = alias,
                    posicion = posicion,
                    atacante = atacante,
                    defensor = defensor,
                    altura = altura,
                    descripcion = descripcion,
                    orden = nuevaOrden
                )

                _uiState.value = _uiState.value.copy(
                    tecnicas = _uiState.value.tecnicas + nueva,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al añadir técnica"
                )
            }
        }
    }

    fun updateTecnica(
        tecnica: Tecnica,
        nombre: String,
        alias: String,
        posicion: String,
        atacante: String,
        defensor: String,
        altura: String,
        descripcion: String
    ) {
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "nombre" to nombre,
                    "alias" to alias,
                    "posicion" to posicion,
                    "atacante" to atacante,
                    "defensor" to defensor,
                    "altura" to altura,
                    "descripcion" to descripcion
                )

                tecnicasCollection()
                    .document(tecnica.id)
                    .update(data)
                    .await()

                val listaActualizada = _uiState.value.tecnicas.map {
                    if (it.id == tecnica.id) {
                        it.copy(
                            nombre = nombre,
                            alias = alias,
                            posicion = posicion,
                            atacante = atacante,
                            defensor = defensor,
                            altura = altura,
                            descripcion = descripcion
                        )
                    } else it
                }

                _uiState.value = _uiState.value.copy(
                    tecnicas = listaActualizada,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al actualizar técnica"
                )
            }
        }
    }

    fun deleteTecnica(tecnica: Tecnica) {
        viewModelScope.launch {
            try {
                tecnicasCollection()
                    .document(tecnica.id)
                    .delete()
                    .await()

                _uiState.value = _uiState.value.copy(
                    tecnicas = _uiState.value.tecnicas.filter { it.id != tecnica.id },
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al eliminar técnica"
                )
            }
        }
    }

    fun moveTecnica(fromIndex: Int, toIndex: Int) {
        val lista = _uiState.value.tecnicas.toMutableList()
        if (fromIndex !in lista.indices || toIndex !in lista.indices) return

        val item = lista.removeAt(fromIndex)
        lista.add(toIndex, item)

        val reordenada = lista.mapIndexed { index, tecnica ->
            tecnica.copy(orden = index)
        }

        _uiState.value = _uiState.value.copy(
            tecnicas = reordenada,
            error = null
        )
    }

    fun persistirOrden() {
        viewModelScope.launch {
            try {
                val batch = db.batch()

                _uiState.value.tecnicas.forEachIndexed { index, tecnica ->
                    val ref = tecnicasCollection()
                        .document(tecnica.id)

                    batch.update(ref, "orden", index)
                }

                batch.commit().await()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al guardar orden"
                )
            }
        }
    }
}