package com.example.tecnicapp.ui.grados

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tecnicapp.data.Friend
import com.example.tecnicapp.data.FirestoreRepository
import com.example.tecnicapp.data.FriendsUiState
import com.example.tecnicapp.model.Grado
import kotlinx.coroutines.launch

data class GradosUiState(
    val isLoading: Boolean = false,
    val grados: List<Grado> = emptyList(),
    val error: String? = null
)

data class ShareDataUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val shareSuccessMessage: String? = null,
    val friendToOverwrite: Friend? = null
)

class GradosViewModel(
    private val repo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    var uiState by mutableStateOf(GradosUiState())
        private set

    var friendsState by mutableStateOf(FriendsUiState())
        private set

    var shareDataState by mutableStateOf(ShareDataUiState())
        private set

    var seleccionTecnicasState by mutableStateOf(SeleccionTecnicasUiState())
        private set

    var friendAddedSuccessfully by mutableStateOf(false)
        private set

    // GRADOS

    fun loadGrados() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                error = null
            )

            val result = repo.getGrados()

            uiState = if (result.isSuccess) {
                uiState.copy(
                    isLoading = false,
                    grados = result.getOrNull().orEmpty()
                )
            } else {
                uiState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se han podido cargar los grados"
                )
            }
        }
    }

    fun addGrado(nombre: String) {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                error = null
            )

            val result = repo.addGrado(nombre)

            uiState = if (result.isSuccess) {
                uiState.copy(
                    isLoading = false
                )
            } else {
                uiState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se ha podido crear el grado"
                )
            }

            loadGrados()
        }
    }

    fun updateGrado(
        grado: Grado,
        nuevoNombre: String
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                error = null
            )

            val result = repo.updateGrado(
                gradoId = grado.id,
                nuevoNombre = nuevoNombre
            )

            uiState = if (result.isSuccess) {
                uiState.copy(
                    isLoading = false
                )
            } else {
                uiState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se ha podido actualizar el grado"
                )
            }

            loadGrados()
        }
    }

    fun deleteGrado(grado: Grado) {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                error = null
            )

            val result = repo.deleteGrado(grado.id)

            uiState = if (result.isSuccess) {
                uiState.copy(
                    isLoading = false
                )
            } else {
                uiState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se ha podido eliminar el grado"
                )
            }

            loadGrados()
        }
    }

    fun clearError() {
        uiState = uiState.copy(
            error = null
        )
    }
    fun clearUserData() {
        uiState = GradosUiState()
        friendsState = FriendsUiState()
        shareDataState = ShareDataUiState()
        seleccionTecnicasState = SeleccionTecnicasUiState()
        friendAddedSuccessfully = false
    }

    fun currentUserUid(): String {
        return repo.currentUserUid()
    }

    // AMIGOS

    fun loadFriends() {
        viewModelScope.launch {
            friendsState = friendsState.copy(
                isLoading = true,
                error = null
            )

            val result = repo.getFriends()

            friendsState = if (result.isSuccess) {
                friendsState.copy(
                    isLoading = false,
                    friends = result.getOrNull().orEmpty()
                )
            } else {
                friendsState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se han podido cargar los amigos"
                )
            }
        }
    }

    fun addFriend(
        uidAmigo: String,
        nombre: String
    ) {
        viewModelScope.launch {
            friendAddedSuccessfully = false

            friendsState = friendsState.copy(
                isLoading = true,
                error = null
            )

            android.util.Log.d(
                "TECNIAPP_FRIENDS",
                "Intentando añadir amigo: uid=$uidAmigo, nombre=$nombre"
            )

            val result = repo.addFriend(
                uidAmigo = uidAmigo,
                nombre = nombre
            )

            if (result.isSuccess) {
                android.util.Log.d(
                    "TECNIAPP_FRIENDS",
                    "Amigo añadido correctamente"
                )

                friendAddedSuccessfully = true

                friendsState = friendsState.copy(
                    isLoading = false,
                    error = null
                )

                loadFriends()
            } else {
                val exception = result.exceptionOrNull()

                android.util.Log.e(
                    "TECNIAPP_FRIENDS",
                    "Error al añadir amigo",
                    exception
                )

                friendAddedSuccessfully = false

                friendsState = friendsState.copy(
                    isLoading = false,
                    error = exception?.localizedMessage
                        ?: "No se ha podido añadir el amigo"
                )
            }
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch {
            friendsState = friendsState.copy(
                isLoading = true,
                error = null
            )

            val result = repo.deleteFriend(friend.id)

            friendsState = if (result.isSuccess) {
                friendsState.copy(
                    isLoading = false
                )
            } else {
                friendsState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se ha podido eliminar el amigo"
                )
            }

            loadFriends()
        }
    }

    fun clearFriendAdded() {
        friendAddedSuccessfully = false
    }

    // SELECCIÓN DE TÉCNICAS

    fun cargarTecnicasParaSeleccion() {
        viewModelScope.launch {
            seleccionTecnicasState = seleccionTecnicasState.copy(
                isLoading = true,
                error = null,
                tecnicas = emptyList(),
                seleccionadas = emptySet()
            )

            val result = repo.getMisTecnicasSeleccionables()

            seleccionTecnicasState = if (result.isSuccess) {
                seleccionTecnicasState.copy(
                    isLoading = false,
                    tecnicas = result.getOrNull().orEmpty()
                )
            } else {
                seleccionTecnicasState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se pudieron cargar tus técnicas"
                )
            }
        }
    }

    fun cargarTecnicasDeCompaniero(uidAmigo: String) {
        viewModelScope.launch {
            seleccionTecnicasState = seleccionTecnicasState.copy(
                isLoading = true,
                error = null,
                tecnicas = emptyList(),
                seleccionadas = emptySet()
            )

            val result = repo.getTecnicasSeleccionablesDeUsuario(
                uidUsuario = uidAmigo
            )

            seleccionTecnicasState = if (result.isSuccess) {
                seleccionTecnicasState.copy(
                    isLoading = false,
                    tecnicas = result.getOrNull().orEmpty()
                )
            } else {
                seleccionTecnicasState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se pudieron cargar las técnicas del compañero"
                )
            }
        }
    }

    fun alternarSeleccionTecnica(tecnica: TecnicaSeleccionable) {
        val clave = "${tecnica.gradoId}_${tecnica.tecnicaId}"

        val seleccionActual = seleccionTecnicasState.seleccionadas

        seleccionTecnicasState = seleccionTecnicasState.copy(
            seleccionadas = if (clave in seleccionActual) {
                seleccionActual - clave
            } else {
                seleccionActual + clave
            }
        )
    }

    fun seleccionarTodasLasTecnicas() {
        seleccionTecnicasState = seleccionTecnicasState.copy(
            seleccionadas = seleccionTecnicasState.tecnicas
                .map { "${it.gradoId}_${it.tecnicaId}" }
                .toSet()
        )
    }

    fun deseleccionarTodasLasTecnicas() {
        seleccionTecnicasState = seleccionTecnicasState.copy(
            seleccionadas = emptySet()
        )
    }

    private fun obtenerTecnicasSeleccionadas(): List<TecnicaSeleccionable> {
        return seleccionTecnicasState.tecnicas.filter { tecnica ->
            val clave = "${tecnica.gradoId}_${tecnica.tecnicaId}"
            clave in seleccionTecnicasState.seleccionadas
        }
    }

    fun compartirTecnicasSeleccionadas(friend: Friend) {
        viewModelScope.launch {
            val tecnicas = obtenerTecnicasSeleccionadas()

            if (tecnicas.isEmpty()) {
                shareDataState = shareDataState.copy(
                    error = "Selecciona al menos una técnica"
                )
                return@launch
            }

            shareDataState = shareDataState.copy(
                isLoading = true,
                error = null,
                shareSuccessMessage = null
            )

            val miUid = repo.currentUserUid()

            val result = repo.crearSolicitudCompartirTecnicas(
                fromUid = miUid,
                toUid = friend.uidAmigo,
                tecnicas = tecnicas
            )

            shareDataState = if (result.isSuccess) {
                shareDataState.copy(
                    isLoading = false,
                    shareSuccessMessage =
                        "Solicitud enviada a ${friend.nombre}. " +
                                "Deberá aceptarla para recibir las técnicas."
                )
            } else {
                shareDataState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se ha podido enviar la solicitud"
                )
            }
        }
    }

    var solicitudesRecibidas by mutableStateOf<List<Map<String, Any?>>>(emptyList())
        private set

    var solicitudesLoading by mutableStateOf(false)
        private set

    var solicitudesError by mutableStateOf<String?>(null)
        private set

    fun cargarSolicitudesRecibidas() {
        viewModelScope.launch {
            solicitudesLoading = true
            solicitudesError = null

            val result = repo.getSolicitudesCompartirRecibidas()

            if (result.isSuccess) {
                solicitudesRecibidas = result.getOrNull().orEmpty()
            } else {
                solicitudesError = result.exceptionOrNull()?.localizedMessage
                    ?: "No se han podido cargar las solicitudes"
            }

            solicitudesLoading = false
        }
    }

    fun aceptarSolicitud(solicitudId: String) {
        viewModelScope.launch {
            solicitudesLoading = true
            solicitudesError = null

            val result = repo.aceptarSolicitudCompartirTecnicas(solicitudId)

            if (result.isSuccess) {
                val total = result.getOrNull() ?: 0

                shareDataState = shareDataState.copy(
                    shareSuccessMessage = if (total == 0) {
                        "No había técnicas nuevas que añadir"
                    } else {
                        "Se han añadido $total técnicas nuevas"
                    }
                )

                cargarSolicitudesRecibidas()
                loadGrados()
            } else {
                solicitudesError = result.exceptionOrNull()?.localizedMessage
                    ?: "No se ha podido aceptar la solicitud"
            }

            solicitudesLoading = false
        }
    }

    fun rechazarSolicitud(solicitudId: String) {
        viewModelScope.launch {
            solicitudesLoading = true
            solicitudesError = null

            val result = repo.rechazarSolicitudCompartirTecnicas(solicitudId)

            if (result.isSuccess) {
                cargarSolicitudesRecibidas()
            } else {
                solicitudesError = result.exceptionOrNull()?.localizedMessage
                    ?: "No se ha podido rechazar la solicitud"
            }

            solicitudesLoading = false
        }
    }

    fun clonarTecnicasSeleccionadas(uidAmigo: String) {
        viewModelScope.launch {
            val tecnicas = obtenerTecnicasSeleccionadas()

            if (tecnicas.isEmpty()) {
                shareDataState = shareDataState.copy(
                    error = "Selecciona al menos una técnica"
                )
                return@launch
            }

            shareDataState = shareDataState.copy(
                isLoading = true,
                error = null,
                shareSuccessMessage = null
            )

            val result = repo.clonarTecnicasSeleccionadas(
                uidAmigo = uidAmigo,
                tecnicas = tecnicas
            )

            shareDataState = if (result.isSuccess) {
                val total = result.getOrNull() ?: 0

                shareDataState.copy(
                    isLoading = false,
                    shareSuccessMessage = if (total == 0) {
                        "No había técnicas nuevas que clonar"
                    } else {
                        "Se han clonado $total técnicas nuevas"
                    }
                )
            } else {
                shareDataState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se han podido clonar las técnicas"
                )
            }

            loadGrados()
        }
    }

    // COMPATIBILIDAD CON PANTALLA ANTIGUA

    fun prepareShareDataToFriend(friend: Friend) {
        viewModelScope.launch {
            shareDataState = shareDataState.copy(
                isLoading = true,
                error = null,
                shareSuccessMessage = null,
                friendToOverwrite = null
            )

            val miUid = repo.currentUserUid()

            val result = repo.shareGradosAndTecnicasToUser(
                fromUid = miUid,
                toUid = friend.uidAmigo,
                overwrite = false
            )

            shareDataState = if (result.isSuccess) {
                val total = result.getOrNull() ?: 0

                shareDataState.copy(
                    isLoading = false,
                    shareSuccessMessage = if (total == 0) {
                        "No había técnicas nuevas que compartir"
                    } else {
                        "Se han compartido $total técnicas con ${friend.nombre}"
                    }
                )
            } else {
                shareDataState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se han podido compartir las técnicas"
                )
            }
        }
    }

    fun confirmOverwriteData(friend: Friend) {
        viewModelScope.launch {
            shareDataState = shareDataState.copy(
                isLoading = true,
                error = null,
                shareSuccessMessage = null,
                friendToOverwrite = null
            )

            val miUid = repo.currentUserUid()

            val result = repo.shareGradosAndTecnicasToUser(
                fromUid = miUid,
                toUid = friend.uidAmigo,
                overwrite = true
            )

            shareDataState = if (result.isSuccess) {
                val total = result.getOrNull() ?: 0

                shareDataState.copy(
                    isLoading = false,
                    shareSuccessMessage = if (total == 0) {
                        "No había técnicas nuevas que compartir"
                    } else {
                        "Se han compartido $total técnicas con ${friend.nombre}"
                    }
                )
            } else {
                shareDataState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se han podido compartir las técnicas"
                )
            }
        }
    }

    fun cancelOverwriteData() {
        shareDataState = shareDataState.copy(
            friendToOverwrite = null
        )
    }

    fun cloneTechniquesFromFriend(uidAmigo: String) {
        viewModelScope.launch {
            shareDataState = shareDataState.copy(
                isLoading = true,
                error = null,
                shareSuccessMessage = null
            )

            val result = repo.cloneTechniquesFromFriend(
                uidAmigo = uidAmigo
            )

            shareDataState = if (result.isSuccess) {
                val total = result.getOrNull() ?: 0

                shareDataState.copy(
                    isLoading = false,
                    shareSuccessMessage = if (total == 0) {
                        "No había técnicas nuevas que clonar"
                    } else {
                        "Se han clonado $total técnicas nuevas"
                    }
                )
            } else {
                shareDataState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se han podido clonar las técnicas"
                )
            }

            loadGrados()
        }
    }

    fun clearShareMessage() {
        shareDataState = shareDataState.copy(
            error = null,
            shareSuccessMessage = null
        )
    }
}