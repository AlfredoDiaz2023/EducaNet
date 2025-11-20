package com.example.educanet.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Libro
import com.example.educanet.repository.LibroRepository
import com.example.educanet.repository.NotificacionRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AddLibroUiState(
    val nombre: String = "",
    val nivel: String = "",
    val cantidad: Int = 0,
    val imagenUri: Uri? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddLibroViewModel : ViewModel() {

    private val libroRepository = LibroRepository()
    private val notificacionRepository = NotificacionRepository()

    private val _uiState = MutableStateFlow(AddLibroUiState())
    val uiState: StateFlow<AddLibroUiState> = _uiState.asStateFlow()

    fun onNombreChange(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre)
    }

    fun onNivelChange(nivel: String) {
        _uiState.value = _uiState.value.copy(nivel = nivel)
    }

    fun onCantidadChange(cantidad: String) {
        _uiState.value = _uiState.value.copy(cantidad = cantidad.toIntOrNull() ?: 0)
    }

    fun onImagenChange(uri: Uri?) {
        android.util.Log.d("DEBUG_URI", "Imagen seleccionada: $uri")
        _uiState.value = _uiState.value.copy(imagenUri = uri)
    }

    // Copia el content Uri a un archivo real dentro de context.cacheDir
    private fun copiarUriAFileTemporal(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver

        // Intenta abrir input stream
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw Exception("InputStream es null - no se pudo abrir el URI")

        // Crear temp file dentro del cacheDir de la app
        val tempFile = File.createTempFile("libro_", ".jpg", context.cacheDir)

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private suspend fun subirImagen(context: Context, uri: Uri): String {
        // primero copiamos y logueamos
        val archivo = try {
            copiarUriAFileTemporal(context, uri)
        } catch (e: Exception) {
            Log.e("DEBUG_FILE", "Error al copiar URI a archivo temporal: ${e.message}")
            throw e
        }

        Log.d("DEBUG_FILE", "Archivo temporal generado: ${archivo.absolutePath} existe? ${archivo.exists()}")

        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("libros/${System.currentTimeMillis()}.jpg")

        return suspendCancellableCoroutine { continuation ->
            fileRef.putFile(Uri.fromFile(archivo))
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { url ->
                        Log.d("DEBUG_UPLOAD", "Upload ok, URL: $url")
                        continuation.resume(url.toString())
                    }.addOnFailureListener { e ->
                        Log.e("DEBUG_UPLOAD", "getDownloadUrl failed: ${e.message}")
                        continuation.resumeWithException(e)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DEBUG_UPLOAD", "putFile failed: ${e.message}")
                    continuation.resumeWithException(e)
                }
        }
    }

    // Public function que recibe context para poder leer el URI
    fun saveLibro(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                val uri = _uiState.value.imagenUri
                    ?: throw Exception("Debe seleccionar una imagen")

                val url = subirImagen(context, uri)

                val libro = Libro(
                    nombre = _uiState.value.nombre,
                    nivel = _uiState.value.nivel,
                    cantidad = _uiState.value.cantidad,
                    imagen = url
                )

                val success = libroRepository.agregarLibro(libro)

                if (success) {
                    notificacionRepository.agregarNotificacion(
                        titulo = "Nuevo libro agregado",
                        mensaje = "Se agregó: ${_uiState.value.nombre}"
                    )
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Error al guardar el libro"
                    )
                }
            } catch (e: Exception) {
                Log.e("DEBUG_SAVE", "saveLibro error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message
                )
            }
        }
    }
}
