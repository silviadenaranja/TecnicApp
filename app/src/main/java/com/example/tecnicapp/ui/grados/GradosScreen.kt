package com.example.tecnicapp.ui.grados

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tecnicapp.data.Friend
import com.example.tecnicapp.model.Grado
import com.example.tecnicapp.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

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
    val state by vm.uiState.collectAsState()
    LaunchedEffect(state.grados, state.isLoading) {
        android.util.Log.d(
            "TECNIAPP_GRADOS_SCREEN",
            "Pantalla: loading=${state.isLoading}, grados=${state.grados.size}"
        )
    }
    val friendsState = vm.friendsState
    val shareDataState = vm.shareDataState
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

    var showFriendsDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showListFriendsDialog by remember { mutableStateOf(false) }

    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    var friendToDelete by remember { mutableStateOf<Friend?>(null) }
    var showDeleteFriendDialog by remember { mutableStateOf(false) }
/*
    val gradosOrdenados = remember(state.grados, sortOrder) {
        when (sortOrder) {
            GradosSortOrder.ASC -> {
                state.grados.sortedBy { it.nombre.lowercase() }
            }

            GradosSortOrder.DESC -> {
                state.grados.sortedByDescending { it.nombre.lowercase() }
            }
        }
    }
*/
    val gradosOrdenados = when (sortOrder) {
        GradosSortOrder.ASC -> {
            state.grados.sortedBy { it.nombre.lowercase() }
        }

        GradosSortOrder.DESC -> {
            state.grados.sortedByDescending { it.nombre.lowercase() }
        }
    }


    /*
     * Un único efecto para mensajes de compartir:
     * éxito o error. El SnackbarHost está dentro del Scaffold.
     */
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

    /*
     * Mensajes propios de cargar/crear/editar/eliminar grados.
     */
    LaunchedEffect(state.error) {
        val error = state.error

        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error)
            vm.clearError()
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
                title = { Text("Mis grados") },
                navigationIcon = {
                    Box {
                        IconButton(
                            onClick = {
                                menuExpanded = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú"
                            )
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
                                    Text("Compartir mis técnicas")
                                },
                                onClick = {
                                    menuExpanded = false
                                    showFriendsDialog = true
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
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /*
             * Capa de carga al compartir/sobrescribir.
             */
            if (shareDataState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            /*
             * Crear / editar grado.
             */
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

            /*
             * Eliminar grado.
             */
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
                                color = MaterialTheme.colorScheme.error
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

            /*
             * Cambiar contraseña.
             */
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

            /*
             * Menú / diálogos de amigos.
             */
            FriendsDialogs(
                vm = vm,
                friendsState = friendsState,
                showFriendsDialog = showFriendsDialog,
                onShowFriendsDialogChange = {
                    showFriendsDialog = it
                },
                showAddFriendDialog = showAddFriendDialog,
                onShowAddFriendDialogChange = {
                    showAddFriendDialog = it
                },
                showListFriendsDialog = showListFriendsDialog,
                onShowListFriendsDialogChange = {
                    showListFriendsDialog = it
                },
                selectedFriend = selectedFriend,
                onSelectedFriendChange = {
                    selectedFriend = it
                },
                showDeleteFriendDialog = showDeleteFriendDialog,
                onShowDeleteFriendDialogChange = {
                    showDeleteFriendDialog = it
                },
                friendToDelete = friendToDelete,
                onFriendToDeleteChange = {
                    friendToDelete = it
                }
            )

            /*
             * Amigo ya tiene datos: pedir confirmación.
             */
            val friendToOverwrite = shareDataState.friendToOverwrite

            if (friendToOverwrite != null) {
                AlertDialog(
                    onDismissRequest = {
                        vm.cancelOverwriteData()
                    },
                    title = {
                        Text("Datos existentes")
                    },
                    text = {
                        Text(
                            "${friendToOverwrite.nombre} ya tiene grados o técnicas guardadas. " +
                                    "¿Quieres borrar sus datos actuales y sobrescribirlos con los tuyos?"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.confirmOverwriteData(friendToOverwrite)
                            }
                        ) {
                            Text("Sí, sobrescribir")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                vm.cancelOverwriteData()
                            }
                        ) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
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
                        color = MaterialTheme.colorScheme.error
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
private fun FriendsDialogs(
    vm: GradosViewModel,
    friendsState: FriendsUiState,
    showFriendsDialog: Boolean,
    onShowFriendsDialogChange: (Boolean) -> Unit,
    showAddFriendDialog: Boolean,
    onShowAddFriendDialogChange: (Boolean) -> Unit,
    showListFriendsDialog: Boolean,
    onShowListFriendsDialogChange: (Boolean) -> Unit,
    selectedFriend: Friend?,
    onSelectedFriendChange: (Friend?) -> Unit,
    showDeleteFriendDialog: Boolean,
    onShowDeleteFriendDialogChange: (Boolean) -> Unit,
    friendToDelete: Friend?,
    onFriendToDeleteChange: (Friend?) -> Unit
) {
    if (showFriendsDialog) {
        AlertDialog(
            onDismissRequest = {
                onShowFriendsDialogChange(false)
            },
            title = {
                Text("Amigos")
            },
            text = {
                Column {
                    Button(
                        onClick = {
                            onShowFriendsDialogChange(false)
                            onShowAddFriendDialogChange(true)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Añadir amigo")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onShowFriendsDialogChange(false)
                            onSelectedFriendChange(null)
                            vm.loadFriends()
                            onShowListFriendsDialogChange(true)
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
                        onShowFriendsDialogChange(false)
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showAddFriendDialog) {
        var uidText by remember {
            mutableStateOf("")
        }

        var nombreText by remember {
            mutableStateOf("")
        }

        AlertDialog(
            onDismissRequest = {
                onShowAddFriendDialogChange(false)
            },
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

                    friendsState.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = uidText.trim().isNotBlank() &&
                            nombreText.trim().isNotBlank(),
                    onClick = {
                        vm.addFriend(
                            uidAmigo = uidText.trim(),
                            nombre = nombreText.trim()
                        )

                        onShowAddFriendDialogChange(false)
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onShowAddFriendDialogChange(false)
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showListFriendsDialog) {
        AlertDialog(
            onDismissRequest = {
                onShowListFriendsDialogChange(false)
                onSelectedFriendChange(null)
            },
            title = {
                Text("Tus amigos")
            },
            text = {
                Column {
                    when {
                        friendsState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        friendsState.friends.isEmpty() -> {
                            Text("Todavía no tienes amigos añadidos")
                        }

                        else -> {
                            friendsState.friends.forEach { friend ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectedFriendChange(friend)
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = friend.nombre,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (selectedFriend?.id == friend.id) {
                                        IconButton(
                                            onClick = {
                                                vm.prepareShareDataToFriend(friend)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = "Compartir mis técnicas"
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            onFriendToDeleteChange(friend)
                                            onShowDeleteFriendDialogChange(true)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar amigo",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }

                    friendsState.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        onShowListFriendsDialogChange(false)
                        onSelectedFriendChange(null)
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showDeleteFriendDialog && friendToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                onShowDeleteFriendDialogChange(false)
                onFriendToDeleteChange(null)
            },
            title = {
                Text("Eliminar amigo")
            },
            text = {
                Text(
                    "¿Quieres eliminar a ${friendToDelete.nombre} de tu lista de amigos? " +
                            "Esto no borrará sus grados ni sus técnicas."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteFriend(friendToDelete)

                        if (selectedFriend?.id == friendToDelete.id) {
                            onSelectedFriendChange(null)
                        }

                        onShowDeleteFriendDialogChange(false)
                        onFriendToDeleteChange(null)
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onShowDeleteFriendDialogChange(false)
                        onFriendToDeleteChange(null)
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
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