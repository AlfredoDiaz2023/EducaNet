package com.example.educanet.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ProfesorRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun registroProfesor(correo: String, clave: String, nombre: String, rol: String): Boolean {
        return try {
            // 1. Verificar si el correo ya existe en la colección unificada "usuario"
            val querySnapshot = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                return false // El correo ya está registrado
            }

            // 2. Preparamos los datos
            val userData = hashMapOf(
                "correo" to correo,
                "clave" to clave,
                "nombre" to nombre,
                "rol" to "Profesor",
                "fechaRegistro" to getCurrentDate(),
                "fotoUrl" to null // <--- ¡ESTO ES LO QUE FALTABA!
            )

            // 3. Guardamos en la colección "usuario"
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