package com.example.tecnicapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Friend(
    val id: String = "",
    val uidAmigo: String = "",
    val nombre: String = ""
)
data class FriendsUiState(
    val isLoading: Boolean = false,
    val friends: List<Friend> = emptyList(),
    val error: String? = null,
    val friendAdded: Boolean = false
)
class FriendsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun currentUid(): String {
        return auth.currentUser?.uid.orEmpty()
    }

    private fun friendsCollection() = db.collection("users")
        .document(currentUid())
        .collection("friends")

    suspend fun addFriend(
        uidAmigo: String,
        nombre: String
    ): Result<Unit> {
        return try {
            val myUid = currentUid()

            if (myUid.isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            if (uidAmigo.isBlank() || nombre.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("El UID y el nombre son obligatorios")
                )
            }

            if (uidAmigo == myUid) {
                return Result.failure(
                    IllegalArgumentException("No puedes añadirte a ti misma como amiga")
                )
            }

            val existingFriend = friendsCollection()
                .whereEqualTo("uidAmigo", uidAmigo)
                .get()
                .await()

            if (!existingFriend.isEmpty) {
                return Result.failure(
                    IllegalArgumentException("Este amigo ya está en tu lista")
                )
            }

            val data = mapOf(
                "uidAmigo" to uidAmigo.trim(),
                "nombre" to nombre.trim()
            )

            friendsCollection()
                .add(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriends(): Result<List<Friend>> {
        return try {
            val uid = currentUid()

            if (uid.isBlank()) {
                return Result.success(emptyList())
            }

            val snapshot = friendsCollection()
                .get()
                .await()

            val friends = snapshot.documents.map { doc ->
                Friend(
                    id = doc.id,
                    uidAmigo = doc.getString("uidAmigo").orEmpty(),
                    nombre = doc.getString("nombre").orEmpty()
                )
            }.sortedBy { it.nombre.lowercase() }

            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFriend(friendId: String): Result<Unit> {
        return try {
            if (currentUid().isBlank()) {
                return Result.failure(
                    IllegalStateException("No hay usuario autenticado")
                )
            }

            if (friendId.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("No se ha encontrado el amigo")
                )
            }

            friendsCollection()
                .document(friendId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}