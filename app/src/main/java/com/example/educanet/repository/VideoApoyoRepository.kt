package com.example.educanet.repository

import com.example.educanet.model.ProfesorSimple
import com.example.educanet.model.VideoApoyo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ResultadoVideosApoyo(
    val videos: List<VideoApoyo>,
    val ultimoDocumento: Any?
)

class VideoApoyoRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun agregarVideo(video: VideoApoyo): Boolean {
        return try {
            // Crear estructura plana para Firestore
            val videoData = hashMapOf(
                "nombre" to video.nombre,
                "descripcion" to video.descripcion,
                "duracion" to video.duracion,
                "nivel" to video.nivel,
                "video" to video.video,
                "profesor" to hashMapOf(
                    "correo" to video.profesor.correo,
                    "nombre" to video.profesor.nombre,
                    "rol" to video.profesor.rol
                )
            )
            db.collection("video_apoyo").add(videoData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Función auxiliar para mapear documento a VideoApoyo de forma segura
    private fun documentToVideoApoyo(doc: com.google.firebase.firestore.DocumentSnapshot): VideoApoyo? {
        return try {
            val data = doc.data ?: return null
            
            // Extraer profesor de forma segura
            val profesorData = data["profesor"] as? Map<*, *>
            val profesorSimple = if (profesorData != null) {
                ProfesorSimple(
                    correo = profesorData["correo"]?.toString() ?: "",
                    nombre = profesorData["nombre"]?.toString() ?: "",
                    rol = profesorData["rol"]?.toString() ?: ""
                )
            } else {
                ProfesorSimple()
            }
            
            VideoApoyo(
                id = doc.id,
                nombre = data["nombre"]?.toString() ?: "",
                nivel = data["nivel"]?.toString() ?: "",
                video = data["video"]?.toString() ?: "",
                descripcion = data["descripcion"]?.toString() ?: "",
                duracion = (data["duracion"] as? Number)?.toInt() ?: 0,
                profesor = profesorSimple
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun obtenerVideosDeApoyo(limite: Int = 10): ResultadoVideosApoyo {
        return try {
            val query = db.collection("video_apoyo")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val videos = querySnapshot.documents.mapNotNull { doc ->
                documentToVideoApoyo(doc)
            }

            val ultimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoVideosApoyo(videos, ultimoDocumento)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoVideosApoyo(emptyList(), null)
        }
    }

    suspend fun obtenerMasVideosDeApoyo(limite: Int = 10, ultimoDocumento: Any?): ResultadoVideosApoyo {
        return try {
            if (ultimoDocumento == null) return ResultadoVideosApoyo(emptyList(), null)

            val query = db.collection("video_apoyo")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .startAfter(ultimoDocumento)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val videos = querySnapshot.documents.mapNotNull { doc ->
                documentToVideoApoyo(doc)
            }

            val nuevoUltimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoVideosApoyo(videos, nuevoUltimoDocumento)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoVideosApoyo(emptyList(), null)
        }
    }

    suspend fun obtenerVideoDeApoyoPorId(videoId: String): VideoApoyo? {
        return try {
            val doc = db.collection("video_apoyo")
                .document(videoId)
                .get()
                .await()
            documentToVideoApoyo(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
