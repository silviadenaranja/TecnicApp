package com.example.tecnicapp.ui.tecnicas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tecnicapp.model.Tecnica
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
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

    private fun currentUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")
    }

    private fun tecnicasCollection() = db.collection("users")
        .document(currentUid())
        .collection("grados")
        .document(gradoId)
        .collection("tecnicas")

    private val _uiState = MutableStateFlow(TecnicasUiState())
    val uiState: StateFlow<TecnicasUiState> = _uiState

    init {
        cargarTecnicas()
    }

    fun cargarTecnicas() {
        viewModelScope.launch {
            try {
                val uid = currentUid()

                Log.d(
                    "TECNIAPP_TECNICAS",
                    "Cargando técnicas: uid=$uid, gradoId=$gradoId"
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                val snapshot = tecnicasCollection()
                    .orderBy("orden")
                    .get(Source.SERVER)
                    .await()

                val lista = snapshot.documents.map { doc ->
                    Tecnica(
                        id = doc.id,
                        nombre = doc.getString("nombre").orEmpty(),
                        alias = doc.getString("alias").orEmpty(),
                        posicion = doc.getString("posicion").orEmpty(),
                        atacante = doc.getString("atacante").orEmpty(),
                        defensor = doc.getString("defensor").orEmpty(),
                        altura = doc.getString("altura").orEmpty(),
                        descripcion = doc.getString("descripcion").orEmpty(),
                        orden = (doc.getLong("orden") ?: 0L).toInt()
                    )
                }

                _uiState.value = TecnicasUiState(
                    isLoading = false,
                    tecnicas = lista
                )
            } catch (e: Exception) {
                Log.e(
                    "TECNIAPP_TECNICAS",
                    "Error al cargar técnicas",
                    e
                )

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
                val nuevaOrden =
                    (_uiState.value.tecnicas.maxOfOrNull { it.orden } ?: -1) + 1

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

                val nuevaTecnica = Tecnica(
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
                    tecnicas = _uiState.value.tecnicas + nuevaTecnica,
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

                _uiState.value = _uiState.value.copy(
                    tecnicas = _uiState.value.tecnicas.map { item ->
                        if (item.id == tecnica.id) {
                            item.copy(
                                nombre = nombre,
                                alias = alias,
                                posicion = posicion,
                                atacante = atacante,
                                defensor = defensor,
                                altura = altura,
                                descripcion = descripcion
                            )
                        } else {
                            item
                        }
                    },
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
                val uid = currentUid()

                Log.d(
                    "TECNIAPP_TECNICAS",
                    "Borrando técnica: uid=$uid, gradoId=$gradoId, tecnicaId=${tecnica.id}"
                )

                tecnicasCollection()
                    .document(tecnica.id)
                    .delete()
                    .await()

                _uiState.value = _uiState.value.copy(
                    tecnicas = _uiState.value.tecnicas.filter {
                        it.id != tecnica.id
                    },
                    error = null
                )
            } catch (e: Exception) {
                Log.e(
                    "TECNIAPP_TECNICAS",
                    "Error al borrar técnica",
                    e
                )

                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al eliminar técnica"
                )
            }
        }
    }

    fun moveTecnica(fromIndex: Int, toIndex: Int) {
        val lista = _uiState.value.tecnicas.toMutableList()

        if (fromIndex !in lista.indices || toIndex !in lista.indices) {
            return
        }

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