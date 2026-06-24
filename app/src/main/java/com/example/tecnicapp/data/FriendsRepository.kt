package com.example.tecnicapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Friend(
    val id: String = "",
    val uidAmigo: String = "",
    val nombre: String = ""
)

class FriendsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun friendsCollection() = db.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("friends")

    suspend fun addFriend(uidAmigo: String, nombre: String): Result<Unit> {
        val data = mapOf(
            "uidAmigo" to uidAmigo,
            "nombre" to nombre
        )

        return try {
            friendsCollection()
                .add(data)
                .await()  // requiere kotlinx-coroutines-play-services
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriends(): Result<List<Friend>> {
        return try {
            val snapshot = friendsCollection()
                .get()
                .await()

            val friends = snapshot.documents.map { doc ->
                Friend(
                    id = doc.id,
                    uidAmigo = doc.getString("uidAmigo") ?: "",
                    nombre = doc.getString("nombre") ?: ""
                )
            }

            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}