package com.example.educanet.repository

import androidx.compose.ui.layout.FirstBaseline
import com.example.educanet.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(correo: String, clave: String) : Usuario? {
        return try {
            // Intentar autenticar con Authentication de Firebase - admin
            when {
                correo == "admin@educanet.cl" -> {
                    val resultado = auth.signInWithEmailAndPassword(correo, clave).await()
                    Usuario (
                        correo = correo,
                        nombre = "Administrador",
                        rol = "admin"
                    )
                }
                else -> {
                    loginProfesor(correo, clave)
                    loginApoderado(correo, clave)
                    loginAlumno(correo, clave)
                }
            }
        }catch (e: Exception) {
            null
        }
    }

    private suspend fun loginProfesor(correo: String, clave: String): Usuario? {
        return try {
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()
            if (!query.isEmpty && query.documents.isNotEmpty()) {
                val doc = query.documents[0]
                Usuario (
                    correo = doc.getString("correo") ?: "",
                    clave = doc.getString("clave") ?: "",
                    nombre = doc.getString("nombre") ?: "Profesor",
                    rol = doc.getString("rol") ?: "profesor"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun loginApoderado(correo: String, clave: String): Usuario? {
        return try {
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()
            if (!query.isEmpty && query.documents.isNotEmpty()) {
                val doc = query.documents[0]
                Usuario (
                    correo = doc.getString("correo") ?: "",
                    clave = doc.getString("clave") ?: "",
                    nombre = doc.getString("nombre") ?: "Apoderado",
                    rol = doc.getString("rol") ?: "apoderado"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun loginAlumno(correo: String, clave: String): Usuario? {
        return try {
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()
            if (!query.isEmpty && query.documents.isNotEmpty()) {
                val doc = query.documents[0]
                Usuario (
                    correo = doc.getString("correo") ?: "",
                    clave = doc.getString("clave") ?: "",
                    nombre = doc.getString("nombre") ?: "Alumno",
                    rol = doc.getString("rol") ?: "alumno"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}