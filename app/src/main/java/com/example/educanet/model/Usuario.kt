package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

// 1️⃣ Primero defines la clase base sellada
sealed class Usuario(
    open val correo: String = "",
    open val clave: String = "",
    open val nombre: String = "",
    open val rol: String = ""
)

// 2️⃣ Luego defines las subclases (una vez cada una)
@IgnoreExtraProperties
data class Administrador(
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Administrador"
) : Usuario(correo, clave, nombre, rol)

@IgnoreExtraProperties
data class Alumno(
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Alumno"
) : Usuario(correo, clave, nombre, rol)

@IgnoreExtraProperties
data class Apoderado(
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Apoderado"
) : Usuario(correo, clave, nombre, rol)

@IgnoreExtraProperties
data class Profesor(
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Profesor"
) : Usuario(correo, clave, nombre, rol)