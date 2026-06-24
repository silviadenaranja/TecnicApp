package com.example.tecnicapp.ui.grados

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.List
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tecnicapp.data.Friend
import com.example.tecnicapp.model.Grado
import com.example.tecnicapp.ui.auth.AuthViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

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
    val changePassState = authVm.changePassState

    var showDialog by remember { mutableStateOf(false) }
    var editingGrado by remember { mutableStateOf<Grado?>(null) }
    var nombreTemp by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(GradosSortOrder.ASC) }
    var showDeleteGradoDialog by remember { mutableStateOf(false) }
    var gradoToDelete by remember { mutableStateOf<Grado?>(null) }

    // Amigos
    var showFriendsDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showListFriendsDialog by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    var showConfirmSendDialog by remember { mutableStateOf(false) }
    // Menú hamburguesa
    var menuExpanded by remember { mutableStateOf(false) }

    // Cambio de contraseña
    var showChangePassDialog by remember { mutableStateOf(false) }

    val gradosOrdenados = remember(state.grados, sortOrder) {
        when (sortOrder) {
            GradosSortOrder.ASC -> state.grados.sortedBy { it.nombre.lowercase() }
            GradosSortOrder.DESC -> state.grados.sortedByDescending { it.nombre.lowercase() }
        }
    }

    fun openNew() {
        editingGrado = null
        nombreTemp = ""
        showDialog = true
    }

    fun openEdit(grado: Grado) {
        editingGrado = grado
        nombreTemp = grado.nombre
        showDialog = true
    }

    fun save() {
        val nombre = nombreTemp.trim()
        if (nombre.isEmpty()) return

        if (editingGrado == null) {
            vm.addGrado(nombre)
        } else {
            vm.updateGrado(editingGrado!!, nombre)
        }
        showDialog = false
        editingGrado = null
        nombreTemp = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis grados") },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú"
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            // Cambiar contraseña
                            DropdownMenuItem(
                                text = { Text("Cambiar contraseña") },
                                onClick = {
                                    menuExpanded = false
                                    authVm.clearChangePasswordState()
                                    showChangePassDialog = true
                                }
                            )

                            // Descargar / Importar técnicas
                            DropdownMenuItem(
                                text = { Text("Importar técnicas") },
                                onClick = {
                                    menuExpanded = false
                                    // vm.importarTecnicas()
                                }
                            )

                            // Amigos
                            DropdownMenuItem(
                                text = { Text("Amigos") },
                                onClick = {
                                    menuExpanded = false
                                    showFriendsDialog = true
                                }
                            )

                            // Cerrar sesión
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    menuExpanded = false
                                    onLoggedOut()
                                }
                            )
                        }
                    }
                },
                actions = {
                    // Ordenar
                    SortMenu(
                        sortOrder = sortOrder,
                        onSortChange = { sortOrder = it }
                    )

                    // Ver todas las técnicas
                    IconButton(onClick = onVerTodasTecnicas) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Ver todas las técnicas"
                        )
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
            Column(modifier = Modifier.fillMaxSize()) {

                Button(
                    onClick = { openNew() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir nuevo grado")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading && state.grados.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (gradosOrdenados.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay grados creados todavía")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(gradosOrdenados, key = { it.id }) { grado ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onGradoClicked(grado) }
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

                                    IconButton(onClick = { openEdit(grado) }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar"
                                        )
                                    }

                                    IconButton(onClick = {
                                        gradoToDelete = grado
                                        showDeleteGradoDialog = true
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar grado"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                state.error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.Red
                    )
                }
            }

            // Diálogo crear/editar grado
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        editingGrado = null
                        nombreTemp = ""
                    },
                    title = {
                        Text(if (editingGrado == null) "Nuevo grado" else "Editar grado")
                    },
                    text = {
                        OutlinedTextField(
                            value = nombreTemp,
                            onValueChange = { nombreTemp = it },
                            label = { Text("Nombre del grado") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Button(onClick = { save() }) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            editingGrado = null
                            nombreTemp = ""
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo eliminar grado
            if (showDeleteGradoDialog && gradoToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteGradoDialog = false
                        gradoToDelete = null
                    },
                    title = { Text("Eliminar grado") },
                    text = {
                        Text("¿Seguro que quieres eliminar este grado y todas sus técnicas? Esta acción no se puede deshacer.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            vm.deleteGrado(gradoToDelete!!)
                            showDeleteGradoDialog = false
                            gradoToDelete = null
                        }) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteGradoDialog = false
                            gradoToDelete = null
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo de cambiar contraseña
            if (showChangePassDialog) {
                ChangePasswordDialog(
                    state = changePassState,
                    onDismiss = {
                        showChangePassDialog = false
                        authVm.clearChangePasswordState()
                    },
                    onChangePassword = { current, new ->
                        authVm.changePassword(current, new)
                    }
                )
            }

            // Diálogos de amigos
            FriendsDialogs(
                vm = vm,
                showFriendsDialog = showFriendsDialog,
                onShowFriendsDialogChange = { showFriendsDialog = it },
                showAddFriendDialog = showAddFriendDialog,
                onShowAddFriendDialogChange = { showAddFriendDialog = it },
                showListFriendsDialog = showListFriendsDialog,
                onShowListFriendsDialogChange = { showListFriendsDialog = it },
                selectedFriend = selectedFriend,
                onSelectedFriendChange = { selectedFriend = it },
                showConfirmSendDialog = showConfirmSendDialog,
                onShowConfirmSendDialogChange = { showConfirmSendDialog = it }
            )
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    state: AuthViewModel.ChangePasswordUiState,
    onDismiss: () -> Unit,
    onChangePassword: (current: String, new: String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var newPass2 by remember { mutableStateOf("") }

    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showNew2 by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña") },
        text = {
            Column {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Contraseña actual") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showCurrent) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        val icon = if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        val desc = if (showCurrent) "Ocultar" else "Mostrar"
                        IconButton(onClick = { showCurrent = !showCurrent }) {
                            Icon(icon, contentDescription = desc)
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showNew) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        val icon = if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        val desc = if (showNew) "Ocultar" else "Mostrar"
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(icon, contentDescription = desc)
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPass2,
                    onValueChange = { newPass2 = it },
                    label = { Text("Repite nueva contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showNew2) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        val icon = if (showNew2) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        val desc = if (showNew2) "Ocultar" else "Mostrar"
                        IconButton(onClick = { showNew2 = !showNew2 }) {
                            Icon(icon, contentDescription = desc)
                        }
                    }
                )

                state.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color.Red)
                }
                if (state.success) {
                    Spacer(Modifier.height(8.dp))
                    Text("Contraseña actualizada correctamente")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !state.isLoading &&
                        newPass.isNotBlank() &&
                        newPass == newPass2,
                onClick = { onChangePassword(current, newPass) }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun FriendsDialogs(
    vm: GradosViewModel,
    showFriendsDialog: Boolean,
    onShowFriendsDialogChange: (Boolean) -> Unit,
    showAddFriendDialog: Boolean,
    onShowAddFriendDialogChange: (Boolean) -> Unit,
    showListFriendsDialog: Boolean,
    onShowListFriendsDialogChange: (Boolean) -> Unit,
    selectedFriend: Friend?,
    onSelectedFriendChange: (Friend?) -> Unit,
    showConfirmSendDialog: Boolean,
    onShowConfirmSendDialogChange: (Boolean) -> Unit
) {
    val friendsState = vm.friendsState

    // Diálogo principal amigos
    if (showFriendsDialog) {
        AlertDialog(
            onDismissRequest = { onShowFriendsDialogChange(false) },
            title = { Text("Amigos") },
            text = {
                Column {
                    Button(onClick = {
                        onShowFriendsDialogChange(false)
                        onShowAddFriendDialogChange(true)
                    }) {
                        Text("Añadir amigo")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        onShowFriendsDialogChange(false)
                        vm.loadFriends()
                        onShowListFriendsDialogChange(true)
                    }) {
                        Text("Ver amigos")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { onShowFriendsDialogChange(false) }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Añadir amigo
    if (showAddFriendDialog) {
        var uidText by remember { mutableStateOf("") }
        var nombreText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { onShowAddFriendDialogChange(false) },
            title = { Text("Añadir amigo") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uidText,
                        onValueChange = { uidText = it },
                        label = { Text("UID de Firestore") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombreText,
                        onValueChange = { nombreText = it },
                        label = { Text("Nombre") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.addFriend(uidText.trim(), nombreText.trim())
                        onShowAddFriendDialogChange(false)
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { onShowAddFriendDialogChange(false) }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Ver amigos
    if (showListFriendsDialog) {
        AlertDialog(
            onDismissRequest = { onShowListFriendsDialogChange(false) },
            title = { Text("Tus amigos") },
            text = {
                Column {
                    if (friendsState.isLoading) {
                        CircularProgressIndicator()
                    } else if (friendsState.friends.isEmpty()) {
                        Text("Todavía no tienes amigos añadidos")
                    } else {
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
                                    IconButton(onClick = {
                                        onShowConfirmSendDialogChange(true)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Pasar datos"
                                        )
                                    }
                                }
                            }
                        }
                    }
                    friendsState.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = Color.Red)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { onShowListFriendsDialogChange(false) }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Confirmar pasar datos
    if (showConfirmSendDialog && selectedFriend != null) {
        AlertDialog(
            onDismissRequest = { onShowConfirmSendDialogChange(false) },
            title = { Text("Pasar datos") },
            text = {
                Text("¿Seguro que quieres pasar tus grados y técnicas a ${selectedFriend.nombre}?")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.sendMyDataToFriend(selectedFriend.uidAmigo)
                    onShowConfirmSendDialogChange(false)
                    onShowListFriendsDialogChange(false)
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowConfirmSendDialogChange(false) }) {
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
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.Sort, contentDescription = "Ordenar grados")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Ascendente") },
            onClick = {
                onSortChange(GradosSortOrder.ASC)
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("Descendente") },
            onClick = {
                onSortChange(GradosSortOrder.DESC)
                expanded = false
            }
        )
    }
}