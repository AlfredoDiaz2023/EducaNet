package com.example.educanet.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ProfesorRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun registroProfesor(correo: String, clave: String, nombre: String, rol: String): Boolean {
        return try {

            val querySnapshot = db.collection("Profesor")
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
                "rol" to "Profesor",
                "fechaRegistro" to getCurrentDate()
            )


            db.collection("Profesor").add(userData).await()
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