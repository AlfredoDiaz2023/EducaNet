package com.example.educanet.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.educanet.ui.CameraScreen
import com.example.educanet.ui.screens.clasesvirtuales.AddClaseVirtualScreen
import com.example.educanet.ui.screens.clasesvirtuales.ClasesVirtualesScreen
import com.example.educanet.ui.screens.libro.AddLibroScreen
import com.example.educanet.ui.screens.libro.LibroScreen
import com.example.educanet.ui.screens.login.LoginScreen
import com.example.educanet.ui.screens.progresoacademico.AddProgresoAcademicoScreen
import com.example.educanet.ui.screens.progresoacademico.ProgresoAcademicoScreen
import com.example.educanet.ui.screens.menu.MenuScreen
import com.example.educanet.ui.screens.notificaciones.NotificacionesScreen
import com.example.educanet.ui.screens.perfil.*
import com.example.educanet.ui.screens.videoApoyo.AddVideoScreen
import com.example.educanet.ui.screens.videoApoyo.VideoApoyoScreen
import com.example.educanet.ui.screens.registro.RegistroScreen

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // LOGIN
        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { user ->
                    navController.navigate("menu/${user.nombre}/${user.rol}") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // REGISTER
        composable("register") {
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }

        // MENU
        composable(
            "menu/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Usuario"
            val rol = backStackEntry.arguments?.getString("rol") ?: "Alumno"

            MenuScreen(
                nombre = nombre,
                rol = rol,
                onLibroClick = { navController.navigate("libros/$rol/$nombre") },
                onVideoClick = { navController.navigate("video_apoyo/$rol") },
                onClaseVirtualClick = { navController.navigate("clases_virtuales/$rol") },
                onProgresoAcademicoClick = { navController.navigate("progreso_academico/$rol") },
                onVerNotificaciones = { navController.navigate("notificaciones") },
                onCameraClick = { navController.navigate("camera") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // LIBROS
        composable(
            "libros/{rol}/{nombre}",
            arguments = listOf(
                navArgument("rol") { type = NavType.StringType },
                navArgument("nombre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rol = backStackEntry.arguments?.getString("rol") ?: ""
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            LibroScreen(
                rol = rol,
                nombre = nombre,
                onBack = { navController.popBackStack() },
                onAddLibro = { navController.navigate("add_libro") }
            )
        }

        composable("add_libro") {
            AddLibroScreen(onBack = { navController.popBackStack() })
        }

        // VIDEO APOYO
        composable("video_apoyo/{rol}",
            arguments = listOf(navArgument("rol") { type = NavType.StringType })
        ) { backStackEntry ->
            val rol = backStackEntry.arguments?.getString("rol") ?: ""
            VideoApoyoScreen(
                rol = rol,
                onBack = { navController.popBackStack() },
                onAddVideo = { navController.navigate("add_video") }
            )
        }

        composable("add_video") {
            AddVideoScreen(onBack = { navController.popBackStack() })
        }

        // CLASES VIRTUALES
        composable(
            "clases_virtuales/{rol}",
            arguments = listOf(navArgument("rol") { type = NavType.StringType })
        ) { backStackEntry ->
            val rol = backStackEntry.arguments?.getString("rol") ?: ""
            ClasesVirtualesScreen(
                rol = rol,
                onBack = { navController.popBackStack() },
                onAddClase = { navController.navigate("add_clase_virtual") }
            )
        }

        composable("add_clase_virtual") {
            AddClaseVirtualScreen(onBack = { navController.popBackStack() })
        }

        // PROGRESO ACADEMICO
        composable("progreso_academico/{rol}",
            arguments = listOf(navArgument("rol") { type = NavType.StringType })
        ) { backStackEntry ->
            val rol = backStackEntry.arguments?.getString("rol") ?: "Alumno"
            ProgresoAcademicoScreen(
                rol = rol,
                onBack = { navController.popBackStack() },
                onAddNota = { navController.navigate("add_progreso_academico") }
            )
        }

        // ADD NOTA
        composable(
            "add_nota/{progresoId}",
            arguments = listOf(navArgument("progresoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val progresoId = backStackEntry.arguments?.getString("progresoId") ?: ""
            AddProgresoAcademicoScreen(onBack = { navController.popBackStack() })
        }

        // ADD PROGRESO
        composable("add_progreso_academico") {
            AddProgresoAcademicoScreen(onBack = { navController.popBackStack() })
        }

        // PERFIL
        composable(
            "perfil_admin/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) {
            val nombre = it.arguments?.getString("nombre") ?: "Administrador"
            PerfilAdminScreen(nombre = nombre, onLogout = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        composable(
            "perfil_profesor/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) {
            val nombre = it.arguments?.getString("nombre") ?: "Profesor"
            PerfilProfesorScreen(nombre = nombre, onLogout = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        composable(
            "perfil_apoderado/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) {
            val nombre = it.arguments?.getString("nombre") ?: "Apoderado"
            PerfilApoderadoScreen(nombre = nombre, onLogout = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        composable(
            "perfil_alumno/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) {
            val nombre = it.arguments?.getString("nombre") ?: "Alumno"
            PerfilAlumnoScreen(nombre = nombre, onLogout = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        // NOTIFICACIONES
        composable("notificaciones") {
            NotificacionesScreen(onBack = { navController.popBackStack() })
        }

        // CAMARA
        composable("camera") {
            CameraScreen(
                onImageCaptured = { uri ->
                    // Envía la URI de la imagen a la pantalla anterior y regresa
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_image_uri", uri.toString())
                    navController.popBackStack()
                },
                onError = {
                    // Simplemente regresa si hay un error
                    navController.popBackStack()
                }
            )
        }
    }
}
