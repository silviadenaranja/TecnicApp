package com.example.tecnicapp.data

import com.example.tecnicapp.model.Grado
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun currentUserUid(): String {
        val uid = auth.currentUser?.uid.orEmpty()
        // Opcional para debug:
        // android.util.Log.d("FirestoreRepository", "currentUserUid = $uid")
        return uid
    }

    // ---------------- GRADOS ----------------

    suspend fun getGrados(): Result<List<Grado>> {
        return try {
            val uid = currentUserUid()
            if (uid.isBlank()) {
                // No hay usuario logueado: devolver lista vacía
                return Result.success(emptyList())
            }

            val snapshot = db.collection("users")
                .document(uid)
                .collection("grados")
                .get()
                .await()

            val grados = snapshot.documents.map { doc ->
                Grado(
                    id = doc.id,
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
            val uid = currentUserUid()
            if (uid.isBlank()) {
                // No hay usuario: no escribas nada y devuelve error/controlado
                return Result.failure(IllegalStateException("No hay usuario autenticado"))
            }

            val data = mapOf("nombre" to nombre)
            db.collection("users")
                .document(uid)
                .collection("grados")
                .add(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGrado(gradoId: String, nuevoNombre: String): Result<Unit> {
        return try {
            val uid = currentUserUid()
            if (uid.isBlank()) {
                return Result.failure(IllegalStateException("No hay usuario autenticado"))
            }

            db.collection("users")
                .document(uid)
                .collection("grados")
                .document(gradoId)
                .update("nombre", nuevoNombre)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGrado(gradoId: String): Result<Unit> {
        return try {
            val uid = currentUserUid()
            if (uid.isBlank()) {
                return Result.failure(IllegalStateException("No hay usuario autenticado"))
            }

            db.collection("users")
                .document(uid)
                .collection("grados")
                .document(gradoId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------------- COPIAR GRADOS Y TÉCNICAS A OTRO UID ----------------

    suspend fun copyGradosAndTecnicasToUser(
        fromUid: String,
        toUid: String
    ): Result<Unit> {
        return try {
            if (fromUid.isBlank() || toUid.isBlank()) {
                return Result.failure(IllegalStateException("UID origen o destino vacío"))
            }

            // 1. Leer grados del usuario origen
            val gradosSnapshot = db.collection("users")
                .document(fromUid)
                .collection("grados")
                .get()
                .await()

            for (gradoDoc in gradosSnapshot.documents) {
                val gradoData = gradoDoc.data ?: continue

                // Crear un nuevo grado en el usuario destino
                val newGradoRef = db.collection("users")
                    .document(toUid)
                    .collection("grados")
                    .document()

                newGradoRef.set(gradoData).await()

                // 2. Copiar técnicas de cada grado
                val tecnicasSnapshot = gradoDoc.reference
                    .collection("tecnicas")
                    .get()
                    .await()

                for (tecnicaDoc in tecnicasSnapshot.documents) {
                    val tecnicaData = tecnicaDoc.data ?: continue
                    newGradoRef
                        .collection("tecnicas")
                        .add(tecnicaData)
                        .await()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}