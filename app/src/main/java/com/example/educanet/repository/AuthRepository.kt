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

                // Se añade fotoUrl como cadena vacía para el admin hardcodeado
                Administrador(
                    correo = correo,
                    clave = clave,
                    nombre = "Administrador",
                    rol = "Administrador",
                    fotoUrl = "" // CORREGIDO: No puede ser null
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
            // PRIMERO: Verificar si el usuario existe en Firestore
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()

            if (query.isEmpty || query.documents.isEmpty()) {
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
            
            // AHORA: Autenticar con Firebase Auth DESPUÉS de verificar Firestore
            try {
                auth.signInWithEmailAndPassword(correo, clave).await()
                android.util.Log.d("AuthRepository", "Login Firebase Auth exitoso para: $correo")
            } catch (authError: Exception) {
                android.util.Log.w("AuthRepository", "SignIn falló, intentando crear usuario: ${authError.message}")
                // Si falla la autenticación, intentar crear el usuario en Auth
                try {
                    auth.createUserWithEmailAndPassword(correo, clave).await()
                    android.util.Log.d("AuthRepository", "Usuario creado en Firebase Auth: $correo")
                } catch (createError: Exception) {
                    android.util.Log.e("AuthRepository", "No se pudo crear usuario en Auth: ${createError.message}")
                    // Continuar aunque falle Auth - el usuario existe en Firestore
                }
            }
            
            // Guardar el UID de Firebase Auth en el documento si no existe
            val uid = auth.currentUser?.uid
            if (uid != null && doc.getString("uid") == null) {
                db.collection("usuario").document(id).update("uid", uid)
            }

            when (rol) {
                "Profesor" -> Profesor(
                    id = id,
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Profesor",
                    rol = rol ?: "Profesor",
                    fotoUrl = fotoUrl ?: "" // CORREGIDO: Si es null, usa ""
                )
                "Apoderado" -> Apoderado(
                    id = id,
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Apoderado",
                    rol = rol ?: "Apoderado",
                    fotoUrl = fotoUrl ?: "" // CORREGIDO
                )
                "Alumno" -> Alumno(
                    id = id,
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Alumno",
                    rol = rol ?: "Alumno",
                    fotoUrl = fotoUrl ?: "" // CORREGIDO
                )
                "Administrador" -> Administrador(
                    id = id,
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Admin",
                    rol = rol ?: "Administrador",
                    fotoUrl = fotoUrl ?: "" // CORREGIDO
                )
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}