package com.example.tecnicapp.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tecnicapp.data.importarTecnicasDesdePdf
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    fun importarTecnicas() {
        viewModelScope.launch {
            importarTecnicasDesdePdf()
        }
    }
}