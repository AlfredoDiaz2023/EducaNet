package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Usuario
import com.example.educanet.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repositorio = AuthRepository()
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _cargaLogin = MutableStateFlow(false)
    val cargaLogin: StateFlow<Boolean> = _cargaLogin

    private val _loginExitoso = MutableStateFlow(false)
    val loginExitoso: StateFlow<Boolean> = _loginExitoso

    private val _errorMensaje = MutableStateFlow("")
    val errorMensaje: StateFlow<String> = _errorMensaje

    fun login(correo: String, clave: String) {
        _cargaLogin.value = true
        _errorMensaje.value = "" // Limpiar mensaje previo
        viewModelScope.launch {
            try {
                val resultado = repositorio.login(correo, clave)
                if (resultado != null) {
                    _usuario.value = resultado
                    _loginExitoso.value = true
                } else {
                    _errorMensaje.value = "Usuario o contraseña incorrecta. Intente nuevamente."
                    _loginExitoso.value = false
                }
            } catch (e: Exception) {
                _usuario.value = null
                _loginExitoso.value = false
                _errorMensaje.value = "Error al iniciar sesión: ${e.message ?: "intente más tarde."}"
            } finally {
                _cargaLogin.value = false
            }
        }
    }
}