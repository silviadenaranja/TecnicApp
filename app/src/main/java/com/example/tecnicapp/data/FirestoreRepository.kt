package com.example.tecnicapp.data

import com.example.tecnicapp.model.Grado
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun currentUserUid(): String {
        return auth.currentUser?.uid.orEmpty()
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
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val data = mapOf(
                "nombre" to nombre,
                "numero" to 0L
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

    suspend fun updateGrado(
        gradoId: String,
        nuevoNombre: String
    ): Result<Unit> {
        return try {
            val uid = currentUserUid()

            if (uid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
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

            val tecnicasSnapshot = gradoRef
                .collection("tecnicas")
                .get(Source.SERVER)
                .await()

            for (tecnicaDoc in tecnicasSnapshot.documents) {
                android.util.Log.d(
                    "TECNIAPP_DELETE_GRADO",
                    "Borrando técnica: tecnicaId=${tecnicaDoc.id}"
                )

                tecnicaDoc.reference
                    .delete()
                    .await()
            }

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

//suspend fun copyGradosAndTecnicasToUser(
//    fromUid: String,
//    toUid: String
//): Result<Unit> {
//    return try {
//        if (fromUid.isBlank() || toUid.isBlank()) {
//            return Result.failure(
//                IllegalArgumentException("UID origen o destino vacío")
//            )
//        }

//        if (fromUid == toUid) {
//            return Result.failure(
//                IllegalArgumentException(
//                    "No puedes copiar tus datos a tu propia cuenta"
//                )
//            )
//        }

//        val gradosOrigen = db.collection("users")
//            .document(fromUid)
//            .collection("grados")
//            .get(Source.SERVER)
//            .await()

//        for (gradoOrigen in gradosOrigen.documents) {
//            val datosGrado = gradoOrigen.data ?: continue

//            val gradoDestino = db.collection("users")
//                .document(toUid)
//                .collection("grados")
//                .document(gradoOrigen.id)

//            gradoDestino
//                .set(datosGrado)
//                .await()

//            val tecnicasOrigen = gradoOrigen.reference
//                .collection("tecnicas")
//                .get(Source.SERVER)
//                .await()

//            for (tecnicaOrigen in tecnicasOrigen.documents) {
//                val datosTecnica = tecnicaOrigen.data ?: continue

//                gradoDestino
//                    .collection("tecnicas")
//                    .document(tecnicaOrigen.id)
//                    .set(datosTecnica)
//                    .await()
//            }
//        }
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }


    suspend fun shareGradosAndTecnicasToUser(
        fromUid: String,
        toUid: String
    ): Result<Int> {
        return try {
            if (fromUid.isBlank() || toUid.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("UID origen o destino vacío")
                )
            }

            if (fromUid == toUid) {
                return Result.failure(
                    IllegalArgumentException(
                        "No puedes compartir tus datos contigo misma"
                    )
                )
            }

            val gradosOrigen = db.collection("users")
                .document(fromUid)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            val gradosDestino = db.collection("users")
                .document(toUid)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            /*
             * Nombres de grados que ya existen en la cuenta destino.
             * Se comparan en minúsculas para evitar duplicados por
             * mayúsculas o espacios.
             */
            val nombresGradosDestino = gradosDestino.documents
                .mapNotNull { doc ->
                    doc.getString("nombre")
                        ?.trim()
                        ?.lowercase()
                }
                .toMutableSet()

            var gradosCopiados = 0

            for (gradoOrigen in gradosOrigen.documents) {
                val nombreOriginal = gradoOrigen.getString("nombre")
                    ?.trim()
                    .orEmpty()

                if (nombreOriginal.isBlank()) {
                    continue
                }

                val nombreCopia = "Copia_$nombreOriginal"
                val claveCopia = nombreCopia.lowercase()

                /*
                 * Si ya se compartió antes, no se vuelve a crear
                 * ni se duplican técnicas.
                 */
                if (claveCopia in nombresGradosDestino) {
                    continue
                }

                val nuevoGradoRef = db.collection("users")
                    .document(toUid)
                    .collection("grados")
                    .add(
                        mapOf(
                            "nombre" to nombreCopia,
                            "numero" to (gradoOrigen.getLong("numero") ?: 0L)
                        )
                    )
                    .await()

                nombresGradosDestino.add(claveCopia)
                gradosCopiados++

                val tecnicasOrigen = gradoOrigen.reference
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                for (tecnicaOrigen in tecnicasOrigen.documents) {
                    val datosTecnica = tecnicaOrigen.data ?: continue

                    datosTecnica.remove("id")

                    nuevoGradoRef
                        .collection("tecnicas")
                        .add(datosTecnica)
                        .await()
                }
            }

            Result.success(gradosCopiados)
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

                for (tecnicaDoc in tecnicasSnapshot.documents) {
                    tecnicaDoc.reference
                        .delete()
                        .await()
                }

                gradoDoc.reference
                    .delete()
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//  suspend fun overwriteGradosAndTecnicasToUser(
//      fromUid: String,
//      toUid: String
//  ): Result<Unit> {
//      return try {
//          if (fromUid.isBlank() || toUid.isBlank()) {
//              return Result.failure(
//                  IllegalArgumentException("UID origen o destino vacío")
//              )
//          }

//          if (fromUid == toUid) {
//              return Result.failure(
//                  IllegalArgumentException(
//                      "No puedes sobrescribir tus propios datos"
//                  )
//              )
//          }

//          val deleteResult = deleteGradosAndTecnicasOfUser(toUid)

//          if (deleteResult.isFailure) {
//              return Result.failure(
//                  deleteResult.exceptionOrNull()
//                      ?: IllegalStateException("No se pudieron borrar los datos previos")
//              )
//          }

//          copyGradosAndTecnicasToUser(
//              fromUid = fromUid,
//              toUid = toUid
//          )
//      } catch (e: Exception) {
//          Result.failure(e)
//      }
//  }



    // ---------------- CLONAR TÉCNICAS DE UN COMPAÑERO ----------------

    suspend fun cloneTechniquesFromFriend(uidAmigo: String): Result<Int> {
        return try {
            val miUid = currentUserUid()

            if (miUid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            if (uidAmigo.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("El UID del compañero está vacío")
                )
            }

            if (uidAmigo == miUid) {
                return Result.failure(
                    IllegalArgumentException(
                        "No puedes clonar técnicas de tu propia cuenta"
                    )
                )
            }

            android.util.Log.d(
                "TECNIAPP_CLONE",
                "Clonando desde uidAmigo=$uidAmigo hacia miUid=$miUid"
            )

            val gradosAmigo = db.collection("users")
                .document(uidAmigo)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            val misGradosSnapshot = db.collection("users")
                .document(miUid)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            val misGradosPorNombre = misGradosSnapshot.documents
                .mapNotNull { doc ->
                    val nombre = doc.getString("nombre")
                        ?.trim()
                        ?.lowercase()
                        .orEmpty()

                    if (nombre.isBlank()) {
                        null
                    } else {
                        nombre to doc.id
                    }
                }
                .toMap()
                .toMutableMap()

            var tecnicasClonadas = 0

            for (gradoAmigoDoc in gradosAmigo.documents) {
                val nombreGrado = gradoAmigoDoc.getString("nombre")
                    ?.trim()
                    .orEmpty()

                if (nombreGrado.isBlank()) {
                    continue
                }

                val claveGrado = nombreGrado.lowercase()

                val miGradoIdExistente = misGradosPorNombre[claveGrado]

                val miGradoId: String

                if (miGradoIdExistente != null) {
                    miGradoId = miGradoIdExistente
                } else {
                    val nuevoGradoRef = db.collection("users")
                        .document(miUid)
                        .collection("grados")
                        .add(
                            mapOf(
                                "nombre" to nombreGrado,
                                "numero" to (gradoAmigoDoc.getLong("numero") ?: 0L)
                            )
                        )
                        .await()

                    miGradoId = nuevoGradoRef.id

                    misGradosPorNombre[claveGrado] = miGradoId
                }

                val tecnicasAmigo = db.collection("users")
                    .document(uidAmigo)
                    .collection("grados")
                    .document(gradoAmigoDoc.id)
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                val misTecnicas = db.collection("users")
                    .document(miUid)
                    .collection("grados")
                    .document(miGradoId)
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                val nombresExistentes = misTecnicas.documents
                    .mapNotNull { tecnicaDoc ->
                        tecnicaDoc.getString("nombre")
                            ?.trim()
                            ?.lowercase()
                    }
                    .toMutableSet()

                for (tecnicaAmigoDoc in tecnicasAmigo.documents) {
                    val nombreTecnica = tecnicaAmigoDoc.getString("nombre")
                        ?.trim()
                        .orEmpty()

                    if (nombreTecnica.isBlank()) {
                        continue
                    }

                    val claveTecnica = nombreTecnica.lowercase()

                    if (claveTecnica in nombresExistentes) {
                        continue
                    }

                    val datosTecnica = tecnicaAmigoDoc.data.orEmpty()
                        .toMutableMap()

                    datosTecnica.remove("id")

                    db.collection("users")
                        .document(miUid)
                        .collection("grados")
                        .document(miGradoId)
                        .collection("tecnicas")
                        .add(datosTecnica)
                        .await()

                    nombresExistentes.add(claveTecnica)
                    tecnicasClonadas++
                }
            }

            android.util.Log.d(
                "TECNIAPP_CLONE",
                "Técnicas clonadas: $tecnicasClonadas"
            )

            Result.success(tecnicasClonadas)
        } catch (e: Exception) {
            android.util.Log.e(
                "TECNIAPP_CLONE",
                "Error clonando técnicas",
                e
            )

            Result.failure(e)
        }
    }
}