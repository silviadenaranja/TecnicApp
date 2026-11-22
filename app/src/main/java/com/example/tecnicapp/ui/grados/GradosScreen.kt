package com.example.tecnicapp.ui.grados

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tecnicapp.data.Friend
import com.example.tecnicapp.data.FriendsUiState
import com.example.tecnicapp.model.Grado
import com.example.tecnicapp.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tecnicapp.R
import androidx.compose.material.icons.filled.Close

data class TecnicaSeleccionable(
    val gradoId: String = "",
    val gradoNombre: String = "",
    val gradoNumero: Int = 0,
    val tecnicaId: String = "",
    val nombre: String = "",
    val datos: Map<String, Any?> = emptyMap()
)

data class SeleccionTecnicasUiState(
    val isLoading: Boolean = false,
    val tecnicas: List<TecnicaSeleccionable> = emptyList(),
    val seleccionadas: Set<String> = emptySet(),
    val error: String? = null
)

enum class GradosSortOrder {
    ASC,
    DESC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradosScreen(
    vm: GradosViewModel = viewModel(),
    authVm: AuthViewModel = viewModel(),
    onGradoClicked: (Grado) -> Unit,
    onVerTodasTecnicas: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state = vm.uiState

    val friendsState = vm.friendsState
    val shareDataState = vm.shareDataState
    val seleccionTecnicasState = vm.seleccionTecnicasState
    val changePassState = authVm.changePassState

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showGradoDialog by remember { mutableStateOf(false) }
    var editingGrado by remember { mutableStateOf<Grado?>(null) }
    var nombreTemp by remember { mutableStateOf("") }

    var sortOrder by remember { mutableStateOf(GradosSortOrder.ASC) }

    var showDeleteGradoDialog by remember { mutableStateOf(false) }
    var gradoToDelete by remember { mutableStateOf<Grado?>(null) }

    var menuExpanded by remember { mutableStateOf(false) }
    var showChangePassDialog by remember { mutableStateOf(false) }

    var showFriendsMenuDialog by remember { mutableStateOf(false) }
    var showFriendsDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    var showShareDialog by remember { mutableStateOf(false) }
    var showCloneDialog by remember { mutableStateOf(false) }

    var showSeleccionTecnicasDialog by remember { mutableStateOf(false) }
    var modoSeleccion by remember { mutableStateOf("compartir") }
    var amigoSeleccionado by remember { mutableStateOf<Friend?>(null) }

    var friendToDelete by remember { mutableStateOf<Friend?>(null) }
    var showDeleteFriendDialog by remember { mutableStateOf(false) }

    var showSolicitudesDialog by remember { mutableStateOf(false) }

    var showEasterEggDialog by remember { mutableStateOf(false) }
    var tituloTaps by remember { mutableStateOf(0) }

    val gradosOrdenados = when (sortOrder) {
        GradosSortOrder.ASC -> {
            state.grados.sortedBy { it.nombre.lowercase() }
        }

        GradosSortOrder.DESC -> {
            state.grados.sortedByDescending { it.nombre.lowercase() }
        }
    }

    LaunchedEffect(Unit) {
        vm.loadGrados()
        vm.cargarSolicitudesRecibidas()
    }

    LaunchedEffect(
        shareDataState.error,
        shareDataState.shareSuccessMessage
    ) {
        val message = shareDataState.error
            ?: shareDataState.shareSuccessMessage

        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            vm.clearShareMessage()
        }
    }

    LaunchedEffect(state.error) {
        val error = state.error

        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error)
            vm.clearError()
        }
    }

    LaunchedEffect(vm.friendAddedSuccessfully) {
        if (vm.friendAddedSuccessfully) {
            showAddFriendDialog = false
            vm.clearFriendAdded()
        }
    }

    fun openNewGradoDialog() {
        editingGrado = null
        nombreTemp = ""
        showGradoDialog = true
    }

    fun openEditGradoDialog(grado: Grado) {
        editingGrado = grado
        nombreTemp = grado.nombre
        showGradoDialog = true
    }

    fun closeGradoDialog() {
        showGradoDialog = false
        editingGrado = null
        nombreTemp = ""
    }

    fun saveGrado() {
        val nombre = nombreTemp.trim()

        if (nombre.isBlank()) {
            return
        }

        if (editingGrado == null) {
            vm.addGrado(nombre)
        } else {
            vm.updateGrado(
                grado = editingGrado!!,
                nuevoNombre = nombre
            )
        }

        closeGradoDialog()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis grados",
                        modifier = Modifier.clickable {
                            tituloTaps++

                            if (tituloTaps >= 5) {
                                tituloTaps = 0
                                showEasterEggDialog = true
                            }
                        }
                    )
                },
                navigationIcon = {
                    Box {
                        IconButton(
                            onClick = {
                                menuExpanded = true
                            }
                        ) {
                            Box {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menú"
                                )

                                if (vm.solicitudesRecibidas.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(12.dp)
                                            .background(
                                                color = Color.Red,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                    }
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = {
                                menuExpanded = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Copiar mi ID")
                                },
                                onClick = {
                                    menuExpanded = false

                                    val uid = vm.currentUserUid()

                                    if (uid.isBlank()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "No hay usuario autenticado"
                                            )
                                        }
                                    } else {
                                        clipboardManager.setText(
                                            AnnotatedString(uid)
                                        )

                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "ID copiado al portapapeles"
                                            )
                                        }
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Amigos")
                                },
                                onClick = {
                                    menuExpanded = false
                                    vm.loadFriends()
                                    showFriendsMenuDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                      //  Spacer(modifier = Modifier.width(8.dp))

                                        Text("Solicitudes recibidas")

                                        if (vm.solicitudesRecibidas.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(8.dp))

                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(
                                                        color = Color.Red,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = vm.solicitudesRecibidas.size.toString(),
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    vm.cargarSolicitudesRecibidas()
                                    showSolicitudesDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Compartir mis técnicas con un compañero")
                                },
                                onClick = {
                                    menuExpanded = false
                                    modoSeleccion = "compartir"
                                    vm.loadFriends()
                                    showShareDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Clonar técnicas de un compañero")
                                },
                                onClick = {
                                    menuExpanded = false
                                    modoSeleccion = "clonar"
                                    vm.loadFriends()
                                    showCloneDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Cambiar contraseña")
                                },
                                onClick = {
                                    menuExpanded = false
                                    authVm.clearChangePasswordState()
                                    showChangePassDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Cerrar sesión")
                                },
                                onClick = {
                                    menuExpanded = false
                                    onLoggedOut()
                                }
                            )
                        }
                    }
                },
                actions = {
                    SortMenu(
                        sortOrder = sortOrder,
                        onSortChange = { sortOrder = it }
                    )

                    TextButton(
                        onClick = onVerTodasTecnicas
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Ver todas las técnicas"
                            )

                            Text(
                                text = "Técnicas",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Button(
                    onClick = {
                        openNewGradoDialog()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Añadir nuevo grado")
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    state.isLoading && state.grados.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    gradosOrdenados.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay grados creados todavía")
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = gradosOrdenados,
                                key = { it.id }
                            ) { grado ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onGradoClicked(grado)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = grado.nombre,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )

                                        IconButton(
                                            onClick = {
                                                openEditGradoDialog(grado)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar grado"
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                gradoToDelete = grado
                                                showDeleteGradoDialog = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar grado",
                                              //  tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (shareDataState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (showSolicitudesDialog) {
                SolicitudesDialog(
                    solicitudes = vm.solicitudesRecibidas,
                    isLoading = vm.solicitudesLoading,
                    error = vm.solicitudesError,
                    onDismiss = {
                        showSolicitudesDialog = false
                    },
                    onAceptar = { solicitudId ->
                        vm.aceptarSolicitud(solicitudId)
                    },
                    onRechazar = { solicitudId ->
                        vm.rechazarSolicitud(solicitudId)
                    }
                )
            }
            if (showEasterEggDialog) {
                EasterEggDialog(
                    onDismiss = {
                        showEasterEggDialog = false
                        tituloTaps = 0
                    }
                )
            }

            if (showGradoDialog) {
                AlertDialog(
                    onDismissRequest = {
                        closeGradoDialog()
                    },
                    title = {
                        Text(
                            if (editingGrado == null) {
                                "Nuevo grado"
                            } else {
                                "Editar grado"
                            }
                        )
                    },
                    text = {
                        OutlinedTextField(
                            value = nombreTemp,
                            onValueChange = {
                                nombreTemp = it
                            },
                            label = {
                                Text("Nombre del grado")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            enabled = nombreTemp.trim().isNotBlank(),
                            onClick = {
                                saveGrado()
                            }
                        ) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                closeGradoDialog()
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showDeleteGradoDialog && gradoToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteGradoDialog = false
                        gradoToDelete = null
                    },
                    title = {
                        Text("Eliminar grado completo")
                    },
                    text = {
                        Text(
                            "Vas a eliminar el grado completo y todas sus técnicas. " +
                                    "Esta acción no se puede deshacer."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.deleteGrado(gradoToDelete!!)
                                showDeleteGradoDialog = false
                                gradoToDelete = null
                            }
                        ) {
                            Text(
                                text = "Eliminar grado",
                                //color = Color.Red
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteGradoDialog = false
                                gradoToDelete = null
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showChangePassDialog) {
                ChangePasswordDialog(
                    state = changePassState,
                    onDismiss = {
                        showChangePassDialog = false
                        authVm.clearChangePasswordState()
                    },
                    onChangePassword = { currentPassword, newPassword ->
                        authVm.changePassword(currentPassword, newPassword)
                    }
                )
            }

            if (showFriendsMenuDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showFriendsMenuDialog = false
                    },
                    title = {
                        Text("Amigos")
                    },
                    text = {
                        Column {
                            Button(
                                onClick = {
                                    showFriendsMenuDialog = false
                                    showAddFriendDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Añadir amigo")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    showFriendsMenuDialog = false
                                    vm.loadFriends()
                                    showFriendsDialog = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver amigos")
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showFriendsMenuDialog = false
                            }
                        ) {
                            Text("Cerrar")
                        }
                    }
                )
            }

            if (showFriendsDialog) {
                FriendsDialog(
                    friendsState = friendsState,
                    onDismiss = {
                        showFriendsDialog = false
                    },
                    onDeleteFriendClick = { friend ->
                        friendToDelete = friend
                        showDeleteFriendDialog = true
                    }
                )
            }

            if (showAddFriendDialog) {
                AddFriendDialog(
                    friendsState = friendsState,
                    onDismiss = {
                        showAddFriendDialog = false
                        vm.clearFriendAdded()
                    },
                    onAddFriend = { uid, nombre ->
                        vm.addFriend(
                            uidAmigo = uid,
                            nombre = nombre
                        )
                    }
                )
            }

            val currentFriendToDelete = friendToDelete

            if (showDeleteFriendDialog && currentFriendToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteFriendDialog = false
                        friendToDelete = null
                    },
                    title = {
                        Text("Eliminar amigo")
                    },
                    text = {
                        Text(
                            "¿Quieres eliminar a ${currentFriendToDelete.nombre} de tu lista de amigos? " +
                                    "Esto no borrará sus grados ni sus técnicas."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.deleteFriend(currentFriendToDelete)

                                showDeleteFriendDialog = false
                                friendToDelete = null
                            }
                        ) {
                            Text(
                                text = "Eliminar",
                                //color = Color.Black
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteFriendDialog = false
                                friendToDelete = null
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showShareDialog) {
                ShareOrCloneDialog(
                    title = "Compartir mis técnicas con un compañero",
                    description = "Selecciona un amigo existente o introduce el UID de un compañero. " +
                            "Después podrás elegir qué técnicas tuyas quieres compartir. " +
                            "El compañero recibirá una solicitud y podrá aceptarla o rechazarla.",
                    friends = friendsState.friends,
                    isLoading = friendsState.isLoading,
                    onDismiss = {
                        showShareDialog = false
                    },
                    onFriendSelected = { friend ->
                        showShareDialog = false
                        amigoSeleccionado = friend
                        modoSeleccion = "compartir"

                        vm.cargarTecnicasParaSeleccion()

                        showSeleccionTecnicasDialog = true
                    },
                    onUidSelected = { uid ->
                        showShareDialog = false
                        amigoSeleccionado = Friend(
                            id = uid,
                            uidAmigo = uid,
                            nombre = "Compañero"
                        )
                        modoSeleccion = "compartir"

                        vm.cargarTecnicasParaSeleccion()

                        showSeleccionTecnicasDialog = true
                    },
                    onAddFriendClick = {
                        showShareDialog = false
                        showAddFriendDialog = true
                    }
                )
            }

            if (showCloneDialog) {
                ShareOrCloneDialog(
                    title = "Clonar técnicas de un compañero",
                    description = "Selecciona un amigo existente o introduce el UID de un compañero. " +
                            "Después podrás elegir qué técnicas de ese compañero quieres clonar. " +
                            "Se crearán en tu cuenta los grados necesarios y solo se añadirán las técnicas que todavía no tengas.",
                    friends = friendsState.friends,
                    isLoading = friendsState.isLoading,
                    onDismiss = {
                        showCloneDialog = false
                    },
                    onFriendSelected = { friend ->
                        showCloneDialog = false
                        amigoSeleccionado = friend
                        modoSeleccion = "clonar"

                        vm.cargarTecnicasDeCompaniero(friend.uidAmigo)

                        showSeleccionTecnicasDialog = true
                    },
                    onUidSelected = { uid ->
                        showCloneDialog = false
                        amigoSeleccionado = Friend(
                            id = uid,
                            uidAmigo = uid,
                            nombre = "Compañero"
                        )
                        modoSeleccion = "clonar"

                        vm.cargarTecnicasDeCompaniero(uid)

                        showSeleccionTecnicasDialog = true
                    },
                    onAddFriendClick = {
                        showCloneDialog = false
                        showAddFriendDialog = true
                    }
                )
            }

            val amigoParaSeleccion = amigoSeleccionado

            if (showSeleccionTecnicasDialog && amigoParaSeleccion != null) {
                SeleccionTecnicasDialog(
                    state = seleccionTecnicasState,
                    titulo = if (modoSeleccion == "compartir") {
                        "Selecciona técnicas para compartir"
                    } else {
                        "Selecciona técnicas para clonar"
                    },
                    onDismiss = {
                        showSeleccionTecnicasDialog = false
                    },
                    onToggleTecnica = { tecnica ->
                        vm.alternarSeleccionTecnica(tecnica)
                    },
                    onSeleccionarTodas = {
                        vm.seleccionarTodasLasTecnicas()
                    },
                    onDeseleccionarTodas = {
                        vm.deseleccionarTodasLasTecnicas()
                    },
                    onConfirmar = {
                        if (modoSeleccion == "compartir") {
                            vm.compartirTecnicasSeleccionadas(amigoParaSeleccion)
                        } else {
                            vm.clonarTecnicasSeleccionadas(
                                amigoParaSeleccion.uidAmigo
                            )
                        }

                        showSeleccionTecnicasDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FriendsDialog(
    friendsState: FriendsUiState,
    onDismiss: () -> Unit,
    onDeleteFriendClick: (Friend) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Mis amigos")
        },
        text = {
            Column {
                when {
                    friendsState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    friendsState.friends.isEmpty() -> {
                        Text(
                            text = "Todavía no tienes amigos añadidos",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(
                                items = friendsState.friends,
                                key = { it.id }
                            ) { friend ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = friend.nombre,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = {
                                            onDeleteFriendClick(friend)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar amigo",
                                          //  tint = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                friendsState.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = error,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun ShareOrCloneDialog(
    title: String,
    description: String,
    friends: List<Friend>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onFriendSelected: (Friend) -> Unit,
    onUidSelected: (String) -> Unit,
    onAddFriendClick: () -> Unit
) {
    var uidText by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Column {
                Text(
                    text = description,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAddFriendClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Añadir amigo nuevo")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Selecciona un amigo existente",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    friends.isEmpty() -> {
                        Text(
                            text = "Todavía no tienes amigos añadidos",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    else -> {
                        friends.forEach { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onFriendSelected(friend)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = friend.nombre,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Seleccionar amigo"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "O introduce un UID nuevo",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uidText,
                    onValueChange = {
                        uidText = it
                    },
                    label = {
                        Text("UID del compañero")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    enabled = uidText.trim().isNotBlank(),
                    onClick = {
                        onUidSelected(uidText.trim())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar con este UID")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun SeleccionTecnicasDialog(
    state: SeleccionTecnicasUiState,
    titulo: String,
    onDismiss: () -> Unit,
    onToggleTecnica: (TecnicaSeleccionable) -> Unit,
    onSeleccionarTodas: () -> Unit,
    onDeseleccionarTodas: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(titulo)
        },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onSeleccionarTodas
                    ) {
                        Text("Seleccionar todas")
                    }

                    TextButton(
                        onClick = onDeseleccionarTodas
                    ) {
                        Text("Quitar selección")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.tecnicas.isEmpty()) {
                    Text("No hay técnicas disponibles")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tecnicasPorGrado: Map<String, List<TecnicaSeleccionable>> =
                            state.tecnicas.groupBy { tecnica ->
                                tecnica.gradoNombre
                            }

                        tecnicasPorGrado.forEach { entrada ->
                            val gradoNombre = entrada.key
                            val tecnicas = entrada.value

                            item {
                                Text(
                                    text = gradoNombre,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(
                                items = tecnicas,
                                key = { tecnica ->
                                    "${tecnica.gradoId}_${tecnica.tecnicaId}"
                                }
                            ) { tecnica ->
                                val clave = "${tecnica.gradoId}_${tecnica.tecnicaId}"

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onToggleTecnica(tecnica)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = clave in state.seleccionadas,
                                        onCheckedChange = {
                                            onToggleTecnica(tecnica)
                                        }
                                    )

                                    Text(
                                        text = tecnica.nombre.ifBlank {
                                            "Técnica sin nombre"
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = error,
                        color = Color.Black
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = state.seleccionadas.isNotEmpty() &&
                        !state.isLoading,
                onClick = onConfirmar
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun AddFriendDialog(
    friendsState: FriendsUiState,
    onDismiss: () -> Unit,
    onAddFriend: (String, String) -> Unit
) {
    var uidText by remember {
        mutableStateOf("")
    }

    var nombreText by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Añadir amigo")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = uidText,
                    onValueChange = {
                        uidText = it
                    },
                    label = {
                        Text("UID del amigo")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nombreText,
                    onValueChange = {
                        nombreText = it
                    },
                    label = {
                        Text("Nombre del amigo")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (friendsState.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Comprobando UID...")
                    }
                }

                friendsState.error?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = error,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = uidText.trim().isNotBlank() &&
                        nombreText.trim().isNotBlank() &&
                        !friendsState.isLoading,
                onClick = {
                    onAddFriend(
                        uidText.trim(),
                        nombreText.trim()
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    state: AuthViewModel.ChangePasswordUiState,
    onDismiss: () -> Unit,
    onChangePassword: (String, String) -> Unit
) {
    var currentPassword by remember {
        mutableStateOf("")
    }

    var newPassword by remember {
        mutableStateOf("")
    }

    var repeatNewPassword by remember {
        mutableStateOf("")
    }

    var showCurrentPassword by remember {
        mutableStateOf(false)
    }

    var showNewPassword by remember {
        mutableStateOf(false)
    }

    var showRepeatPassword by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cambiar contraseña")
        },
        text = {
            Column {
                PasswordField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                    },
                    label = "Contraseña actual",
                    visible = showCurrentPassword,
                    onVisibleChange = {
                        showCurrentPassword = it
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PasswordField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                    },
                    label = "Nueva contraseña",
                    visible = showNewPassword,
                    onVisibleChange = {
                        showNewPassword = it
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PasswordField(
                    value = repeatNewPassword,
                    onValueChange = {
                        repeatNewPassword = it
                    },
                    label = "Repite la nueva contraseña",
                    visible = showRepeatPassword,
                    onVisibleChange = {
                        showRepeatPassword = it
                    }
                )

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = error,
                        color = Color.Black
                    )
                }

                if (state.success) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Contraseña actualizada correctamente")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !state.isLoading &&
                        currentPassword.isNotBlank() &&
                        newPassword.isNotBlank() &&
                        newPassword == repeatNewPassword,
                onClick = {
                    onChangePassword(
                        currentPassword,
                        newPassword
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    onVisibleChange(!visible)
                }
            ) {
                Icon(
                    imageVector = if (visible) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = if (visible) {
                        "Ocultar contraseña"
                    } else {
                        "Mostrar contraseña"
                    }
                )
            }
        }
    )
}
@Composable
fun SortMenu(
    sortOrder: GradosSortOrder,
    onSortChange: (GradosSortOrder) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box {
        TextButton(
            onClick = {
                expanded = true
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Ordenar grados"
                )

                Text(
                    text = "Ordenar",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text("Ascendente")
                },
                onClick = {
                    onSortChange(GradosSortOrder.ASC)
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text("Descendente")
                },
                onClick = {
                    onSortChange(GradosSortOrder.DESC)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun SolicitudesDialog(
    solicitudes: List<Map<String, Any?>>,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onAceptar: (String) -> Unit,
    onRechazar: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Solicitudes recibidas")
        },
        text = {
            Column {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    solicitudes.isEmpty() -> {
                        Text(
                            text = "No tienes solicitudes pendientes",
                            color = Color.Gray
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = solicitudes,
                                key = { it["id"].toString() }
                            ) { solicitud ->
                                val solicitudId = solicitud["id"].toString()
                                val nombreEmisor = solicitud["fromNombre"]
                                    ?.toString()
                                    .orEmpty()

                                val tecnicas = solicitud["tecnicas"] as? List<*>
                                    ?: emptyList<Any>()

                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = if (nombreEmisor.isBlank()) {
                                                "Un usuario quiere compartirte técnicas"
                                            } else {
                                                "$nombreEmisor quiere compartirte técnicas"
                                            },
                                            style = MaterialTheme.typography.titleSmall
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Técnicas incluidas: ${tecnicas.size}",
                                            fontSize = 14.sp
                                        )

                                        if (tecnicas.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = "Técnicas que quiere compartir:",
                                                style = MaterialTheme.typography.titleSmall
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Column {
                                                tecnicas.forEach { tecnicaItem ->
                                                    val datosTecnica = tecnicaItem as? Map<*, *>

                                                    val nombreTecnica = datosTecnica?.get("nombre")
                                                        ?.toString()
                                                        ?.trim()
                                                        .orEmpty()

                                                    if (nombreTecnica.isNotBlank()) {
                                                        Text(
                                                            text = "• $nombreTecnica",
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    onRechazar(solicitudId)
                                                }
                                            ) {
                                                Text("No")
                                            }

                                            Button(
                                                onClick = {
                                                    onAceptar(solicitudId)
                                                }
                                            ) {
                                                Text("Sí")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                error?.let { mensajeError ->
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = mensajeError,
                        color = Color.Black
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cerrar")
            }
        }
    )
}
@Composable
private fun EasterEggDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Image(
                painter = painterResource(id = R.drawable.easter_egg),
                contentDescription = "Easter egg",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.Gray
                )
            }
        }
    }
}