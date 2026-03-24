package com.example.educanet

import com.example.educanet.model.Asignatura
import com.example.educanet.model.AsignaturasPredefinidas
import com.example.educanet.model.NivelEducativo
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.string.shouldNotBeBlank

class AsignaturaTest : BehaviorSpec({

    // Test del modelo Asignatura
    given("una asignatura") {
        val asignatura = Asignatura(
            id = "1",
            nombre = "Matemática",
            descripcion = "Desarrollo del pensamiento lógico-matemático",
            nivelEducativo = NivelEducativo.TODOS,
            cursos = listOf("1° Básico", "2° Básico", "3° Básico"),
            esObligatoria = true,
            activa = true
        )

        `when`("se crea correctamente") {
            then("debe tener todos los campos correctos") {
                asignatura.id shouldBe "1"
                asignatura.nombre shouldBe "Matemática"
                asignatura.descripcion shouldBe "Desarrollo del pensamiento lógico-matemático"
                asignatura.nivelEducativo shouldBe NivelEducativo.TODOS
                asignatura.cursos shouldHaveSize 3
                asignatura.esObligatoria shouldBe true
                asignatura.activa shouldBe true
            }
        }

        `when`("se verifica si es obligatoria") {
            then("debe ser obligatoria") {
                asignatura.esObligatoria shouldBe true
            }
        }

        `when`("se copia como electiva") {
            val electiva = asignatura.copy(esObligatoria = false)
            
            then("debe ser electiva") {
                electiva.esObligatoria shouldBe false
            }
        }
    }

    // Test de los niveles educativos
    given("los niveles educativos disponibles") {
        val niveles = NivelEducativo.entries

        `when`("se verifica la cantidad de niveles") {
            then("debe haber 4 niveles") {
                niveles shouldHaveSize 4
            }
        }

        `when`("se verifican los nombres de los niveles") {
            then("deben tener nombres descriptivos") {
                NivelEducativo.EDUCACION_BASICA.displayName shouldBe "Educación Básica"
                NivelEducativo.EDUCACION_MEDIA.displayName shouldBe "Educación Media"
                NivelEducativo.EDUCACION_MEDIA_SUPERIOR.displayName shouldBe "3° y 4° Medio"
                NivelEducativo.TODOS.displayName shouldBe "Todos los niveles"
            }
        }
    }

    // Test de asignaturas predefinidas
    given("las asignaturas predefinidas del currículo chileno") {
        val todasAsignaturas = AsignaturasPredefinidas.todasLasAsignaturas

        `when`("se verifica la lista completa") {
            then("debe haber al menos 12 asignaturas") {
                todasAsignaturas shouldHaveAtLeastSize 12
            }

            then("todas deben tener nombre") {
                todasAsignaturas.forEach { asignatura ->
                    asignatura.nombre.shouldNotBeBlank()
                }
            }

            then("todas deben tener al menos un curso asignado") {
                todasAsignaturas.forEach { asignatura ->
                    asignatura.cursos.shouldNotBeEmpty()
                }
            }
        }

        `when`("se verifican las asignaturas base") {
            val asignaturasBase = AsignaturasPredefinidas.asignaturasBase
            
            then("debe contener Lenguaje y Comunicación") {
                asignaturasBase.any { it.nombre == "Lenguaje y Comunicación" } shouldBe true
            }

            then("debe contener Matemática") {
                asignaturasBase.any { it.nombre == "Matemática" } shouldBe true
            }

            then("debe contener Historia, Geografía y Ciencias Sociales") {
                asignaturasBase.any { it.nombre == "Historia, Geografía y Ciencias Sociales" } shouldBe true
            }

            then("debe contener Ciencias Naturales") {
                asignaturasBase.any { it.nombre == "Ciencias Naturales" } shouldBe true
            }

            then("debe contener Educación Física y Salud") {
                asignaturasBase.any { it.nombre == "Educación Física y Salud" } shouldBe true
            }

            then("debe contener Orientación") {
                asignaturasBase.any { it.nombre == "Orientación" } shouldBe true
            }

            then("debe contener Tecnología") {
                asignaturasBase.any { it.nombre == "Tecnología" } shouldBe true
            }
        }

        `when`("se verifican las asignaturas de educación básica") {
            val asignaturasBasica = AsignaturasPredefinidas.asignaturasBasica
            
            then("debe contener Artes Visuales") {
                asignaturasBasica.any { it.nombre == "Artes Visuales" } shouldBe true
            }

            then("debe contener Música") {
                asignaturasBasica.any { it.nombre == "Música" } shouldBe true
            }

            then("Artes Visuales solo debe estar en cursos básicos") {
                val artesVisuales = asignaturasBasica.find { it.nombre == "Artes Visuales" }
                artesVisuales shouldNotBe null
                artesVisuales!!.cursos.all { it.contains("Básico") } shouldBe true
            }
        }

        `when`("se verifica Inglés desde 5° Básico") {
            val ingles = AsignaturasPredefinidas.asignaturasDesde5Basico.find { it.nombre == "Inglés" }
            
            then("Inglés debe existir") {
                ingles shouldNotBe null
            }

            then("Inglés no debe estar en 1° a 4° Básico") {
                ingles!!.cursos shouldNotContain "1° Básico"
                ingles.cursos shouldNotContain "2° Básico"
                ingles.cursos shouldNotContain "3° Básico"
                ingles.cursos shouldNotContain "4° Básico"
            }

            then("Inglés debe estar desde 5° Básico") {
                ingles!!.cursos shouldContain "5° Básico"
                ingles.cursos shouldContain "6° Básico"
                ingles.cursos shouldContain "7° Básico"
                ingles.cursos shouldContain "8° Básico"
            }

            then("Inglés debe estar en educación media") {
                ingles!!.cursos shouldContain "1° Medio"
                ingles.cursos shouldContain "2° Medio"
                ingles.cursos shouldContain "3° Medio"
                ingles.cursos shouldContain "4° Medio"
            }
        }

        `when`("se verifican las asignaturas de 3° y 4° Medio") {
            val asignaturasMediaSuperior = AsignaturasPredefinidas.asignaturasMediaSuperior
            
            then("debe contener Filosofía") {
                asignaturasMediaSuperior.any { it.nombre == "Filosofía" } shouldBe true
            }

            then("debe contener Educación Ciudadana") {
                asignaturasMediaSuperior.any { it.nombre == "Educación Ciudadana" } shouldBe true
            }

            then("debe contener Ciencias para la Ciudadanía") {
                asignaturasMediaSuperior.any { it.nombre == "Ciencias para la Ciudadanía" } shouldBe true
            }

            then("debe contener Lengua y Literatura") {
                asignaturasMediaSuperior.any { it.nombre == "Lengua y Literatura" } shouldBe true
            }

            then("debe contener Matemática Avanzada") {
                asignaturasMediaSuperior.any { it.nombre == "Matemática Avanzada" } shouldBe true
            }

            then("Filosofía solo debe estar en 3° y 4° Medio") {
                val filosofia = asignaturasMediaSuperior.find { it.nombre == "Filosofía" }
                filosofia shouldNotBe null
                filosofia!!.cursos shouldHaveSize 2
                filosofia.cursos shouldContain "3° Medio"
                filosofia.cursos shouldContain "4° Medio"
            }
        }
    }

    // Test de filtrado de asignaturas por curso
    given("la función de filtrado por curso") {
        
        `when`("se filtran asignaturas para 1° Básico") {
            val asignaturas1Basico = AsignaturasPredefinidas.obtenerAsignaturasPorCurso("1° Básico")
            
            then("debe incluir asignaturas base") {
                asignaturas1Basico.any { it.nombre == "Matemática" } shouldBe true
                asignaturas1Basico.any { it.nombre == "Lenguaje y Comunicación" } shouldBe true
            }

            then("debe incluir Artes Visuales y Música") {
                asignaturas1Basico.any { it.nombre == "Artes Visuales" } shouldBe true
                asignaturas1Basico.any { it.nombre == "Música" } shouldBe true
            }

            then("NO debe incluir Inglés") {
                asignaturas1Basico.any { it.nombre == "Inglés" } shouldBe false
            }

            then("NO debe incluir Filosofía") {
                asignaturas1Basico.any { it.nombre == "Filosofía" } shouldBe false
            }
        }

        `when`("se filtran asignaturas para 5° Básico") {
            val asignaturas5Basico = AsignaturasPredefinidas.obtenerAsignaturasPorCurso("5° Básico")
            
            then("debe incluir Inglés") {
                asignaturas5Basico.any { it.nombre == "Inglés" } shouldBe true
            }

            then("debe incluir asignaturas base") {
                asignaturas5Basico.any { it.nombre == "Matemática" } shouldBe true
            }
        }

        `when`("se filtran asignaturas para 3° Medio") {
            val asignaturas3Medio = AsignaturasPredefinidas.obtenerAsignaturasPorCurso("3° Medio")
            
            then("debe incluir Filosofía") {
                asignaturas3Medio.any { it.nombre == "Filosofía" } shouldBe true
            }

            then("debe incluir Educación Ciudadana") {
                asignaturas3Medio.any { it.nombre == "Educación Ciudadana" } shouldBe true
            }

            then("debe incluir Ciencias para la Ciudadanía") {
                asignaturas3Medio.any { it.nombre == "Ciencias para la Ciudadanía" } shouldBe true
            }

            then("debe incluir Inglés") {
                asignaturas3Medio.any { it.nombre == "Inglés" } shouldBe true
            }

            then("NO debe incluir Artes Visuales (solo hasta 8° Básico)") {
                asignaturas3Medio.any { it.nombre == "Artes Visuales" } shouldBe false
            }
        }

        `when`("se filtran asignaturas para 4° Medio") {
            val asignaturas4Medio = AsignaturasPredefinidas.obtenerAsignaturasPorCurso("4° Medio")
            
            then("debe incluir todas las asignaturas de media superior") {
                asignaturas4Medio.any { it.nombre == "Filosofía" } shouldBe true
                asignaturas4Medio.any { it.nombre == "Educación Ciudadana" } shouldBe true
                asignaturas4Medio.any { it.nombre == "Ciencias para la Ciudadanía" } shouldBe true
                asignaturas4Medio.any { it.nombre == "Lengua y Literatura" } shouldBe true
                asignaturas4Medio.any { it.nombre == "Matemática Avanzada" } shouldBe true
            }
        }
    }

    // Test de filtrado por nivel educativo
    given("la función de filtrado por nivel educativo") {
        
        `when`("se filtran asignaturas de Educación Básica") {
            val asignaturasBasica = AsignaturasPredefinidas.obtenerAsignaturasPorNivel(NivelEducativo.EDUCACION_BASICA)
            
            then("debe incluir Artes Visuales") {
                asignaturasBasica.any { it.nombre == "Artes Visuales" } shouldBe true
            }

            then("debe incluir asignaturas de nivel TODOS") {
                asignaturasBasica.any { it.nombre == "Matemática" } shouldBe true
            }
        }

        `when`("se filtran asignaturas de nivel TODOS") {
            val todasAsignaturas = AsignaturasPredefinidas.obtenerAsignaturasPorNivel(NivelEducativo.TODOS)
            
            then("debe incluir todas las asignaturas") {
                todasAsignaturas shouldHaveAtLeastSize 12
            }
        }
    }

    // Test de validación de asignatura
    given("validación de datos de asignatura") {
        
        `when`("se crea una asignatura sin nombre") {
            val asignaturaSinNombre = Asignatura(nombre = "")
            
            then("el nombre debe estar vacío") {
                asignaturaSinNombre.nombre shouldBe ""
            }
        }

        `when`("se crea una asignatura sin cursos") {
            val asignaturaSinCursos = Asignatura(nombre = "Test", cursos = emptyList())
            
            then("la lista de cursos debe estar vacía") {
                asignaturaSinCursos.cursos shouldHaveSize 0
            }
        }

        `when`("se crea una asignatura con valores por defecto") {
            val asignaturaDefault = Asignatura()
            
            then("debe tener valores por defecto correctos") {
                asignaturaDefault.id shouldBe ""
                asignaturaDefault.nombre shouldBe ""
                asignaturaDefault.descripcion shouldBe ""
                asignaturaDefault.nivelEducativo shouldBe NivelEducativo.EDUCACION_BASICA
                asignaturaDefault.cursos shouldHaveSize 0
                asignaturaDefault.esObligatoria shouldBe true
                asignaturaDefault.activa shouldBe true
            }
        }
    }
})

private infix fun <T> List<T>.shouldNotContain(element: T) {
    if (this.contains(element)) {
        throw AssertionError("List should not contain $element but it does")
    }
}
