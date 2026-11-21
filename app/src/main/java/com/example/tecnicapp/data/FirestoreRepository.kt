package com.example.tecnicapp.data

import com.example.tecnicapp.model.Grado
import com.example.tecnicapp.ui.grados.TecnicaSeleccionable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun currentUserUid(): String {
        return auth.currentUser?.uid.orEmpty()
    }

    // GRADOS

    suspend fun getGrados(): Result<List<Grado>> {
        return try {
            val uid = currentUserUid()

            if (uid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val snapshot = db.collection("users")
                .document(uid)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            val grados = snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val nombre = doc.getString("nombre").orEmpty()

                if (nombre.isBlank()) {
                    null
                } else {
                    Grado(
                        id = id,
                        nombre = nombre,
                        numero = doc.getLong("numero")?.toInt() ?: 0
                    )
                }
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
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val nombreLimpio = nombre.trim()

            if (nombreLimpio.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("El nombre del grado está vacío")
                )
            }

            db.collection("users")
                .document(uid)
                .collection("grados")
                .add(
                    mapOf(
                        "nombre" to nombreLimpio,
                        "numero" to 0L
                    )
                )
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

            val nombreLimpio = nuevoNombre.trim()

            if (nombreLimpio.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("El nombre del grado está vacío")
                )
            }

            db.collection("users")
                .document(uid)
                .collection("grados")
                .document(gradoId)
                .update(
                    mapOf(
                        "nombre" to nombreLimpio
                    )
                )
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

            val tecnicas = db.collection("users")
                .document(uid)
                .collection("grados")
                .document(gradoId)
                .collection("tecnicas")
                .get(Source.SERVER)
                .await()

            for (tecnica in tecnicas.documents) {
                tecnica.reference.delete().await()
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

    // TÉCNICAS

    suspend fun getTecnicas(gradoId: String): Result<List<Map<String, Any?>>> {
        return try {
            val uid = currentUserUid()

            if (uid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val snapshot = db.collection("users")
                .document(uid)
                .collection("grados")
                .document(gradoId)
                .collection("tecnicas")
                .get(Source.SERVER)
                .await()

            val tecnicas = snapshot.documents.map { doc ->
                val datos = doc.data.orEmpty().toMutableMap()
                datos["id"] = doc.id
                datos
            }

            Result.success(tecnicas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTecnica(
        gradoId: String,
        datos: Map<String, Any?>
    ): Result<Unit> {
        return try {
            val uid = currentUserUid()

            if (uid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val datosLimpios = datos.toMutableMap()
            datosLimpios.remove("id")

            db.collection("users")
                .document(uid)
                .collection("grados")
                .document(gradoId)
                .collection("tecnicas")
                .add(datosLimpios)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTecnica(
        gradoId: String,
        tecnicaId: String,
        datos: Map<String, Any?>
    ): Result<Unit> {
        return try {
            val uid = currentUserUid()

            if (uid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val datosLimpios = datos.toMutableMap()
            datosLimpios.remove("id")

            db.collection("users")
                .document(uid)
                .collection("grados")
                .document(gradoId)
                .collection("tecnicas")
                .document(tecnicaId)
                .set(datosLimpios)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTecnica(
        gradoId: String,
        tecnicaId: String
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
                .collection("tecnicas")
                .document(tecnicaId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // AMIGOS

    suspend fun getFriends(): Result<List<Friend>> {
        return try {
            val uid = currentUserUid()

            if (uid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val snapshot = db.collection("users")
                .document(uid)
                .collection("friends")
                .get(Source.SERVER)
                .await()

            val friends = snapshot.documents.mapNotNull { doc ->
                val uidAmigo = doc.getString("uidAmigo").orEmpty()

                if (uidAmigo.isBlank()) {
                    null
                } else {
                    Friend(
                        id = doc.id,
                        uidAmigo = uidAmigo,
                        nombre = doc.getString("nombre").orEmpty()
                    )
                }
            }

            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addFriend(
        uidAmigo: String,
        nombre: String
    ): Result<Unit> {
        return try {
            val miUid = currentUserUid()

            android.util.Log.d(
                "TECNIAPP_FRIENDS",
                "Mi UID: $miUid"
            )

            android.util.Log.d(
                "TECNIAPP_FRIENDS",
                "UID amigo recibido: $uidAmigo"
            )

            if (miUid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val uidLimpio = uidAmigo.trim()

            if (uidLimpio.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("El UID del amigo está vacío")
                )
            }

            if (uidLimpio == miUid) {
                return Result.failure(
                    IllegalArgumentException(
                        "No puedes añadirte a ti mismo como amigo"
                    )
                )
            }

            val nombreLimpio = nombre.trim()

            if (nombreLimpio.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("El nombre del amigo está vacío")
                )
            }

            val usuarioAmigo = db.collection("users")
                .document(uidLimpio)
                .get(Source.SERVER)
                .await()

            android.util.Log.d(
                "TECNIAPP_FRIENDS",
                "¿Existe el usuario amigo?: ${usuarioAmigo.exists()}"
            )

            if (!usuarioAmigo.exists()) {
                return Result.failure(
                    IllegalArgumentException(
                        "No existe ningún usuario con ese UID"
                    )
                )
            }

            val nombreFirestore = usuarioAmigo.getString("nombre")
                ?.trim()
                .orEmpty()

            val nombreFinal = if (nombreFirestore.isNotBlank()) {
                nombreFirestore
            } else {
                nombreLimpio
            }

            db.collection("users")
                .document(miUid)
                .collection("friends")
                .document(uidLimpio)
                .set(
                    mapOf(
                        "uidAmigo" to uidLimpio,
                        "nombre" to nombreFinal,
                        "fechaCreacion" to System.currentTimeMillis()
                    )
                )
                .await()

            android.util.Log.d(
                "TECNIAPP_FRIENDS",
                "Amigo guardado en Firestore"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(
                "TECNIAPP_FRIENDS",
                "Excepción al añadir amigo",
                e
            )

            Result.failure(e)
        }
    }
    suspend fun crearSolicitudCompartirTecnicas(
        fromUid: String,
        toUid: String,
        tecnicas: List<TecnicaSeleccionable>
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
                        "No puedes compartir técnicas contigo misma"
                    )
                )
            }

            if (tecnicas.isEmpty()) {
                return Result.failure(
                    IllegalArgumentException(
                        "Selecciona al menos una técnica"
                    )
                )
            }

            val usuarioEmisor = db.collection("users")
                .document(fromUid)
                .get(Source.SERVER)
                .await()

            val email = usuarioEmisor.getString("email")
                ?.trim()
                .orEmpty()

            val fromNombre = if (email.contains("@")) {
                email.substringBefore("@")
            } else {
                email
            }

            if (fromNombre.isBlank()) {
                return Result.failure(
                    IllegalArgumentException(
                        "No se ha podido obtener el nombre del emisor"
                    )
                )
            }

            val tecnicasData = tecnicas.map { tecnica ->
                mapOf(
                    "gradoId" to tecnica.gradoId,
                    "gradoNombre" to tecnica.gradoNombre,
                    "gradoNumero" to tecnica.gradoNumero,
                    "tecnicaId" to tecnica.tecnicaId,
                    "nombre" to tecnica.nombre,
                    "datos" to tecnica.datos
                )
            }

            db.collection("shareRequests")
                .add(
                    mapOf(
                        "fromUid" to fromUid,
                        "toUid" to toUid,
                        "fromNombre" to fromNombre,
                        "estado" to "pendiente",
                        "fechaCreacion" to System.currentTimeMillis(),
                        "tecnicas" to tecnicasData
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getSolicitudesCompartirRecibidas(): Result<List<Map<String, Any?>>> {
        return try {
            val miUid = currentUserUid()

            if (miUid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val snapshot = db.collection("shareRequests")
                .whereEqualTo("toUid", miUid)
                .whereEqualTo("estado", "pendiente")
                .get(Source.SERVER)
                .await()

            val solicitudes = snapshot.documents.map { doc ->
                val datos = doc.data.orEmpty().toMutableMap()
                datos["id"] = doc.id
                datos
            }

            Result.success(solicitudes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aceptarSolicitudCompartirTecnicas(
        solicitudId: String
    ): Result<Int> {
        return try {
            val miUid = currentUserUid()

            if (miUid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            val solicitud = db.collection("shareRequests")
                .document(solicitudId)
                .get(Source.SERVER)
                .await()

            if (!solicitud.exists()) {
                return Result.failure(
                    IllegalArgumentException("La solicitud no existe")
                )
            }

            if (solicitud.getString("toUid") != miUid) {
                return Result.failure(
                    IllegalArgumentException(
                        "Esta solicitud no es para tu cuenta"
                    )
                )
            }

            if (solicitud.getString("estado") != "pendiente") {
                return Result.failure(
                    IllegalArgumentException(
                        "Esta solicitud ya ha sido gestionada"
                    )
                )
            }

            val tecnicasData = solicitud.get("tecnicas") as? List<*>
                ?: emptyList<Any>()

            var tecnicasAnadidas = 0

            for (item in tecnicasData) {
                val datosTecnica = item as? Map<*, *> ?: continue

                val nombreGrado = datosTecnica["gradoNombre"]?.toString()
                    ?.trim()
                    .orEmpty()

                val numeroGrado = (datosTecnica["gradoNumero"] as? Long ?: 0L).toInt()

                val nombreTecnica = datosTecnica["nombre"]?.toString()
                    ?.trim()
                    .orEmpty()

                if (nombreGrado.isBlank() || nombreTecnica.isBlank()) {
                    continue
                }

                val nombreGradoCopia = "Copia_$nombreGrado"

                val gradosExistentes = db.collection("users")
                    .document(miUid)
                    .collection("grados")
                    .whereEqualTo("nombre", nombreGradoCopia)
                    .get(Source.SERVER)
                    .await()

                val gradoDestinoId = if (gradosExistentes.documents.isNotEmpty()) {
                    gradosExistentes.documents.first().id
                } else {
                    val nuevoGrado = db.collection("users")
                        .document(miUid)
                        .collection("grados")
                        .add(
                            mapOf(
                                "nombre" to nombreGradoCopia,
                                "numero" to numeroGrado.toLong()
                            )
                        )
                        .await()

                    nuevoGrado.id
                }

                val tecnicasExistentes = db.collection("users")
                    .document(miUid)
                    .collection("grados")
                    .document(gradoDestinoId)
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                val nombresExistentes = tecnicasExistentes.documents
                    .mapNotNull { doc ->
                        doc.getString("nombre")
                            ?.trim()
                            ?.lowercase()
                    }
                    .toSet()

                val claveTecnica = nombreTecnica.lowercase()

                if (claveTecnica in nombresExistentes) {
                    continue
                }

                val datos = (datosTecnica["datos"] as? Map<*, *>)
                    ?.mapKeys { it.key.toString() }
                    ?.toMutableMap()
                    ?: mutableMapOf()

                datos.remove("id")

                db.collection("users")
                    .document(miUid)
                    .collection("grados")
                    .document(gradoDestinoId)
                    .collection("tecnicas")
                    .add(datos)
                    .await()

                tecnicasAnadidas++
            }

            db.collection("shareRequests")
                .document(solicitudId)
                .update(
                    mapOf(
                        "estado" to "aceptada",
                        "fechaRespuesta" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(tecnicasAnadidas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rechazarSolicitudCompartirTecnicas(
        solicitudId: String
    ): Result<Unit> {
        return try {
            val miUid = currentUserUid()

            if (miUid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            db.collection("shareRequests")
                .document(solicitudId)
                .update(
                    mapOf(
                        "estado" to "rechazada",
                        "fechaRespuesta" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteFriend(friendId: String): Result<Unit> {
        return try {
            val uid = currentUserUid()

            if (uid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            db.collection("users")
                .document(uid)
                .collection("friends")
                .document(friendId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // SELECCIÓN DE TÉCNICAS

    suspend fun getMisTecnicasSeleccionables(): Result<List<TecnicaSeleccionable>> {
        return getTecnicasSeleccionablesDeUsuario(
            uidUsuario = currentUserUid()
        )
    }

    suspend fun getTecnicasSeleccionablesDeUsuario(
        uidUsuario: String
    ): Result<List<TecnicaSeleccionable>> {
        return try {
            if (uidUsuario.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("El UID del usuario está vacío")
                )
            }

            val grados = db.collection("users")
                .document(uidUsuario)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            val tecnicas = mutableListOf<TecnicaSeleccionable>()

            for (grado in grados.documents) {
                val gradoId = grado.id
                val gradoNombre = grado.getString("nombre").orEmpty()
                val gradoNumero = grado.getLong("numero")?.toInt() ?: 0

                val tecnicasGrado = grado.reference
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                for (tecnica in tecnicasGrado.documents) {
                    tecnicas.add(
                        TecnicaSeleccionable(
                            gradoId = gradoId,
                            gradoNombre = gradoNombre,
                            gradoNumero = gradoNumero,
                            tecnicaId = tecnica.id,
                            nombre = tecnica.getString("nombre").orEmpty(),
                            datos = tecnica.data.orEmpty()
                        )
                    )
                }
            }

            Result.success(tecnicas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // COMPARTIR TÉCNICAS SELECCIONADAS

    suspend fun compartirTecnicasSeleccionadas(
        fromUid: String,
        toUid: String,
        tecnicas: List<TecnicaSeleccionable>
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
                        "No puedes compartir técnicas contigo misma"
                    )
                )
            }

            if (tecnicas.isEmpty()) {
                return Result.success(0)
            }

            val gradosDestino = db.collection("users")
                .document(toUid)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            val gradosDestinoPorNombre = gradosDestino.documents
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

            var tecnicasCompartidas = 0

            for (tecnica in tecnicas) {
                val nombreGradoOriginal = tecnica.gradoNombre.trim()

                if (nombreGradoOriginal.isBlank()) {
                    continue
                }

                val nombreGradoCopia = "Copia_$nombreGradoOriginal"
                val claveGradoCopia = nombreGradoCopia.lowercase()

                val gradoDestinoId = gradosDestinoPorNombre[claveGradoCopia]
                    ?: run {
                        val nuevoGrado = db.collection("users")
                            .document(toUid)
                            .collection("grados")
                            .add(
                                mapOf(
                                    "nombre" to nombreGradoCopia,
                                    "numero" to 0L
                                )
                            )
                            .await()

                        gradosDestinoPorNombre[claveGradoCopia] = nuevoGrado.id

                        nuevoGrado.id
                    }

                val tecnicasExistentes = db.collection("users")
                    .document(toUid)
                    .collection("grados")
                    .document(gradoDestinoId)
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                val nombresExistentes = tecnicasExistentes.documents
                    .mapNotNull { doc ->
                        doc.getString("nombre")
                            ?.trim()
                            ?.lowercase()
                    }
                    .toSet()

                val nombreTecnica = tecnica.nombre.trim()

                if (nombreTecnica.isBlank()) {
                    continue
                }

                val claveTecnica = nombreTecnica.lowercase()

                if (claveTecnica in nombresExistentes) {
                    continue
                }

                val datos = tecnica.datos.toMutableMap()
                datos.remove("id")

                db.collection("users")
                    .document(toUid)
                    .collection("grados")
                    .document(gradoDestinoId)
                    .collection("tecnicas")
                    .add(datos)
                    .await()

                tecnicasCompartidas++
            }

            Result.success(tecnicasCompartidas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CLONAR TÉCNICAS SELECCIONADAS

    suspend fun clonarTecnicasSeleccionadas(
        uidAmigo: String,
        tecnicas: List<TecnicaSeleccionable>
    ): Result<Int> {
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

            if (tecnicas.isEmpty()) {
                return Result.success(0)
            }

            val misGrados = db.collection("users")
                .document(miUid)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            val misGradosPorNombre = misGrados.documents
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

            for (tecnica in tecnicas) {
                val nombreGrado = tecnica.gradoNombre.trim()
                val numeroGrado = tecnica.gradoNumero

                if (nombreGrado.isBlank()) {
                    continue
                }

                val claveGrado = nombreGrado.lowercase()

                val miGradoId = misGradosPorNombre[claveGrado]
                    ?: run {
                        val nuevoGrado = db.collection("users")
                            .document(miUid)
                            .collection("grados")
                            .add(
                                mapOf(
                                    "nombre" to nombreGrado,
                                    "numero" to numeroGrado.toLong()
                                )
                            )
                            .await()

                        misGradosPorNombre[claveGrado] = nuevoGrado.id

                        nuevoGrado.id
                    }

                val misTecnicas = db.collection("users")
                    .document(miUid)
                    .collection("grados")
                    .document(miGradoId)
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                val nombresExistentes = misTecnicas.documents
                    .mapNotNull { doc ->
                        doc.getString("nombre")
                            ?.trim()
                            ?.lowercase()
                    }
                    .toMutableSet()

                val nombreTecnica = tecnica.nombre.trim()

                if (nombreTecnica.isBlank()) {
                    continue
                }

                val claveTecnica = nombreTecnica.lowercase()

                if (claveTecnica in nombresExistentes) {
                    continue
                }

                val datos = tecnica.datos.toMutableMap()
                datos.remove("id")

                db.collection("users")
                    .document(miUid)
                    .collection("grados")
                    .document(miGradoId)
                    .collection("tecnicas")
                    .add(datos)
                    .await()

                nombresExistentes.add(claveTecnica)
                tecnicasClonadas++
            }

            Result.success(tecnicasClonadas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // COMPATIBILIDAD: compartir todos los grados y técnicas
    suspend fun shareGradosAndTecnicasToUser(
        fromUid: String,
        toUid: String,
        overwrite: Boolean = false
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
                        "No puedes compartir técnicas contigo misma"
                    )
                )
            }

            val gradosOrigen = db.collection("users")
                .document(fromUid)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            var tecnicasCompartidas = 0

            for (gradoOrigen in gradosOrigen.documents) {
                val nombreGradoOrigen = gradoOrigen.getString("nombre")
                    ?.trim()
                    .orEmpty()

                if (nombreGradoOrigen.isBlank()) {
                    continue
                }

                val nombreGradoDestino = "Copia_$nombreGradoOrigen"

                val gradosDestino = db.collection("users")
                    .document(toUid)
                    .collection("grados")
                    .whereEqualTo("nombre", nombreGradoDestino)
                    .get(Source.SERVER)
                    .await()

                val gradoDestinoId = if (gradosDestino.documents.isNotEmpty()) {
                    gradosDestino.documents.first().id
                } else {
                    val nuevoGrado = db.collection("users")
                        .document(toUid)
                        .collection("grados")
                        .add(
                            mapOf(
                                "nombre" to nombreGradoDestino,
                                "numero" to 0L
                            )
                        )
                        .await()

                    nuevoGrado.id
                }

                if (overwrite) {
                    val tecnicasAntiguas = db.collection("users")
                        .document(toUid)
                        .collection("grados")
                        .document(gradoDestinoId)
                        .collection("tecnicas")
                        .get(Source.SERVER)
                        .await()

                    for (tecnica in tecnicasAntiguas.documents) {
                        tecnica.reference.delete().await()
                    }
                }

                val tecnicasOrigen = gradoOrigen.reference
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                val tecnicasDestino = db.collection("users")
                    .document(toUid)
                    .collection("grados")
                    .document(gradoDestinoId)
                    .collection("tecnicas")
                    .get(Source.SERVER)
                    .await()

                val nombresExistentes = tecnicasDestino.documents
                    .mapNotNull { doc ->
                        doc.getString("nombre")
                            ?.trim()
                            ?.lowercase()
                    }
                    .toMutableSet()

                for (tecnicaOrigen in tecnicasOrigen.documents) {
                    val nombreTecnica = tecnicaOrigen.getString("nombre")
                        ?.trim()
                        .orEmpty()

                    if (nombreTecnica.isBlank()) {
                        continue
                    }

                    val clave = nombreTecnica.lowercase()

                    if (!overwrite && clave in nombresExistentes) {
                        continue
                    }

                    val datos = tecnicaOrigen.data.orEmpty().toMutableMap()
                    datos.remove("id")

                    db.collection("users")
                        .document(toUid)
                        .collection("grados")
                        .document(gradoDestinoId)
                        .collection("tecnicas")
                        .add(datos)
                        .await()

                    nombresExistentes.add(clave)
                    tecnicasCompartidas++
                }
            }

            Result.success(tecnicasCompartidas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // COMPATIBILIDAD: clonar todos los grados y técnicas de un compañero
    suspend fun cloneTechniquesFromFriend(
        uidAmigo: String
    ): Result<Int> {
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

            val gradosAmigo = db.collection("users")
                .document(uidAmigo)
                .collection("grados")
                .get(Source.SERVER)
                .await()

            var tecnicasClonadas = 0

            for (gradoAmigo in gradosAmigo.documents) {
                val nombreGrado = gradoAmigo.getString("nombre")
                    ?.trim()
                    .orEmpty()

                val numeroGrado = gradoAmigo.getLong("numero")?.toInt() ?: 0

                if (nombreGrado.isBlank()) {
                    continue
                }

                val gradosMios = db.collection("users")
                    .document(miUid)
                    .collection("grados")
                    .whereEqualTo("nombre", nombreGrado)
                    .get(Source.SERVER)
                    .await()

                val miGradoId = if (gradosMios.documents.isNotEmpty()) {
                    gradosMios.documents.first().id
                } else {
                    val nuevoGrado = db.collection("users")
                        .document(miUid)
                        .collection("grados")
                        .add(
                            mapOf(
                                "nombre" to nombreGrado,
                                "numero" to numeroGrado.toLong()
                            )
                        )
                        .await()

                    nuevoGrado.id
                }

                val tecnicasAmigo = gradoAmigo.reference
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
                    .mapNotNull { doc ->
                        doc.getString("nombre")
                            ?.trim()
                            ?.lowercase()
                    }
                    .toMutableSet()

                for (tecnicaAmigo in tecnicasAmigo.documents) {
                    val nombreTecnica = tecnicaAmigo.getString("nombre")
                        ?.trim()
                        .orEmpty()

                    if (nombreTecnica.isBlank()) {
                        continue
                    }

                    val clave = nombreTecnica.lowercase()

                    if (clave in nombresExistentes) {
                        continue
                    }

                    val datos = tecnicaAmigo.data.orEmpty().toMutableMap()
                    datos.remove("id")

                    db.collection("users")
                        .document(miUid)
                        .collection("grados")
                        .document(miGradoId)
                        .collection("tecnicas")
                        .add(datos)
                        .await()

                    nombresExistentes.add(clave)
                    tecnicasClonadas++
                }
            }

            Result.success(tecnicasClonadas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}