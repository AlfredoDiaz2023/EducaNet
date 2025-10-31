package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.ClaseVirtual
import com.example.educanet.repository.ClaseVirtualRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClaseVirtualViewModel : ViewModel() {
    private val repository = ClaseVirtualRepository()

    private val _clases = MutableStateFlow<List<ClaseVirtual>>(emptyList())
    val clases: StateFlow<List<ClaseVirtual>> = _clases.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        obtenerClases()
    }

    fun obtenerClases() {
        viewModelScope.launch {
            _cargando.value = true
            val resultado = repository.obtenerClasesVirtuales()
            _clases.value = resultado.clases
            _cargando.value = false
        }
    }
}
