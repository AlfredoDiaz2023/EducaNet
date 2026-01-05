package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Usuario
import com.example.educanet.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegistroViewModel : ViewModel() {
    private val repositorio = UsuarioRepository()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso

    private val _errorMensaje = MutableStateFlow("")
    val errorMensaje: StateFlow<String> = _errorMensaje

    // Nuevo: almacena el usuario registrado para navegación automática
    private val _usuarioRegistrado = MutableStateFlow<Usuario?>(null)
    val usuarioRegistrado: StateFlow<Usuario?> = _usuarioRegistrado

    fun registroUsuario(correo: String, clave: String, confirmarClave: String, nombre: String, rol: String, curso: String = "") {
        // Limpiar error previo
        _errorMensaje.value = ""
        
        if (correo.isEmpty() || clave.isEmpty() || confirmarClave.isEmpty() || nombre.isEmpty() || rol.isEmpty()) {
            _errorMensaje.value = "Todos los campos son obligatorios"
            return
        }

        // Validar que el alumno tenga curso seleccionado
        if (rol == "Alumno" && curso.isEmpty()) {
            _errorMensaje.value = "Debe seleccionar un curso para el alumno"
            return
        }

        if (!correo.contains("@") || !correo.contains(".")) {
            _errorMensaje.value = "Ingrese un correo electrónico válido"
            return
        }

        if (clave.length < 6) {
            _errorMensaje.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        if (clave != confirmarClave) {
            _errorMensaje.value = "Las contraseñas no coinciden"
            return
        }

        _cargando.value = true

        viewModelScope.launch {
            val resultado = repositorio.registroUsuario(correo, clave, nombre, rol, curso)
            _cargando.value = false
            
            if (resultado.success) {
                _usuarioRegistrado.value = resultado.usuario
                _registroExitoso.value = true
            } else {
                _errorMensaje.value = resultado.errorMessage
            }
        }
    }

    fun resetEstado() {
        _registroExitoso.value = false
        _errorMensaje.value = ""
        _usuarioRegistrado.value = null
    }
}