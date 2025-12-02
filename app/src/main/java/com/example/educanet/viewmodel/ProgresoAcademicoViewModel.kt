package com.example.educanet.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.ProgresoAcademico
import com.example.educanet.repository.ProgresoAcademicoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ProgresoAcademicoViewModel : ViewModel() {


    private val progresoAcademicoRepository = ProgresoAcademicoRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    private val _progresos = MutableStateFlow<List<ProgresoAcademico>>(emptyList())
    val progresos: StateFlow<List<ProgresoAcademico>> = _progresos.asStateFlow()


    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()


    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()


    init {
        loadData()
    }


    private fun loadData() {
        viewModelScope.launch {
            _cargando.value = true


            val currentUser = auth.currentUser
            val email = currentUser?.email
            Log.d("ProgresoVM", "Usuario actual - Email: $email")
            
            val role = fetchUserRole()
            _userRole.value = role
            Log.d("ProgresoVM", "Rol del usuario: $role")


            if (email != null && role != null) {
                // Usar el correo del usuario para buscar sus notas
                listenForProgresos(email, role)
            } else {
                Log.d("ProgresoVM", "No se pudo cargar - email o rol es null")
                _cargando.value = false
            }
        }
    }


    // Nuevo método para cargar notas por correo del alumno
    fun cargarNotasPorCorreo(correo: String, rol: String) {
        viewModelScope.launch {
            _cargando.value = true
            Log.d("ProgresoVM", "cargarNotasPorCorreo - Correo: $correo, Rol: $rol")
            try {
                val resultado = progresoAcademicoRepository.obtenerNotas(correo, rol)
                _progresos.value = resultado.progresoAcademico
                Log.d("ProgresoVM", "Notas cargadas: ${resultado.progresoAcademico.size}")
            } catch (e: Exception) {
                Log.e("ProgresoVM", "Error: ${e.message}")
                e.printStackTrace()
                _progresos.value = emptyList()
            } finally {
                _cargando.value = false
            }
        }
    }


    private suspend fun fetchUserName(): String? {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return null
        }


        return try {
            // Buscar en la colección "usuario" por correo
            val querySnapshot = db.collection("usuario")
                .whereEqualTo("correo", currentUser.email)
                .get()
                .await()
            querySnapshot.documents.firstOrNull()?.getString("nombre")
        } catch (e: Exception) {
            null
        }
    }


    private suspend fun fetchUserRole(): String? {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return null
        }


        return try {
            // Buscar en la colección "usuario" por correo
            val querySnapshot = db.collection("usuario")
                .whereEqualTo("correo", currentUser.email)
                .get()
                .await()
            val rol = querySnapshot.documents.firstOrNull()?.getString("rol")
            Log.d("ProgresoVM", "fetchUserRole - Email: ${currentUser.email}, Rol encontrado: $rol")
            rol
        } catch (e: Exception) {
            Log.e("ProgresoVM", "Error fetchUserRole: ${e.message}")
            null
        }
    }


    private fun listenForProgresos(userEmail: String, userRole: String) {
        viewModelScope.launch {
            _cargando.value = true
            Log.d("ProgresoVM", "listenForProgresos - Email: $userEmail, Rol: $userRole")
            try {
                // Pasar el correo para filtrar las notas del alumno
                val resultado = progresoAcademicoRepository.obtenerNotas(userEmail, userRole)
                _progresos.value = resultado.progresoAcademico
                Log.d("ProgresoVM", "Progresos cargados: ${resultado.progresoAcademico.size}")
            } catch (e: Exception) {
                Log.e("ProgresoVM", "Error listenForProgresos: ${e.message}")
                e.printStackTrace()
            } finally {
                _cargando.value = false
            }
        }
    }


    fun refreshProgresos() {
        loadData()
    }
}

