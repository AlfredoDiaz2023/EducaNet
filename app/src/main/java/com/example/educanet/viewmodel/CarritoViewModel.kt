package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.ItemCarrito
import com.example.educanet.model.Libro
import com.example.educanet.repository.LibroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CarritoViewModel : ViewModel() {
    private val repository = LibroRepository()

    private val _libros = MutableStateFlow<List<Libro>>(emptyList())
    val libros: StateFlow<List<Libro>> = _libros

    private val _carrito = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val carrito: StateFlow<List<ItemCarrito>> = _carrito

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private var ultimoDocumento: Any? = null
    private val limiteLibros = 10

    init {
        cargarLibros()
    }

    fun cargarLibros() {
        _cargando.value = true
        viewModelScope.launch {
            try {
                val resultado = repository.obtenerLibros(limite = limiteLibros)
                _libros.value = resultado.libros
                ultimoDocumento = resultado.ultimoDocumento
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _cargando.value = false
            }
        }
    }

    fun cargarMasLibros() {
        if (_cargando.value) return

        _cargando.value = true
        viewModelScope.launch {
            try {
                val resultado = repository.obtenerMasLibros(
                    limite = limiteLibros,
                    ultimoDocumento = ultimoDocumento
                )
                if (resultado.libros.isNotEmpty()) {
                    _libros.value = _libros.value + resultado.libros
                    ultimoDocumento = resultado.ultimoDocumento
                }
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _cargando.value = false
            }
        }
    }

    fun agregarAlCarrito(libro: Libro) {
        viewModelScope.launch {
            try {
                // Verificar cantidad actualizado
                val libroActualizado = repository.obtenerLibroPorId(libro.id)
                if (libroActualizado == null || libroActualizado.cantidad <= 0) return@launch

                val carritoActual = _carrito.value.toMutableList()
                val itemExistente = carritoActual.find { it.libro.id == libro.id }

                // Verificar cantidad disponible
                val cantidadDisponible = libroActualizado.cantidad - (itemExistente?.cantidad ?: 0)
                if (cantidadDisponible <= 0) return@launch

                if (itemExistente != null) {
                    itemExistente.cantidad++
                    // Actualizar cantidad en Firestore
                    repository.actualizarStock(libro.id, libroActualizado.cantidad - 1)
                } else {
                    carritoActual.add(ItemCarrito(libro = libroActualizado, cantidad = 1))
                    // Actualizar cantidad en Firestore
                    repository.actualizarStock(libro.id, libroActualizado.cantidad - 1)
                }

                _carrito.value = carritoActual
                actualizarLibrosEnCatalogo()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun removerDelCarrito(libro: Libro) {
        viewModelScope.launch {
            try {
                val carritoActual = _carrito.value.toMutableList()
                val itemExistente = carritoActual.find { it.libro.id == libro.id }

                if (itemExistente != null) {
                    if (itemExistente.cantidad > 1) {
                        itemExistente.cantidad--
                        // Aumentar cantidad en Firestore
                        val libroActualizado = repository.obtenerLibroPorId(libro.id)
                        if (libroActualizado != null) {
                            repository.actualizarStock(libro.id, libroActualizado.cantidad + 1)
                        }
                    } else {
                        carritoActual.remove(itemExistente)
                        // Aumentar cantidad en Firestore
                        val libroActualizado = repository.obtenerLibroPorId(libro.id)
                        if (libroActualizado != null) {
                            repository.actualizarStock(libro.id, libroActualizado.cantidad + 1)
                        }
                    }
                }

                _carrito.value = carritoActual
                actualizarLibrosEnCatalogo()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun eliminarLibroDelCarrito(libro: Libro) {
        viewModelScope.launch {
            try {
                val carritoActual = _carrito.value.toMutableList()
                val itemExistente = carritoActual.find { it.libro.id == libro.id }

                if (itemExistente != null) {
                    carritoActual.remove(itemExistente)
                    // Restaurar todo el stock en Firestore
                    val libroActualizado = repository.obtenerLibroPorId(libro.id)
                    if (libroActualizado != null) {
                        repository.actualizarStock(
                            libro.id,
                            libroActualizado.cantidad + itemExistente.cantidad
                        )
                    }
                }

                _carrito.value = carritoActual
                actualizarLibrosEnCatalogo()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun vaciarCarrito() {
        viewModelScope.launch {
            try {
                // Restaurar stock de todos los libros en el carrito
                _carrito.value.forEach { item ->
                    val libroActualizado = repository.obtenerLibroPorId(item.libro.id)
                    if (libroActualizado != null) {
                        repository.actualizarStock(
                            item.libro.id,
                            libroActualizado.cantidad + item.cantidad
                        )
                    }
                }

                _carrito.value = emptyList()
                actualizarLibrosEnCatalogo()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun confirmarCompra() {
        viewModelScope.launch {
            try {
                // Aquí puedes implementar la lógica de confirmación de compra
                // Por ahora solo limpiamos el carrito
                _carrito.value = emptyList()
                // Recargar libros para actualizar stocks
                cargarLibros()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    private suspend fun actualizarLibrosEnCatalogo() {
        // Recargar libros para reflejar cambios de stock
        val resultado = repository.obtenerLibros(limite = _libros.value.size + limiteLibros)
        _libros.value = resultado.libros
        ultimoDocumento = resultado.ultimoDocumento
    }


}