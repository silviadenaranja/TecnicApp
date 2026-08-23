package com.example.tecnicapp.data

import com.example.tecnicapp.model.Grado
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Source

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
                return Result.success(emptyList())
            }

            android.util.Log.d(
                "TECNIAPP_FIRESTORE",
                "getGrados uid=$uid"
            )

            val snapshot = db.collection("users")
                .document(uid)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            val grados = snapshot.documents.map { doc ->
                Grado(
                    id = doc.id,
                    nombre = doc.getString("nombre").orEmpty(),
                    numero = (doc.getLong("numero") ?: 0L).toInt()
                )
            }

            android.util.Log.d(
                "TECNIAPP_FIRESTORE",
                "getGrados uid=$uid, documentos=${grados.size}"
            )

            Result.success(grados)
        } catch (e: Exception) {
            android.util.Log.e(
                "TECNIAPP_FIRESTORE",
                "Error leyendo grados",
                e
            )

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

            val data = mapOf(
                "nombre" to nombre,
                "numero" to 0
            )
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
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val gradoRef = db.collection("users")
                .document(uid)
                .collection("grados")
                .document(gradoId)

            android.util.Log.w(
                "TECNIAPP_DELETE_GRADO",
                "Borrando grado completo: uid=$uid, gradoId=$gradoId"
            )

            // 1. Leer todas las técnicas que contiene el grado.
            val tecnicasSnapshot = gradoRef
                .collection("tecnicas")
                .get(Source.SERVER)
                .await()

            // 2. Eliminar uno por uno los documentos de técnicas.
            for (tecnicaDoc in tecnicasSnapshot.documents) {
                android.util.Log.d(
                    "TECNIAPP_DELETE_GRADO",
                    "Borrando técnica: tecnicaId=${tecnicaDoc.id}"
                )

                tecnicaDoc.reference
                    .delete()
                    .await()
            }

            // 3. Cuando ya no existen técnicas, eliminar el documento del grado.
            gradoRef
                .delete()
                .await()

            android.util.Log.w(
                "TECNIAPP_DELETE_GRADO",
                "Grado eliminado correctamente: gradoId=$gradoId"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(
                "TECNIAPP_DELETE_GRADO",
                "Error eliminando grado completo: gradoId=$gradoId",
                e
            )

            Result.failure(e)
        }
    }

    // ---------------- COPIAR GRADOS Y TÉCNICAS A OTRO UID ----------------


    suspend fun userHasGrados(uid: String): Result<Boolean> {
        return try {
            if (uid.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("El UID del amigo está vacío")
                )
            }

            val snapshot = db.collection("users")
                .document(uid)
                .collection("grados")
                .limit(1)
                .get(Source.SERVER)
                .await()

            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun copyGradosAndTecnicasToUser(
        fromUid: String,
        toUid: String
    ): Result<Unit> {
        return try {
            if (fromUid.isBlank() || toUid.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("UID origen o destino vacío")
                )
            }

            val gradosOrigen = db.collection("users")
                .document(fromUid)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            for (gradoOrigen in gradosOrigen.documents) {
                val datosGrado = gradoOrigen.data ?: continue

                /*
                 * Conservamos el ID del grado origen.
                 * Con set() evitamos crear un documento nuevo aleatorio.
                 */
                val gradoDestino = db.collection("users")
                    .document(toUid)
                    .collection("grados")
                    .document(gradoOrigen.id)

                gradoDestino
                    .set(datosGrado)
                    .await()

                val tecnicasOrigen = gradoOrigen.reference
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                for (tecnicaOrigen in tecnicasOrigen.documents) {
                    val datosTecnica = tecnicaOrigen.data ?: continue

                    /*
                     * Conservamos también el ID de técnica.
                     * No usar add(), porque crea duplicados.
                     */
                    gradoDestino
                        .collection("tecnicas")
                        .document(tecnicaOrigen.id)
                        .set(datosTecnica)
                        .await()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun deleteGradosAndTecnicasOfUser(
        uid: String
    ): Result<Unit> {
        return try {
            if (uid.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("El UID del usuario está vacío")
                )
            }

            val gradosRef = db.collection("users")
                .document(uid)
                .collection("grados")

            val gradosSnapshot = gradosRef
                .get(Source.SERVER)
                .await()

            for (gradoDoc in gradosSnapshot.documents) {
                val tecnicasSnapshot = gradoDoc.reference
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                /*
                 * Primero borrar documentos de técnicas.
                 */
                for (tecnicaDoc in tecnicasSnapshot.documents) {
                    tecnicaDoc.reference
                        .delete()
                        .await()
                }

                /*
                 * Después borrar el documento del grado.
                 */
                gradoDoc.reference
                    .delete()
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun overwriteGradosAndTecnicasToUser(
        fromUid: String,
        toUid: String
    ): Result<Unit> {
        return try {
            if (fromUid.isBlank() || toUid.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("UID origen o destino vacío")
                )
            }

            if (fromUid == toUid) {
                return Result.failure(
                    IllegalArgumentException(
                        "No puedes sobrescribir tus propios datos"
                    )
                )
            }

            val deleteResult = deleteGradosAndTecnicasOfUser(toUid)

            if (deleteResult.isFailure) {
                return Result.failure(
                    deleteResult.exceptionOrNull()
                        ?: IllegalStateException("No se pudieron borrar los datos previos")
                )
            }

            copyGradosAndTecnicasToUser(
                fromUid = fromUid,
                toUid = toUid
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}