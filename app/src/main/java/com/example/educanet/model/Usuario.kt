package com.example.educanet.model

// Quitamos 'data' y agregamos 'open' para permitir herencia
open class Usuario(
    open val id: String = "",
    open val nombre: String = "",
    open val correo: String = "", // Cambiado de email a correo para coincidir con subclases
    open val clave: String = "",  // Agregado clave
    open val rol: String = "",
    open val fotoUrl: String = "" // Cambiado de fotoPerfil a fotoUrl
) {
    // Implementamos copy manualmente para que el ViewModel funcione
    fun copy(
        id: String = this.id,
        nombre: String = this.nombre,
        correo: String = this.correo,
        clave: String = this.clave,
        rol: String = this.rol,
        fotoUrl: String = this.fotoUrl
    ): Usuario {
        return Usuario(id, nombre, correo, clave, rol, fotoUrl)
    }
}