package com.example.educanet.viewmodel


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
            val uid = currentUser?.uid
            val role = fetchUserRole()
            _userRole.value = role


            if (uid != null && role != null) {
                listenForProgresos(uid, role)
            } else {
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
            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            userDoc.getString("nombre")
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
            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            userDoc.getString("rol")
        } catch (e: Exception) {
            null
        }
    }


    private fun listenForProgresos(uid: String, userRole: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val resultado = progresoAcademicoRepository.obtenerNotas(uid, userRole)
                _progresos.value = resultado.progresoAcademico
            } catch (e: Exception) {
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

