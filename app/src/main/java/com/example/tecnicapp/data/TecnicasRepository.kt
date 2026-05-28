package com.example.tecnicapp.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class TecnicaGlobal(
    val id: String = "",
    val nombre: String = "",
    val gradoNombre: String = "",
    val gradoId: String = ""
)

class TecnicasRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Carga todas las técnicas recorriendo todos los grados
    suspend fun cargarTodasLasTecnicas(): List<TecnicaGlobal> {
        val resultado = mutableListOf<TecnicaGlobal>()

        // 1) Leer todos los grados
        val gradosSnapshot = db.collection("grados").get().await()

        for (gradoDoc in gradosSnapshot.documents) {
            val gradoId = gradoDoc.id
            val gradoNombre = gradoDoc.getString("nombre") ?: ""

            // 2) Leer subcolección "tecnicas" de cada grado
            val tecnicasSnapshot = db.collection("grados")
                .document(gradoId)
                .collection("tecnicas")
                .get()
                .await()

            for (tecDoc in tecnicasSnapshot.documents) {
                val tecnica = TecnicaGlobal(
                    id = tecDoc.id,
                    nombre = tecDoc.getString("nombre") ?: "",
                    gradoNombre = gradoNombre,
                    gradoId = gradoId
                )
                resultado.add(tecnica)
            }
        }

        return resultado
    }
}