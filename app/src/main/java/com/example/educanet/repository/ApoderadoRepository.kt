package com.example.educanet.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ApoderadoRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun registroApoderado(correo: String, clave: String, nombre: String, rol: String): Boolean {
        return try {
            val querySnapshot = db.collection("usuario") // Buscar en "usuario"
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                return false
            }

            val userData = hashMapOf(
                "correo" to correo,
                "clave" to clave,
                "nombre" to nombre,
                "rol" to "Apoderado",
                "fechaRegistro" to getCurrentDate(),
                "fotoUrl" to null // <--- ¡ESTO ES LO QUE FALTABA!
            )

            db.collection("usuario").add(userData).await() // Guardar en "usuario"
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