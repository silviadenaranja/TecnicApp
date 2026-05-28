package com.example.tecnicapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


// Texto plano del PDF (tu constante actual)
private const val TECNICAS_PDF_TEXT = """
GRADO: 1 kyu
• morote tsuki nuki   
• katate oshi nuki 
• kata muna otoshi
• eri juji 
• tsubame gaeshi
• chidori gaeshi
• sode nuki
• sode dori
• hiki otoshi
• harai uke geri 
• sode maki 
• sode maki tembin
• tsuki ten ni
• furi ten ni 
• eri nuki
• ude maki 
• morote juji nuki
• morote juji gote 
• tsuki ten san 
• keri ten san  
• morote hiki nuki
• maki gote
GRADO: 2 Kyu
• uchi uke zuki (omote)   Posición: hiraki "misma Guardia" en espejo   Atacante: gyaku tsuki   Defensor: uchi uke tsuki   Altura: puñetazo al cuerpo
• Soto oshi zuki geri
• yoko tenshin geri
• ryote okuri gote
• kusshin tsuki
• Sankaku nuki
• oshi kiri nuki
• kusshin tsuki geri
• johaku nuki
• han tanshin geri
• kon ten ichi
• kusshin geri
• Kiri gaeshi nuki
• uchi oshi uke geri
• juji nuki
• gasho nuki
GRADO: 3 Kyu
• juji nuki
• nidan nuki
• juji uke geri
• Soto oshi uke zuki
• tsuki ten ichi
• gyaku gote Ura gaeshi nage
• hijo nuki mae tembin
• ude juji Tate gassho gatame   Alias: ude juji tirando al suelo Palma hacia arriba
• uchi uke geri Ura y omote
• tsuki nuki
• maki nuki
• juji gote
• ryote yori nuki
• uchi oshi uke suki
GRADO: 4 kyu
• uchi age zuki ( Ura y omote)
• katate okuri gote
• uchi age geri (Ura y omote)
• Soto uke zuki (Ura y omote)
• okuri maki tembin
• tsuki nuki
• Soto uke geri (Ura y omote)
• kiri nuki
GRADO: 5 Kyu
• ryusui geri
• uwa uke geri (ura y omote)
• katate yori nuki 
• maki nuki
• shita uke geri 
• shita uke jun geri 
• gyaku gote
GRADO: 6 Kyu
GRADO: Bases
• han tenka 
• zen tenka 
• me uchi
• uwa uke
• uchi uke 
• shita uke 
• ichiji gamae
• chudan gamae 
• hasso gamae 
• gedan gamae
GRADO: Posicion y desplazamientos
• tai sabaki   Alias: movimiento de la postura
• kani ashi   Alias: el cangrejo
• tai gamae   Alias: distinta Guardia ( mismo pie alante)
• juji ashi   Alias: cruzando
• kumo ashi   Alias: la araña   Descripción: desplazamiento
• hiraki gamae   Alias: Guardia en espejo (giro aquí)
GRADO: Tenchiken - Ryouken - Giwaken
• Byakuren ken da ikkei   Alias: el de la rama
• Ryou ken da ikkei
• Dai go
• giwa ken Dai ikkei   Alias: el 1
• Dai san
• Dai ni
• Dai yon
• da ikki   Alias: el 1
• Dai Rokku
• giwa ken Dai ni   Alias: el 2
GRADO: Ukemis
• mae ukemi   Alias: hacia adelante
• oten Yori okiagari   Alias: croqueta
• Dai sharin   Alias: voltereta rueda
• ushiro ukemi   Alias: hacia atras
"""

/**
 * Carga un mapa nombreDeGrado -> documento de grado del USUARIO (users/{uid}/grados).
 */
private suspend fun cargarMapaGradosPorNombreUsuario(
    db: FirebaseFirestore,
    auth: FirebaseAuth
): Map<String, String> {
    val uid = auth.currentUser?.uid
        ?: throw IllegalStateException("Usuario no autenticado")

    val snapshot = db.collection("users")
        .document(uid)
        .collection("grados")
        .get()
        .await()

    return snapshot.documents.mapNotNull { doc ->
        val nombre = doc.getString("nombre") ?: return@mapNotNull null
        nombre to doc.id
    }.toMap()
}

suspend fun importarTecnicasDesdePdf() {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // 1) Mapa nombreDelGrado -> id del grado del usuario actual
    val gradoIds = cargarMapaGradosPorNombreUsuario(db, auth)

    // 2) Parsear texto -> gradoNombre -> lista de nombres de técnicas
    var gradoActual: String? = null
    val mapaTecnicasPorGrado = mutableMapOf<String, MutableList<String>>()

    TECNICAS_PDF_TEXT.lines().forEach { rawLine ->
        val line = rawLine.trim()
        if (line.isEmpty()) return@forEach

        if (line.startsWith("GRADO:")) {
            gradoActual = line.removePrefix("GRADO:").trim()
        } else if (line.startsWith("•")) {
            val sinBola = line.removePrefix("•").trim()
            val corte = listOf(
                "Alias:", "Posición:", "Posicion:",
                "Atacante:", "Defensor:", "Altura:",
                "Descripción:", "Descripcion:"
            ).map { token ->
                sinBola.indexOf(token).takeIf { it >= 0 } ?: Int.MAX_VALUE
            }.minOrNull() ?: Int.MAX_VALUE

            val nombreTecnica = if (corte == Int.MAX_VALUE) sinBola else sinBola.substring(0, corte)
            val key = gradoActual ?: return@forEach
            val list = mapaTecnicasPorGrado.getOrPut(key) { mutableListOf() }
            list.add(nombreTecnica.trim())
        }
    }

    // 3) Escribir en users/{uid}/grados/{gradoId}/tecnicas
    val uid = auth.currentUser?.uid
        ?: throw IllegalStateException("Usuario no autenticado")
    android.util.Log.d("IMPORT", "Mapa grados: $gradoIds")

    mapaTecnicasPorGrado.forEach { (gradoNombre, tecnicas) ->
        val gradoId = gradoIds[gradoNombre] ?: return@forEach
        android.util.Log.d("IMPORT", "Procesando gradoNombre='$gradoNombre' -> gradoId=$gradoId, numTecnicas=${tecnicas.size}")
        if (gradoId == null) {
            android.util.Log.w("IMPORT", "NO se encontró grado con nombre '$gradoNombre' para este usuario")
            return@forEach
        }
        val colRef = db.collection("users")
            .document(uid)
            .collection("grados")
            .document(gradoId)
            .collection("tecnicas")

        tecnicas.forEachIndexed { index, nombreTecnica ->
            android.util.Log.d("IMPORT", "Guardando tecnica '$nombreTecnica' en gradoId=$gradoId, orden=$index")
            val data = hashMapOf(
                "nombre" to nombreTecnica,
                "alias" to "",
                "posicion" to "",
                "atacante" to "",
                "defensor" to "",
                "altura" to "",
                "descripcion" to "",
                "orden" to index
            )
            colRef.add(data).await()
        }
    }
}