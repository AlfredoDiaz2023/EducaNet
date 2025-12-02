package com.example.educanet.repository

import android.util.Log
import com.example.educanet.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PerfilRepository(
    private val firestore: FirebaseFirestore
) {

    // Cambiar a la colección correcta: "usuario"
    private val usuariosCollection = firestore.collection("usuario")

    suspend fun obtenerPerfil(authUid: String): Usuario? {
        return try {
            Log.d("EDUCA_DEBUG", "PerfilRepository: Buscando usuario con uid=$authUid")
            
            // Buscamos por el campo "uid" que guardaste al registrarte
            val snapshot = usuariosCollection
                .whereEqualTo("uid", authUid)
                .get()
                .await()

            Log.d("EDUCA_DEBUG", "PerfilRepository: Documentos encontrados: ${snapshot.size()}")
            
            if (snapshot.isEmpty) {
                Log.w("EDUCA_DEBUG", "PerfilRepository: No se encontró documento con uid=$authUid")
                return null
            }

            val doc = snapshot.documents[0]
            val idDoc = doc.id // ID del documento (ej: AQna...)
            
            // Mapeo manual para evitar errores de nulos
            val nombre = doc.getString("nombre") ?: ""
            val correo = doc.getString("correo") ?: ""
            val clave = doc.getString("clave") ?: ""
            val rol = doc.getString("rol") ?: ""
            val fotoUrl = doc.getString("fotoUrl") ?: ""
            
            Log.d("EDUCA_DEBUG", "PerfilRepository: Usuario encontrado - DocId=$idDoc, Nombre=$nombre, Correo=$correo, Rol=$rol")

            // Devolvemos el objeto correcto según el rol, pero con el ID del documento
            when (rol) {
                "Alumno" -> Alumno(id = idDoc, nombre = nombre, correo = correo, clave = clave, rol = rol, fotoUrl = fotoUrl)
                "Profesor" -> Profesor(id = idDoc, nombre = nombre, correo = correo, clave = clave, rol = rol, fotoUrl = fotoUrl)
                "Apoderado" -> Apoderado(id = idDoc, nombre = nombre, correo = correo, clave = clave, rol = rol, fotoUrl = fotoUrl)
                "Administrador" -> Administrador(id = idDoc, nombre = nombre, correo = correo, clave = clave, rol = rol, fotoUrl = fotoUrl)
                else -> Usuario(id = idDoc, nombre = nombre, correo = correo, clave = clave, rol = rol, fotoUrl = fotoUrl)
            }
        } catch (e: Exception) {
            Log.e("EDUCA_DEBUG", "PerfilRepository: Error buscando perfil: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun actualizarFotoPerfil(documentId: String, url: String) {
        // Actualizamos el campo fotoUrl en el documento específico
        usuariosCollection.document(documentId).update("fotoUrl", url).await()
    }
}