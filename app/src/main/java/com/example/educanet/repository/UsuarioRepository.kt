package com.example.educanet.repository

import com.example.educanet.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class RegistroResult(
    val success: Boolean,
    val errorMessage: String = "",
    val usuario: Usuario? = null
)

class UsuarioRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun registroUsuario(correo: String, clave: String, nombre: String, rol: String, curso: String = ""): RegistroResult {
        return try {
            // 1. Crear usuario en Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(correo, clave).await()
            val firebaseUser = authResult.user

            if (firebaseUser == null) {
                return RegistroResult(false, "Error al crear usuario")
            }

            // 2. Preparar datos para Firestore
            val rolFinal = when (rol.trim().lowercase()) {
                "profesor" -> "Profesor"
                "apoderado" -> "Apoderado"
                "administrador" -> "Administrador"
                else -> "Alumno"
            }

            val userData = hashMapOf(
                "uid" to firebaseUser.uid,
                "correo" to correo,
                "clave" to clave,
                "nombre" to nombre,
                "rol" to rolFinal,
                "fotoUrl" to "",
                "fechaRegistro" to getCurrentDate(),
                "curso" to if (rolFinal == "Alumno") curso else "" // Solo alumnos tienen curso
            )

            // 3. Guardar en Firestore en la colección "usuario"
            val docRef = db.collection("usuario").add(userData).await()

            // 4. Crear el objeto Usuario según el rol para devolverlo
            val usuario: Usuario = when (rolFinal) {
                "Profesor" -> Profesor(
                    id = docRef.id,
                    nombre = nombre,
                    correo = correo,
                    clave = clave,
                    rol = rolFinal,
                    fotoUrl = "",
                    fechaRegistro = getCurrentDate()
                )
                "Apoderado" -> Apoderado(
                    id = docRef.id,
                    nombre = nombre,
                    correo = correo,
                    clave = clave,
                    rol = rolFinal,
                    fotoUrl = "",
                    fechaRegistro = getCurrentDate()
                )
                "Administrador" -> Administrador(
                    id = docRef.id,
                    nombre = nombre,
                    correo = correo,
                    clave = clave,
                    rol = rolFinal,
                    fotoUrl = "",
                    fechaRegistro = getCurrentDate()
                )
                else -> Alumno(
                    id = docRef.id,
                    nombre = nombre,
                    correo = correo,
                    clave = clave,
                    rol = rolFinal,
                    fotoUrl = "",
                    fechaRegistro = getCurrentDate(),
                    curso = curso
                )
            }

            // 5. Cerrar sesión después del registro para que el usuario tenga que iniciar sesión
            auth.signOut()

            RegistroResult(true, usuario = usuario)
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            RegistroResult(false, "Este correo ya está registrado")
        } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
            RegistroResult(false, "La contraseña es muy débil")
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            RegistroResult(false, "El correo electrónico no es válido")
        } catch (e: Exception) {
            e.printStackTrace()
            RegistroResult(false, "Error al registrar: ${e.message}")
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}