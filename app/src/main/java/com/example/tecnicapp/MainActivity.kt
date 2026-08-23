package com.example.tecnicapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tecnicapp.model.Grado
import com.example.tecnicapp.pdf.generateTecnicasPdf
import com.example.tecnicapp.ui.auth.AuthViewModel
import com.example.tecnicapp.ui.auth.LoginScreen
import com.example.tecnicapp.ui.grados.GradosScreen
import com.example.tecnicapp.ui.grados.GradosViewModel
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
            val channel = NotificationChannel(
                "pdf_download_channel",
                "Exportación de PDF",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando se generan PDFs"
            }

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}

@Composable
private fun App() {
    val context = LocalContext.current

    val authVm: AuthViewModel = viewModel()
    val authState by authVm.uiState.collectAsState()

    /*
     * El UID es parte de la key del GradosViewModel:
     * Lucía y Silvia nunca reutilizarán el mismo ViewModel de grados.
     */
    val currentUid = authState.uid
    var selectedGrado by remember { mutableStateOf<Grado?>(null) }
    var showTodasTecnicas by remember { mutableStateOf(false) }

    /*
     * Al cambiar UID, por login, logout o cambio de cuenta:
     * limpiamos la navegación que pudiera pertenecer al usuario anterior.
     */
    LaunchedEffect(currentUid) {
        selectedGrado = null
        showTodasTecnicas = false
    }

    BackHandler(enabled = true) {
        when {
            !authState.isLoggedIn -> {
                (context as? ComponentActivity)?.finish()
            }

            showTodasTecnicas -> {
                showTodasTecnicas = false
            }

            selectedGrado != null -> {
                selectedGrado = null
            }

            else -> {
                (context as? ComponentActivity)?.finish()
            }
        }
    }

    if (!authState.isLoggedIn) {
        LoginScreen(
            onLoggedIn = {},
            vm = authVm
        )
        return
    }

    if (currentUid.isBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val gradosVm: GradosViewModel = viewModel(
        key = "grados_view_model_$currentUid"
    )

    LaunchedEffect(currentUid) {
        gradosVm.clearUserData()
        gradosVm.loadGrados()
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
                vm = gradosVm,
                authVm = authVm,
                onGradoClicked = { grado ->
                    selectedGrado = grado
                },
                onVerTodasTecnicas = {
                    showTodasTecnicas = true
                },
                onLoggedOut = {
                    gradosVm.clearUserData()
                    authVm.logout()

                    selectedGrado = null
                    showTodasTecnicas = false
                }
            )
        }

        else -> {
            val grado = selectedGrado!!

            val tecnicasVm = remember(currentUid, grado.id) {
                TecnicasViewModel(gradoId = grado.id)
            }

            TecnicasScreen(
                grado = grado,
                vm = tecnicasVm,
                onBack = {
                    selectedGrado = null
                }
            )
        }
    }
}