package com.example.tecnicapp.model

data class Grado(
    val id: String = "",
    val nombre: String = "",
    val numero: Int = 0
)    // Para Firestore: sin-arg ctor + types simples
