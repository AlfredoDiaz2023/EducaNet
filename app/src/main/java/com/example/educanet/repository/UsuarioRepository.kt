package com.example.educanet.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UsuarioRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun registroUsuario(correo: String, clave: String, nombre: String, rol: String): Boolean {
        return try {
            // Verificar si el correo ya existe
            val querySnapshot = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                return false // El correo ya está registrado
            }

            // Normalizar el rol (para evitar errores de mayúsculas o espacios)
            val rolFinal = when (rol.trim().lowercase()) {
                "profesor" -> "Profesor"
                "apoderado" -> "Apoderado"
                "administrador" -> "Administrador"
                else -> "Alumno"
            }

            // Crear mapa de datos del usuario
            val userData = hashMapOf(
                "correo" to correo,
                "clave" to clave,
                "nombre" to nombre,
                "rol" to rolFinal,
                "fechaRegistro" to getCurrentDate()
            )

            // Agregar documento a la colección "usuario"
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
