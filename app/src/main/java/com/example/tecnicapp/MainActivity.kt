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
import com.example.tecnicapp.ui.auth.AuthViewModel
import com.example.tecnicapp.ui.auth.LoginScreen
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

    // ViewModel de autenticación (compartido por Login y resto de pantallas)
    val authVm: AuthViewModel = viewModel()
    val authState by authVm.uiState.collectAsState()

    // Estado de navegación interna (solo cuando está logueado)
    var selectedGrado by remember { mutableStateOf<Grado?>(null) }
    var showTodasTecnicas by remember { mutableStateOf(false) }

    // ViewModel de admin (si lo usas para otras cosas)
    val adminVm: AdminViewModel = viewModel()

    // Back físico / gesto atrás
    BackHandler(enabled = true) {
        when {
            !authState.isLoggedIn -> {
                // En la pantalla de login: sale de la app
                (context as? ComponentActivity)?.finish()
            }
            showTodasTecnicas -> showTodasTecnicas = false
            selectedGrado != null -> selectedGrado = null
            else -> (context as? ComponentActivity)?.finish()
        }
    }

    if (!authState.isLoggedIn) {
        // Pantalla de LOGIN
        LoginScreen(
            onLoggedIn = {
                // Cuando AuthViewModel marca isLoggedIn = true,
                // este if se reevalúa y se entra en el flujo de grados.
                // No hace falta hacer nada especial aquí.
            },
            vm = authVm
        )
    } else {
        // Flujo principal de la app (grados / técnicas)
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
                    onVerTodasTecnicas = { showTodasTecnicas = true },
                    // Cuando cerramos sesión:
                    onLoggedOut = {
                        authVm.logout()
                        // logout() ya pone isLoggedIn = false,
                        // así que al recomponer volverás a LoginScreen.
                        selectedGrado = null
                        showTodasTecnicas = false
                    }
                )
            }

            else -> {
                val grado = selectedGrado!!
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
}