package com.example.tecnicapp.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException

fun mensajeErrorEnEspanol(error: Throwable?): String {
    return when (error) {
        is FirebaseNetworkException ->
            "No se ha podido conectar. Comprueba tu conexión a Internet."

        is FirebaseTooManyRequestsException ->
            "Se han realizado demasiados intentos. Espera un momento y vuelve a intentarlo."

        is FirebaseAuthException -> {
            when (error.errorCode) {
                "ERROR_INVALID_EMAIL" ->
                    "El correo electrónico no tiene un formato válido."

                "ERROR_WRONG_PASSWORD",
                "ERROR_USER_NOT_FOUND",
                "ERROR_INVALID_CREDENTIAL",
                "ERROR_INVALID_LOGIN_CREDENTIALS" ->
                    "El correo o la contraseña no son correctos."

                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    "Ya existe una cuenta con ese correo electrónico."

                "ERROR_WEAK_PASSWORD" ->
                    "La contraseña es demasiado débil. Elige una más segura."

                "ERROR_USER_DISABLED" ->
                    "Esta cuenta está deshabilitada."

                "ERROR_REQUIRES_RECENT_LOGIN" ->
                    "Por seguridad, cierra sesión y vuelve a entrar antes de realizar esta acción."

                "ERROR_OPERATION_NOT_ALLOWED" ->
                    "Este método de acceso no está habilitado."

                else ->
                    "No se ha podido completar la operación con tu cuenta."
            }
        }

        is FirebaseFirestoreException -> {
            when (error.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "No tienes permisos para realizar esta acción."

                FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                    "Tu sesión no es válida. Vuelve a iniciar sesión."

                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "No se puede conectar con el servicio. Comprueba tu conexión e inténtalo de nuevo."

                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                    "La operación ha tardado demasiado. Vuelve a intentarlo."

                FirebaseFirestoreException.Code.NOT_FOUND ->
                    "Los datos que buscas ya no están disponibles."

                FirebaseFirestoreException.Code.ALREADY_EXISTS ->
                    "Los datos que intentas crear ya existen."

                FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ->
                    "El servicio ha alcanzado su límite de uso. Inténtalo más tarde."

                FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                    "No se puede completar esta operación en este momento."

                else ->
                    "No se han podido guardar o consultar los datos."
            }
        }

        else ->
            "Ha ocurrido un error inesperado. Vuelve a intentarlo."
    }
}