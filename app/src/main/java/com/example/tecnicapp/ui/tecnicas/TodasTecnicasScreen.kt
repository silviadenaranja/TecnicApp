package com.example.tecnicapp.ui.tecnicas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.tecnicapp.data.TecnicasPorGrado
import com.example.tecnicapp.ui.grados.GradosSortOrder
import com.example.tecnicapp.ui.grados.SortMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodasTecnicasScreen(
    onBack: () -> Unit,
    onExportPdf: (List<TecnicasPorGrado>) -> Unit,
    vm: TodasTecnicasViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    var sortOrder by remember { mutableStateOf(GradosSortOrder.ASC) }

    val bloquesOrdenados = remember(state.bloques, sortOrder) {
        when (sortOrder) {
            GradosSortOrder.ASC -> state.bloques.sortedBy { it.grado.nombre.lowercase() }
            GradosSortOrder.DESC -> state.bloques.sortedByDescending { it.grado.nombre.lowercase() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todas las técnicas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    SortMenu(
                        sortOrder = sortOrder,
                        onSortChange = { sortOrder = it }
                    )
                    IconButton(onClick = { onExportPdf(bloquesOrdenados) }) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar PDF"
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
            when {
                state.isLoading && state.bloques.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                bloquesOrdenados.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay técnicas todavía")
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(bloquesOrdenados, key = { it.grado.id }) { bloque ->
                            BloqueTecnicasPorGrado(bloque)
                        }
                    }
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun BloqueTecnicasPorGrado(bloque: TecnicasPorGrado) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = bloque.grado.nombre,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        bloque.tecnicas.forEach { tecnica ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = tecnica.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (tecnica.alias.isNotBlank()) {
                        Text("Alias: ${tecnica.alias}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Posición: ${tecnica.posicion}", style = MaterialTheme.typography.bodySmall)
                    Text("Atacante: ${tecnica.atacante}", style = MaterialTheme.typography.bodySmall)
                    Text("Defensor: ${tecnica.defensor}", style = MaterialTheme.typography.bodySmall)
                    if (tecnica.altura.isNotBlank()) {
                        Text("Altura: ${tecnica.altura}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (tecnica.descripcion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Descripción: ${tecnica.descripcion}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}