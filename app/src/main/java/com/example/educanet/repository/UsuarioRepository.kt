package com.example.educanet.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UsuarioRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun registroUsuario(correo: String, clave: String, nombre: String, rol: String): Boolean {
        return try {

            val querySnapshot = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                return false
            }


            val rolFinal = when (rol.trim().lowercase()) {
                "profesor" -> "Profesor"
                "apoderado" -> "Apoderado"
                "administrador" -> "Administrador"
                else -> "Alumno"
            }


            val userData = hashMapOf(
                "correo" to correo,
                "clave" to clave,
                "nombre" to nombre,
                "rol" to rolFinal,
                "fechaRegistro" to getCurrentDate()
            )


            db.collection("usuario").add(userData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
