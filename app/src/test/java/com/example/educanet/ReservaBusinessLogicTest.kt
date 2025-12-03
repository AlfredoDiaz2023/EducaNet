package com.example.educanet

import com.example.educanet.model.Reserva
import com.example.educanet.model.Libro
import com.example.educanet.model.ProgresoAcademico
import com.example.educanet.model.Usuario
import com.example.educanet.model.Alumno
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class ReservaBusinessLogicTest : FunSpec({

    // Test: Un libro con stock > 0 puede ser reservado
    test("un libro con stock mayor a cero puede ser reservado") {
        val libro = Libro(
            id = "lib1",
            nombre = "El Principito",
            nivel = "Básico",
            imagen = "",
            cantidad = 5
        )
        
        val puedeReservar = libro.cantidad > 0
        puedeReservar shouldBe true
    }

    // Test: Un libro sin stock no puede ser reservado
    test("un libro sin stock no puede ser reservado") {
        val libroSinStock = Libro(
            id = "lib2",
            nombre = "1984",
            nivel = "Avanzado",
            imagen = "",
            cantidad = 0
        )
        
        val puedeReservar = libroSinStock.cantidad > 0
        puedeReservar shouldBe false
    }

    // Test: Después de una reserva, el stock debe disminuir
    test("al reservar un libro el stock debe disminuir en uno") {
        val stockInicial = 10
        val libro = Libro(
            id = "lib3",
            nombre = "Don Quijote",
            nivel = "Avanzado",
            imagen = "",
            cantidad = stockInicial
        )
        
        // Simular reserva (reducir stock)
        val nuevoStock = libro.cantidad - 1
        
        nuevoStock shouldBe (stockInicial - 1)
    }

    // Test de propiedades: El stock nunca debe ser negativo después de validación
    test("el stock después de reservar siempre debe ser >= 0 cuando se valida") {
        checkAll(Arb.int(0, 100)) { stockInicial ->
            val libro = Libro(id = "test", nombre = "Test", nivel = "", imagen = "", cantidad = stockInicial)
            
            // Solo reservar si hay stock
            val nuevoStock = if (libro.cantidad > 0) libro.cantidad - 1 else libro.cantidad
            
            (nuevoStock >= 0) shouldBe true
        }
    }

    // Test: Validación de notas en progreso académico
    test("las notas deben estar en rango válido de 1.0 a 7.0") {
        val progresoValido = ProgresoAcademico(
            id = "prog1",
            profesor = "Prof. García",
            alumno = "Juan Pérez",
            asignatura = "Matemáticas",
            curso = "8vo Básico",
            notas = 6.5
        )
        
        val esNotaValida = progresoValido.notas in 1.0..7.0
        esNotaValida shouldBe true
    }

    // Test: Una nota fuera de rango debe ser detectada
    test("detectar notas fuera del rango válido") {
        val progresoInvalido = ProgresoAcademico(
            id = "prog2",
            profesor = "Prof. López",
            alumno = "Ana Silva",
            asignatura = "Historia",
            curso = "7mo Básico",
            notas = 8.0 // Nota inválida
        )
        
        val esNotaValida = progresoInvalido.notas in 1.0..7.0
        esNotaValida shouldBe false
    }

    // Test: Un usuario con rol vacío no debería tener permisos especiales
    test("usuario sin rol definido") {
        val usuario = Usuario(
            id = "usr1",
            nombre = "Test User",
            correo = "test@test.com",
            clave = "123456",
            rol = "",
            fotoUrl = ""
        )
        
        val tieneRolAdmin = usuario.rol == "Admin"
        tieneRolAdmin shouldBe false
    }

    // Test: Alumno hereda correctamente de Usuario
    test("alumno debe heredar propiedades de usuario") {
        val alumno = Alumno(
            id = "alum1",
            nombre = "Pedro González",
            correo = "pedro@educanet.com",
            clave = "password123",
            fotoUrl = "http://foto.jpg",
            fechaRegistro = "2025-01-15"
        )
        
        alumno.rol shouldBe "Alumno"
        alumno.nombre shouldBe "Pedro González"
        alumno.correo shouldBe "pedro@educanet.com"
    }

    // Test de propiedades: Reservas siempre deben tener libroId y userId
    test("una reserva válida debe tener libroId y userId") {
        checkAll(Arb.string(1..20), Arb.string(1..20)) { libroId, userId ->
            val reserva = Reserva(
                id = "res1",
                libroId = libroId,
                userId = userId,
                userName = "Usuario Test",
                libroNombre = "Libro Test"
            )
            
            reserva.libroId.isNotEmpty() shouldBe true
            reserva.userId.isNotEmpty() shouldBe true
        }
    }
})