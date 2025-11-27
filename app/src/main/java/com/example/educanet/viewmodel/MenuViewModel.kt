package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.*
import com.example.educanet.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Asegúrate de que MenuUiState tenga fotoUrl
data class MenuUiState(
    val nombre: String = "",
    val rol: String = "",
    val fotoUrl: String? = null, // <--- Nuevo campo
    val isLoading: Boolean = true
)

class MenuViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    fun loadUserData(usuario: Usuario?) {
        if (usuario != null) {
            _uiState.value = mapUsuarioToMenuUiState(usuario)
        } else {
            _uiState.value = MenuUiState(isLoading = false)
        }
    }

    private fun mapUsuarioToMenuUiState(usuario: Usuario): MenuUiState {
        // 2. El 'when' ahora maneja TODOS los casos, incluido Administrador
        return when (usuario) {
            is Alumno -> MenuUiState(
                nombre = usuario.nombre,
                rol = usuario.rol,
                fotoUrl = usuario.fotoUrl,
                isLoading = false
            )
            is Profesor -> MenuUiState(
                nombre = usuario.nombre,
                rol = usuario.rol,
                fotoUrl = usuario.fotoUrl,
                isLoading = false
            )
            is Apoderado -> MenuUiState(
                nombre = usuario.nombre,
                rol = usuario.rol,
                fotoUrl = usuario.fotoUrl,
                isLoading = false
            )
            is Administrador -> MenuUiState(
                nombre = usuario.nombre,
                rol = usuario.rol,
                fotoUrl = usuario.fotoUrl, // Será null, pero ya no da error
                isLoading = false
            )
            else -> MenuUiState(isLoading = false) // Fallback por seguridad
        }
    }
}