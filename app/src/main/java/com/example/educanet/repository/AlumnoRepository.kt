package com.example.educanet.repository

import com.example.educanet.model.Alumno // Importación necesaria para el nuevo método
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class AlumnoRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "Alumno"

    /**
     * FUNCIÓN EXISTENTE: Registra un nuevo alumno en Firestore.
     */
    suspend fun registroAlumno(correo: String, clave: String, nombre: String, rol: String): Boolean {
        return try {
            val querySnapshot = db.collection(collectionName)
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                return false // El correo ya existe
            }

            val userData = hashMapOf(
                "correo" to correo,
                "clave" to clave,
                "nombre" to nombre,
                "rol" to "Alumno",
                "fotoUrl" to null, // Asegurar que el campo existe desde el registro
                "fechaRegistro" to getCurrentDate()
            )

            db.collection(collectionName).add(userData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * FUNCIÓN FALTANTE: Resuelve 'Unresolved reference obtenerAlumnoPorEmail'.
     * Busca un alumno por su correo y mapea el documento completo.
     */
    suspend fun obtenerAlumnoPorEmail(correo: String): Alumno? {
        return try {
            val querySnapshot = db.collection(collectionName)
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return null
            }

            val document = querySnapshot.documents[0]
            // Mapear y asignar el ID del documento
            document.toObject(Alumno::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * FUNCIÓN FALTANTE: Resuelve 'Unresolved reference actualizarFotoPerfil'.
     * Actualiza el campo 'fotoUrl' de un alumno específico.
     */
    suspend fun actualizarFotoPerfil(alumnoId: String, fotoUrl: String): Boolean {
        return try {
            db.collection(collectionName)
                .document(alumnoId)
                .update("fotoUrl", fotoUrl)
                .await()
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