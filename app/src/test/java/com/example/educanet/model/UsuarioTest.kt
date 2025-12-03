package com.example.educanet.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class UsuarioTest : BehaviorSpec({

    given("un Usuario base") {

        `when`("se crea con valores por defecto") {
            val usuario = Usuario()

            then("los campos deben estar vacíos") {
                usuario.id shouldBe ""
                usuario.nombre shouldBe ""
                usuario.correo shouldBe ""
                usuario.clave shouldBe ""
                usuario.rol shouldBe ""
                usuario.fotoUrl shouldBe ""
            }
        }

        `when`("se crea con valores específicos") {
            val usuario = Usuario(
                id = "usr001",
                nombre = "Juan Pérez",
                correo = "juan@educanet.com",
                clave = "password123",
                rol = "Alumno",
                fotoUrl = "https://ejemplo.com/foto.jpg"
            )

            then("los valores deben coincidir con los entregados") {
                usuario.id shouldBe "usr001"
                usuario.nombre shouldBe "Juan Pérez"
                usuario.correo shouldBe "juan@educanet.com"
                usuario.clave shouldBe "password123"
                usuario.rol shouldBe "Alumno"
                usuario.fotoUrl shouldBe "https://ejemplo.com/foto.jpg"
            }
        }

        `when`("se usa el método copy para modificar el nombre") {
            val usuarioOriginal = Usuario(
                id = "usr001",
                nombre = "Juan Pérez",
                correo = "juan@educanet.com",
                clave = "password123",
                rol = "Alumno",
                fotoUrl = ""
            )
            val usuarioModificado = usuarioOriginal.copy(nombre = "Juan Pablo Pérez")

            then("el usuario copiado debe tener el nuevo nombre") {
                usuarioModificado.nombre shouldBe "Juan Pablo Pérez"
                usuarioModificado.correo shouldBe usuarioOriginal.correo
            }
        }

        `when`("se verifica si es administrador") {
            val admin = Usuario(id = "adm001", nombre = "Admin", correo = "", clave = "", rol = "Admin", fotoUrl = "")
            val alumno = Usuario(id = "alm001", nombre = "Alumno", correo = "", clave = "", rol = "Alumno", fotoUrl = "")

            then("solo el admin debe tener rol Admin") {
                (admin.rol == "Admin") shouldBe true
                (alumno.rol == "Admin") shouldBe false
            }
        }
    }

    given("un Alumno que hereda de Usuario") {

        `when`("se crea un Alumno") {
            val alumno = Alumno(
                id = "alum001",
                nombre = "María González",
                correo = "maria@educanet.com",
                clave = "clave123",
                fotoUrl = "",
                fechaRegistro = "2025-01-15"
            )

            then("debe tener el rol 'Alumno' por defecto") {
                alumno.rol shouldBe "Alumno"
            }

            then("debe heredar las propiedades de Usuario") {
                alumno.nombre shouldBe "María González"
                alumno.correo shouldBe "maria@educanet.com"
            }

            then("debe tener la fecha de registro") {
                alumno.fechaRegistro shouldBe "2025-01-15"
            }
        }
    }

    given("un Profesor que hereda de Usuario") {

        `when`("se crea un Profesor") {
            val profesor = Profesor(
                id = "prof001",
                nombre = "Carlos López",
                correo = "carlos@educanet.com",
                clave = "prof123",
                fotoUrl = "https://foto.com/carlos.jpg",
                fechaRegistro = "2024-08-20"
            )

            then("debe tener el rol 'Profesor' por defecto") {
                profesor.rol shouldBe "Profesor"
            }

            then("debe heredar las propiedades de Usuario") {
                profesor.nombre shouldBe "Carlos López"
                profesor.correo shouldBe "carlos@educanet.com"
            }
        }
    }

    given("un Apoderado que hereda de Usuario") {

        `when`("se crea un Apoderado") {
            val apoderado = Apoderado(
                id = "apo001",
                nombre = "Roberto Martínez",
                correo = "roberto@mail.com",
                clave = "apo123",
                fotoUrl = "",
                fechaRegistro = "2025-02-01"
            )

            then("debe tener el rol 'Apoderado' por defecto") {
                apoderado.rol shouldBe "Apoderado"
            }
        }
    }
})
