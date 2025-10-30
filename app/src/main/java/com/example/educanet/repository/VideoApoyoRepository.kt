package com.example.educanet.repository

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

    suspend fun obtenerVideosDeApoyo(limite: Int = 10): ResultadoVideosApoyo {
        return try {
            val query = db.collection("video_apoyo")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val videos = querySnapshot.toObjects(VideoApoyo::class.java)

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
            val videos = querySnapshot.toObjects(VideoApoyo::class.java)

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
            db.collection("video_apoyo")
                .document(videoId)
                .get()
                .await()
                .toObject(VideoApoyo::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
