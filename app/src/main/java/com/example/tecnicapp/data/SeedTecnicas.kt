package com.example.tecnicapp.data

data class TecnicaSeed(
    val gradoId: String,
    val nombre: String,
    val alias: String = "",
    val posicion: String = "",
    val atacante: String = "",
    val defensor: String = "",
    val altura: String = "",
    val descripcion: String = ""
)

// Aquí metes TODAS tus técnicas iniciales
val tecnicasSeed: List<TecnicaSeed> = listOf(
    TecnicaSeed(
        gradoId = "blanco",
        nombre = "Oizuki",
        alias = "Puño frontal",
        posicion = "Zenkutsu dachi",
        atacante = "Frontal",
        defensor = "",
        altura = "Chudan",
        descripcion = "Puño directo al centro"
    ),
    // ... aquí añades el resto que tienes en memoria
)