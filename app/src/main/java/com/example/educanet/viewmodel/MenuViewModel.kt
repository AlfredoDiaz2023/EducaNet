package com.example.educanet.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.*
import com.example.educanet.repository.NotificacionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class MenuUiState(
    val usuario: Usuario? = null,
    val hasUnreadNotifications: Boolean = false,
    val isLoading: Boolean = true,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null
)

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val notificacionRepository = NotificacionRepository()

    private val TAG = "EDU_DEBUG"

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    // Listener para detectar cambios en la sesión en tiempo real
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser != null) {
            Log.d(TAG, "AuthStateListener: Usuario detectado (${firebaseAuth.currentUser?.email}). Cargando datos...")
            loadUserData(firebaseAuth.currentUser?.email)
        } else {
            Log.e(TAG, "AuthStateListener: Usuario cerró sesión o no autenticado.")
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Sesión no iniciada")
        }
    }

    init {
        // Registramos el listener al iniciar el ViewModel
        auth.addAuthStateListener(authListener)
        checkForUnreadNotifications()
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }

    fun loadUserData(email: String?) {
        if (email == null) return

        viewModelScope.launch {
            try {
                // Buscamos en "usuario" por correo
                val querySnapshot = db.collection("usuario")
                    .whereEqualTo("correo", email)
                    .limit(1)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val userDoc = querySnapshot.documents[0]
                    val rol = userDoc.getString("rol")
                    val usuario: Usuario? = when (rol) {
                        "Alumno" -> userDoc.toObject(Alumno::class.java)
                        "Profesor" -> userDoc.toObject(Profesor::class.java)
                        "Apoderado" -> userDoc.toObject(Apoderado::class.java)
                        "Administrador" -> userDoc.toObject(Administrador::class.java)
                        else -> null
                    }
                    _uiState.value = _uiState.value.copy(usuario = usuario, isLoading = false)
                } else {
                    Log.w(TAG, "No se encontró en colección 'usuario'.")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Datos de usuario no encontrados.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun checkForUnreadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(hasUnreadNotifications = notificacionRepository.hayNotificacionesSinLeer())
        }
    }

    fun updateProfilePicture(imageUriString: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingPhoto = true, error = null)

            // INTENTO DE RECUPERACIÓN DE SESIÓN
            if (auth.currentUser == null) {
                Log.w(TAG, "Usuario null, intentando recargar sesión...")
                try {
                    kotlinx.coroutines.delay(500)
                } catch (e: Exception) { }
            }

            val user = auth.currentUser
            if (user == null) {
                Log.e(TAG, "ERROR FATAL: Usuario sigue siendo null tras espera.")
                _uiState.value = _uiState.value.copy(
                    isUploadingPhoto = false,
                    error = "Sesión perdida. Por favor cierra sesión y vuelve a entrar."
                )
                return@launch
            }

            try {
                val base64Image = withContext(Dispatchers.IO) {
                    uriToBase64(Uri.parse(imageUriString))
                }

                if (base64Image == null) {
                    _uiState.value = _uiState.value.copy(isUploadingPhoto = false, error = "Error al leer imagen")
                    return@launch
                }

                val querySnapshot = db.collection("usuario")
                    .whereEqualTo("correo", user.email)
                    .limit(1)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val docId = querySnapshot.documents[0].id
                    val dataToUpdate = hashMapOf("fotoUrl" to base64Image)

                    db.collection("usuario").document(docId)
                        .set(dataToUpdate, SetOptions.merge())
                        .await()

                    // Recargamos los datos para asegurar que la UI tenga la última versión
                    loadUserData(user.email)
                } else {
                    _uiState.value = _uiState.value.copy(error = "No se encontró tu perfil en la base de datos.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Excepción: ${e.message}")
                _uiState.value = _uiState.value.copy(error = "Error: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isUploadingPhoto = false)
            }
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val contentResolver = getApplication<Application>().contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return null

            // 400x400 y calidad 50 para asegurar < 1MB string
            val scaledBitmap = getResizedBitmap(bitmap, 400)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }
}