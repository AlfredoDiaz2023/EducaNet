package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties


sealed class Usuario(
    open val correo: String = "",
    open val clave: String = "",
    open val nombre: String = "",
    open val rol: String = "",
    open val fotoUrl: String = ""
)

@IgnoreExtraProperties
data class Administrador(
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Administrador",
    override val fotoUrl: String = ""
) : Usuario(correo, clave, nombre, rol, fotoUrl)

@IgnoreExtraProperties
data class Alumno(
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Alumno",
    override val fotoUrl: String = ""
) : Usuario(correo, clave, nombre, rol, fotoUrl)

@IgnoreExtraProperties
data class Apoderado(
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Apoderado",
    override val fotoUrl: String = ""
) : Usuario(correo, clave, nombre, rol, fotoUrl)

@IgnoreExtraProperties
data class Profesor(
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Profesor",
    override val fotoUrl: String = ""
) : Usuario(correo, clave, nombre, rol, fotoUrl)