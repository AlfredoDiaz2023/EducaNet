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


                Administrador(
                    correo = correo,
                    clave = clave,
                    nombre = "Administrador"
                ) // 'rol' usará el valor por defecto de la data class

            } else {

                loginUsuarioDesdeFirestore(correo, clave)
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun loginUsuarioDesdeFirestore(correo: String, clave: String): Usuario? {
        return try {
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()

            if (query.isEmpty || query.documents.isEmpty()) {
                return null // Usuario no encontrado o clave incorrecta
            }

            // Si encontramos al usuario, leemos sus datos
            val doc = query.documents[0]
            val email = doc.getString("correo") ?: ""
            val pass = doc.getString("clave") ?: ""
            val nombre = doc.getString("nombre")
            val rol = doc.getString("rol")

            // Decidimos qué objeto de usuario crear según el rol
            when (rol) {
                "Profesor" -> Profesor(
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Profesor", // Si es null, usa "Profesor"
                    rol = rol
                )
                "Apoderado" -> Apoderado(
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Apoderado",
                    rol = rol
                )
                "Alumno" -> Alumno(
                    correo = email,
                    clave = pass,
                    nombre = nombre ?: "Alumno",
                    rol = rol
                )

                else -> null // Rol desconocido o nulo
            }
        } catch (e: Exception) {
            null // Error al consultar Firestore
        }
    }
}