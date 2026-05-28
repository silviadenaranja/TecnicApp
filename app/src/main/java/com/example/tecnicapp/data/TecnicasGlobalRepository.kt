package com.example.tecnicapp.data

import com.example.tecnicapp.model.Grado
import com.example.tecnicapp.model.Tecnica
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class TecnicasPorGrado(
    val grado: Grado,
    val tecnicas: List<Tecnica>
)

class TecnicasGlobalRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun gradosCollection() =
        auth.currentUser?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("grados")
        } ?: throw IllegalStateException("Usuario no autenticado")

    suspend fun getTodasTecnicas(): Result<List<TecnicasPorGrado>> {
        return try {
            val gradosSnap = gradosCollection().get().await()
            val list = mutableListOf<TecnicasPorGrado>()

            for (gradoDoc in gradosSnap.documents) {
                val grado = Grado(
                    id = gradoDoc.id,
                    nombre = gradoDoc.getString("nombre") ?: ""
                )

                val tecnicasSnap = gradosCollection()
                    .document(grado.id)
                    .collection("tecnicas")
                    .orderBy("orden")
                    .get()
                    .await()

                val tecnicas = tecnicasSnap.documents.map { doc ->
                    Tecnica(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        alias = doc.getString("alias") ?: "",
                        posicion = doc.getString("posicion") ?: "",
                        atacante = doc.getString("atacante") ?: "",
                        defensor = doc.getString("defensor") ?: "",
                        altura = doc.getString("altura") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        orden = (doc.getLong("orden") ?: 0L).toInt()
                    )
                }

                list.add(TecnicasPorGrado(grado, tecnicas))
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}