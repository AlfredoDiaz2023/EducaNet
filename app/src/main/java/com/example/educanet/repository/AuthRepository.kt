package com.example.educanet.repository

import androidx.compose.ui.layout.FirstBaseline
import com.example.educanet.model.Administrador
import com.example.educanet.model.Profesor
import com.example.educanet.model.Apoderado
import com.example.educanet.model.Alumno
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(correo: String, clave: String): Any? {
        return try {
            if (correo == "admin@educanet.cl") {
                auth.signInWithEmailAndPassword(correo, clave).await()
                Administrador(correo, "Administrador", "Administrador")
            } else {
                loginProfesor(correo, clave)
                    ?: loginApoderado(correo, clave)
                    ?: loginAlumno(correo, clave)
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun loginProfesor(correo: String, clave: String): Profesor? {
        return try {
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()
            if (!query.isEmpty && query.documents.isNotEmpty()) {
                val doc = query.documents[0]
                Profesor (
                    correo = doc.getString("correo") ?: "",
                    clave = doc.getString("clave") ?: "",
                    nombre = doc.getString("nombre") ?: "Profesor",
                    rol = doc.getString("rol") ?: "Profesor"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun loginApoderado(correo: String, clave: String): Apoderado? {
        return try {
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()
            if (!query.isEmpty && query.documents.isNotEmpty()) {
                val doc = query.documents[0]
                Apoderado (
                    correo = doc.getString("correo") ?: "",
                    clave = doc.getString("clave") ?: "",
                    nombre = doc.getString("nombre") ?: "Apoderado",
                    rol = doc.getString("rol") ?: "Apoderado"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun loginAlumno(correo: String, clave: String): Alumno? {
        return try {
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()
            if (!query.isEmpty && query.documents.isNotEmpty()) {
                val doc = query.documents[0]
                Alumno (
                    correo = doc.getString("correo") ?: "",
                    clave = doc.getString("clave") ?: "",
                    nombre = doc.getString("nombre") ?: "Alumno",
                    rol = doc.getString("rol") ?: "Alumno"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}