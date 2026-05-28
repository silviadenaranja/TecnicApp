package com.example.tecnicapp.ui.tecnicas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.window.Dialog
import com.example.tecnicapp.model.Grado
import com.example.tecnicapp.model.Tecnica

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicasScreen(
    grado: Grado,
    vm: TecnicasViewModel,
    onBack: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingTecnica by remember { mutableStateOf<Tecnica?>(null) }

    var nombreTemp by remember { mutableStateOf("") }
    var aliasTemp by remember { mutableStateOf("") }
    var posicionTemp by remember { mutableStateOf("") }
    var atacanteTemp by remember { mutableStateOf("") }
    var defensorTemp by remember { mutableStateOf("") }
    var alturaTemp by remember { mutableStateOf("") }
    var descripcionTemp by remember { mutableStateOf("") }
    var showDeleteTecnicaDialog by remember { mutableStateOf(false) }
    var tecnicaToDelete by remember { mutableStateOf<Tecnica?>(null) }

    fun openNew() {
        editingTecnica = null
        nombreTemp = ""
        aliasTemp = ""
        posicionTemp = ""
        atacanteTemp = ""
        defensorTemp = ""
        alturaTemp = ""
        descripcionTemp = ""
        showDialog = true
    }

    fun openEdit(tecnica: Tecnica) {
        editingTecnica = tecnica
        nombreTemp = tecnica.nombre
        aliasTemp = tecnica.alias
        posicionTemp = tecnica.posicion
        atacanteTemp = tecnica.atacante
        defensorTemp = tecnica.defensor
        alturaTemp = tecnica.altura
        descripcionTemp = tecnica.descripcion
        showDialog = true
    }

    fun save() {
        val n = nombreTemp.trim()
        if (n.isEmpty()) return

        if (editingTecnica == null) {
            vm.addTecnica(
                nombre = n,
                alias = aliasTemp,
                posicion = posicionTemp,
                atacante = atacanteTemp,
                defensor = defensorTemp,
                altura = alturaTemp,
                descripcion = descripcionTemp
            )
        } else {
            vm.updateTecnica(
                tecnica = editingTecnica!!,
                nombre = n,
                alias = aliasTemp,
                posicion = posicionTemp,
                atacante = atacanteTemp,
                defensor = defensorTemp,
                altura = alturaTemp,
                descripcion = descripcionTemp
            )
        }
        showDialog = false
        editingTecnica = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(grado.nombre) },
                navigationIcon = {
                    IconButton(onClick = {
                        vm.persistirOrden()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { openNew() }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva técnica")
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
                if (state.isLoading && state.tecnicas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.tecnicas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay técnicas para este grado")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(state.tecnicas, key = { _, item -> item.id }) { index, tecnica ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}. ${tecnica.nombre}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = tecnica.nombre,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            if (tecnica.alias.isNotBlank()) {
                                                Text(
                                                    text = "Alias: ${tecnica.alias}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Text(
                                                text = "Posición: ${tecnica.posicion}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Atacante: ${tecnica.atacante}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Defensor: ${tecnica.defensor}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            if (tecnica.altura.isNotBlank()) {
                                                Text(
                                                    text = "Altura: ${tecnica.altura}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }

                                        Column {
                                            IconButton(
                                                onClick = {
                                                    if (index > 0) {
                                                        vm.moveTecnica(index, index - 1)
                                                        vm.persistirOrden()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowUpward,
                                                    contentDescription = "Subir"
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (index < state.tecnicas.lastIndex) {
                                                        vm.moveTecnica(index, index + 1)
                                                        vm.persistirOrden()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDownward,
                                                    contentDescription = "Bajar"
                                                )
                                            }
                                        }

                                        IconButton(onClick = { openEdit(tecnica) }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar"
                                            )
                                        }

                                        IconButton(onClick = {
                                            tecnicaToDelete = tecnica
                                            showDeleteTecnicaDialog = true
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar"
                                            )
                                        }
                                    }

                                    if (tecnica.descripcion.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = tecnica.descripcion,
                                            style = MaterialTheme.typography.bodyMedium
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
                TecnicaDialog(
                    isEditing = editingTecnica != null,
                    nombre = nombreTemp,
                    alias = aliasTemp,
                    posicion = posicionTemp,
                    atacante = atacanteTemp,
                    defensor = defensorTemp,
                    altura = alturaTemp,
                    descripcion = descripcionTemp,
                    onNombreChange = { nombreTemp = it },
                    onAliasChange = { aliasTemp = it },
                    onPosicionChange = { posicionTemp = it },
                    onAtacanteChange = { atacanteTemp = it },
                    onDefensorChange = { defensorTemp = it },
                    onAlturaChange = { alturaTemp = it },
                    onDescripcionChange = { descripcionTemp = it },
                    onSave = { save() },
                    onDismiss = {
                        showDialog = false
                        editingTecnica = null
                    }
                )
            }

            if (showDeleteTecnicaDialog && tecnicaToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteTecnicaDialog = false
                        tecnicaToDelete = null
                    },
                    title = { Text("Eliminar técnica") },
                    text = { Text("¿Seguro que quieres eliminar esta técnica? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        TextButton(onClick = {
                            vm.deleteTecnica(tecnicaToDelete!!)
                            showDeleteTecnicaDialog = false
                            tecnicaToDelete = null
                        }) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteTecnicaDialog = false
                            tecnicaToDelete = null
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TecnicaDialog(
    isEditing: Boolean,
    nombre: String,
    alias: String,
    posicion: String,
    atacante: String,
    defensor: String,
    altura: String,
    descripcion: String,
    onNombreChange: (String) -> Unit,
    onAliasChange: (String) -> Unit,
    onPosicionChange: (String) -> Unit,
    onAtacanteChange: (String) -> Unit,
    onDefensorChange: (String) -> Unit,
    onAlturaChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = if (isEditing) "Editar técnica" else "Nueva técnica",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = alias,
                    onValueChange = onAliasChange,
                    label = { Text("Alias") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = posicion,
                        onValueChange = onPosicionChange,
                        label = { Text("Posición") },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = altura,
                        onValueChange = onAlturaChange,
                        label = { Text("Altura") },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = atacante,
                        onValueChange = onAtacanteChange,
                        label = { Text("Atacante") },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = defensor,
                        onValueChange = onDefensorChange,
                        label = { Text("Defensor") },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = onDescripcionChange,
                    label = { Text("Descripción") },
                    minLines = 2,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onSave) {
                        Text("Guardar", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}