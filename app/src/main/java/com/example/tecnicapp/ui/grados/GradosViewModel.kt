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

data class ShareDataUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val friendToOverwrite: Friend? = null,
    val shareSuccessMessage: String? = null
)

class GradosViewModel(
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val friendsRepo: FriendsRepository = FriendsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradosUiState())
    val uiState: StateFlow<GradosUiState> = _uiState

    var friendsState by mutableStateOf(FriendsUiState())
        private set

    var shareDataState by mutableStateOf(ShareDataUiState())
        private set

    init {
        loadGrados()
    }

    // -------------------------------------------------------
    // GRADOS
    // -------------------------------------------------------

    fun currentUserUid(): String = repo.currentUserUid()

    fun loadGrados() {
        viewModelScope.launch {
            _uiState.value = GradosUiState(
                grados = emptyList(),
                isLoading = true,
                error = null
            )

            val result = repo.getGrados()

            _uiState.value = if (result.isSuccess) {
                GradosUiState(
                    grados = result.getOrNull().orEmpty(),
                    isLoading = false,
                    error = null
                )
            } else {
                GradosUiState(
                    grados = emptyList(),
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se pudieron cargar los grados"
                )
            }
        }
    }

    fun addGrado(nombre: String) {
        val cleanName = nombre.trim()

        if (cleanName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "El nombre del grado es obligatorio"
            )
            return
        }

        viewModelScope.launch {
            val result = repo.addGrado(cleanName)

            if (result.isSuccess) {
                loadGrados()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "Error al crear el grado"
                )
            }
        }
    }

    fun updateGrado(
        grado: Grado,
        nuevoNombre: String
    ) {
        val cleanName = nuevoNombre.trim()

        if (cleanName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "El nombre del grado es obligatorio"
            )
            return
        }

        viewModelScope.launch {
            val result = repo.updateGrado(
                gradoId = grado.id,
                nuevoNombre = cleanName
            )

            if (result.isSuccess) {
                loadGrados()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "Error al editar el grado"
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
                        ?: "Error al eliminar el grado"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // -------------------------------------------------------
    // AMIGOS
    // -------------------------------------------------------

    fun loadFriends() {
        viewModelScope.launch {
            friendsState = friendsState.copy(
                isLoading = true,
                error = null
            )

            val result = friendsRepo.getFriends()

            friendsState = if (result.isSuccess) {
                FriendsUiState(
                    isLoading = false,
                    friends = result.getOrNull().orEmpty(),
                    error = null
                )
            } else {
                FriendsUiState(
                    isLoading = false,
                    friends = emptyList(),
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "Error cargando amigos"
                )
            }
        }
    }

    fun addFriend(
        uidAmigo: String,
        nombre: String
    ) {
        val cleanUid = uidAmigo.trim()
        val cleanName = nombre.trim()

        if (cleanUid.isBlank() || cleanName.isBlank()) {
            friendsState = friendsState.copy(
                error = "El UID y el nombre son obligatorios"
            )
            return
        }

        viewModelScope.launch {
            friendsState = friendsState.copy(
                isLoading = true,
                error = null
            )

            val result = friendsRepo.addFriend(
                uidAmigo = cleanUid,
                nombre = cleanName
            )

            if (result.isSuccess) {
                loadFriends()
            } else {
                friendsState = friendsState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
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

            val result = friendsRepo.deleteFriend(friend.id)

            if (result.isSuccess) {
                loadFriends()
            } else {
                friendsState = friendsState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se ha podido eliminar el amigo"
                )
            }
        }
    }

    // -------------------------------------------------------
    // COMPARTIR / SOBRESCRIBIR
    // -------------------------------------------------------

    fun prepareShareDataToFriend(friend: Friend) {
        viewModelScope.launch {
            shareDataState = ShareDataUiState(isLoading = true)

            val result = repo.userHasGrados(friend.uidAmigo)

            if (result.isFailure) {
                shareDataState = ShareDataUiState(
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "No se ha podido comprobar si el amigo ya tiene datos"
                )
                return@launch
            }

            val friendHasData = result.getOrDefault(false)

            if (friendHasData) {
                shareDataState = ShareDataUiState(
                    friendToOverwrite = friend
                )
            } else {
                copyMyDataToFriend(
                    friendUid = friend.uidAmigo,
                    friendName = friend.nombre,
                    overwrite = false
                )
            }
        }
    }

    fun confirmOverwriteData(friend: Friend) {
        viewModelScope.launch {
            copyMyDataToFriend(
                friendUid = friend.uidAmigo,
                friendName = friend.nombre,
                overwrite = true
            )
        }
    }

    fun cancelOverwriteData() {
        shareDataState = ShareDataUiState()
    }

    private suspend fun copyMyDataToFriend(
        friendUid: String,
        friendName: String,
        overwrite: Boolean
    ) {
        val currentUid = repo.currentUserUid()

        if (currentUid.isBlank()) {
            shareDataState = ShareDataUiState(
                error = "No hay usuario autenticado"
            )
            return
        }

        if (friendUid.isBlank()) {
            shareDataState = ShareDataUiState(
                error = "El UID del amigo no es válido"
            )
            return
        }

        if (currentUid == friendUid) {
            shareDataState = ShareDataUiState(
                error = "No puedes compartir datos contigo misma"
            )
            return
        }

        shareDataState = ShareDataUiState(isLoading = true)

        val result = if (overwrite) {
            repo.overwriteGradosAndTecnicasToUser(
                fromUid = currentUid,
                toUid = friendUid
            )
        } else {
            repo.copyGradosAndTecnicasToUser(
                fromUid = currentUid,
                toUid = friendUid
            )
        }

        shareDataState = if (result.isSuccess) {
            ShareDataUiState(
                shareSuccessMessage = if (overwrite) {
                    "Los datos de $friendName se han sobrescrito correctamente"
                } else {
                    "Grados y técnicas enviados a $friendName"
                }
            )
        } else {
            ShareDataUiState(
                error = result.exceptionOrNull()?.localizedMessage
                    ?: "No se han podido enviar los datos"
            )
        }
    }

    // -------------------------------------------------------
    // CLONAR TÉCNICAS DE UN COMPAÑERO
    // -------------------------------------------------------

    fun cloneTechniquesFromFriend(uidAmigo: String) {
        viewModelScope.launch {
            shareDataState = shareDataState.copy(
                isLoading = true,
                error = null,
                shareSuccessMessage = null,
                friendToOverwrite = null
            )

            val result = repo.cloneTechniquesFromFriend(uidAmigo)

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
                        ?: "No se pudieron clonar las técnicas"
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

    // -------------------------------------------------------
    // CAMBIO / LOGOUT DE USUARIO
    // -------------------------------------------------------

    fun clearUserData() {
        _uiState.value = GradosUiState(
            grados = emptyList(),
            isLoading = false,
            error = null
        )

        friendsState = FriendsUiState(
            isLoading = false,
            error = null,
            friends = emptyList()
        )

        shareDataState = ShareDataUiState(
            isLoading = false,
            error = null,
            friendToOverwrite = null,
            shareSuccessMessage = null
        )
    }
}