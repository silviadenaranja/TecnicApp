package com.example.tecnicapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tecnicapp.model.Grado
import com.example.tecnicapp.pdf.generateTecnicasPdf
import com.example.tecnicapp.ui.admin.AdminViewModel
import com.example.tecnicapp.ui.grados.GradosScreen
import com.example.tecnicapp.ui.tecnicas.TecnicasScreen
import com.example.tecnicapp.ui.tecnicas.TecnicasViewModel
import com.example.tecnicapp.ui.tecnicas.TodasTecnicasScreen
import com.example.tecnicapp.ui.theme.TecnicAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createPdfChannel()

        setContent {
            TecnicAppTheme {
                App()
            }
        }
    }

    private fun createPdfChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "pdf_download_channel"
            val name = "Exportación de PDF"
            val description = "Notificaciones cuando se generan PDFs"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

@Composable
private fun App() {
    val context = LocalContext.current

    // Estado de navegación
    var selectedGrado by remember { mutableStateOf<Grado?>(null) }
    var showTodasTecnicas by remember { mutableStateOf(false) }

    // Back físico / gesto atrás
    BackHandler(enabled = true) {
        when {
            showTodasTecnicas -> showTodasTecnicas = false
            selectedGrado != null -> selectedGrado = null
            else -> (context as? ComponentActivity)?.finish()
        }
    }
    // ViewModel de admin para importar
    val adminVm: AdminViewModel = viewModel()

    BackHandler(enabled = true) {
        when {
            showTodasTecnicas -> showTodasTecnicas = false
            selectedGrado != null -> selectedGrado = null
            else -> (context as? ComponentActivity)?.finish()
        }
    }

    when {
        showTodasTecnicas -> {
            TodasTecnicasScreen(
                onBack = { showTodasTecnicas = false },
                onExportPdf = { bloquesOrdenados ->
                    generateTecnicasPdf(context, bloquesOrdenados)
                }
            )
        }

        selectedGrado == null -> {
            GradosScreen(
                onGradoClicked = { grado -> selectedGrado = grado },
                onVerTodasTecnicas = { showTodasTecnicas = true }
            )
        }

        else -> {
            val grado = selectedGrado!!
            // Ajusta el constructor del ViewModel si el tuyo es distinto
            val tecnicasViewModel = remember(grado.id) {
                TecnicasViewModel(gradoId = grado.id)
            }

            TecnicasScreen(
                grado = grado,
                vm = tecnicasViewModel,
                onBack = { selectedGrado = null }
            )
        }
    }
}