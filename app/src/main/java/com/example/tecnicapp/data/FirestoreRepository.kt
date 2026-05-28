package com.example.tecnicapp.data

import com.example.tecnicapp.model.Grado
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun gradosCollection() =
        auth.currentUser?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("grados")
        } ?: throw IllegalStateException("Usuario no autenticado")

    suspend fun getGrados(): Result<List<Grado>> {
        return try {
            val snapshot = gradosCollection()
                .get()
                .await()

            val grados = snapshot.documents.map { doc ->
                Grado(
                    id = doc.id,                       // id autogenerado del doc
                    nombre = doc.getString("nombre") ?: ""
                )
            }

            Result.success(grados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addGrado(nombre: String): Result<Unit> {
        return try {
            val trimmed = nombre.trim()
            if (trimmed.isEmpty()) return Result.success(Unit)

            val data = mapOf("nombre" to trimmed)
            gradosCollection().add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGrado(gradoId: String, nuevoNombre: String): Result<Unit> {
        return try {
            val trimmed = nuevoNombre.trim()
            if (trimmed.isEmpty()) return Result.success(Unit)

            gradosCollection()
                .document(gradoId)
                .update("nombre", trimmed)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGrado(gradoId: String): Result<Unit> {
        return try {
            // Nota: aquí solo borras el doc del grado; si quieres borrar las técnicas
            // de ese grado también, habría que recorrer la subcolección "tecnicas".
            gradosCollection()
                .document(gradoId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}