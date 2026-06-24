package com.example.tecnicapp.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AppUser(
    val uid: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun currentUser(): FirebaseUser? = auth.currentUser

    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Crear documento en Firestore para este uid
                val appUser = AppUser(
                    uid = user.uid,
                    email = user.email ?: email
                )

                db.collection("users")
                    .document(user.uid)   // id = uid de auth (buena práctica) [web:613]
                    .set(appUser)
                    .await()

                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Usuario nulo tras registro"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Usuario nulo tras login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("No hay usuario logueado"))

        val email = user.email
            ?: return Result.failure(IllegalStateException("El usuario no tiene email"))

        return try {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}