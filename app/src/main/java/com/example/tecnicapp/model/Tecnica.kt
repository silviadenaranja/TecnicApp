package com.example.tecnicapp.model

data class Tecnica(
    val id: String = "",
    val nombre: String = "",
    val alias: String = "",
    val posicion: String = "",
    val atacante: String = "",
    val defensor: String = "",
    val altura: String = "",
    val descripcion: String = "",
    val orden: Int = 0            // <--- campo de orden persistente
)