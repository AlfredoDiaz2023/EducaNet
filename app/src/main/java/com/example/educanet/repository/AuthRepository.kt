package com.example.educanet.repository

import com.example.educanet.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(correo: String, clave: String): Usuario? {
        return try {
            if (correo == "admin@educanet.cl") {
                auth.signInWithEmailAndPassword(correo, clave).await()

                // Se añade fotoUrl como null para el admin hardcodeado
                Administrador(
                    correo = correo,
                    clave = clave,
                    nombre = "Administrador",
                    rol = "Administrador",
                    fotoUrl = null
                )
            } else {
                loginUsuarioDesdeFirestore(correo, clave)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun loginUsuarioDesdeFirestore(correo: String, clave: String): Usuario? {
        return try {
            // Nota: Asegúrate de que tu colección se llama "usuario" o la que corresponda (ej. "Alumno", "Profesor")
            // Si usas una colección única para todos los usuarios:
            val query = db.collection("usuario") // <--- CAMBIA ESTO si usas colecciones separadas por rol
                .whereEqualTo("correo", correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()

            if (query.isEmpty || query.documents.isEmpty()) {
                // Si no está en "usuario", podrías intentar buscar en "Alumno", "Profesor", etc. si están separadas
                // Por ahora asumo la lógica que tenías.
                return null
            }

            val doc = query.documents[0]
            val email = doc.getString("correo") ?: ""
            val pass = doc.getString("clave") ?: ""
            val nombre = doc.getString("nombre")
            val rol = doc.getString("rol")
            val id = doc.id
            // 1. LEER LA FOTO URL
            val fotoUrl = doc.getString("fotoUrl")

            when (rol) {
                "Profesor" -> Profesor(
                    id = id,
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Profesor",
                    rol = rol,
                    fotoUrl = fotoUrl // <--- PASAR FOTO
                )
                "Apoderado" -> Apoderado(
                    id = id,
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Apoderado",
                    rol = rol,
                    fotoUrl = fotoUrl // <--- PASAR FOTO
                )
                "Alumno" -> Alumno(
                    id = id,
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Alumno",
                    rol = rol,
                    fotoUrl = fotoUrl // <--- PASAR FOTO
                )
                // Caso Admin si existiera en BD
                "Administrador" -> Administrador(
                    id = id,
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Admin",
                    rol = rol,
                    fotoUrl = fotoUrl
                )
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}