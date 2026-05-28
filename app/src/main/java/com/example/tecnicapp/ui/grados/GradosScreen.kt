package com.example.tecnicapp.ui.grados

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sort
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tecnicapp.model.Grado
import com.example.tecnicapp.ui.auth.AuthViewModel
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff


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
    val changePassState = authVm.changePassState  // <<< AQUÍ leemos el estado de cambio de contraseña

    var showDialog by remember { mutableStateOf(false) }
    var editingGrado by remember { mutableStateOf<Grado?>(null) }
    var nombreTemp by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(GradosSortOrder.ASC) }
    var showDeleteGradoDialog by remember { mutableStateOf(false) }
    var gradoToDelete by remember { mutableStateOf<Grado?>(null) }
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
                actions = {
                    SortMenu(
                        sortOrder = sortOrder,
                        onSortChange = { sortOrder = it }
                    )

                    IconButton(onClick = onVerTodasTecnicas) {
                        Icon(Icons.Default.List, contentDescription = "Ver todas las técnicas")
                    }
                 //pdf
                 //   IconButton(onClick = { vm.importarTecnicas() }) {
                 //       Icon(
                 //           imageVector = Icons.Default.Download,
                 //           contentDescription = "Importar técnicas"
                 //       )
                 //   }

                    // Perfil: cambiar contraseña
                    IconButton(onClick = {
                        authVm.clearChangePasswordState()
                        showChangePassDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil"
                        )
                    }

                    // Cerrar sesión
                    IconButton(onClick = { onLoggedOut() }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión"
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
                        color = androidx.compose.ui.graphics.Color.Red
                    )
                }
            }

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
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
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
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
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
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        androidx.compose.ui.text.input.PasswordVisualTransformation()
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
                    Text(it, color = androidx.compose.ui.graphics.Color.Red)
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
                Text("Cerrar")
            }
        }
    )
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