package com.example.educanet.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class AlumnoRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun registroAlumno(correo: String, clave: String, nombre: String, rol: String): Boolean {
        return try {
            // 1. Verificar si el correo ya existe en la colección UNIFICADA "usuario"
            val querySnapshot = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                return false // Correo ya registrado
            }

            // 2. Preparamos los datos incluyendo fotoUrl
            val userData = hashMapOf(
                "correo" to correo,
                "clave" to clave,
                "nombre" to nombre,
                "rol" to "Alumno",
                "fechaRegistro" to getCurrentDate(),
                "fotoUrl" to null // <--- ¡ESTO ES LO QUE FALTABA!
            )

            // 3. Guardamos en la colección "usuario"
            db.collection("usuario").add(userData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}