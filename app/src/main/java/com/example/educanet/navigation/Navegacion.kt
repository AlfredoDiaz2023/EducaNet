package com.example.educanet.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.educanet.ui.screens.clasesvirtuales.AddClaseVirtualScreen
import com.example.educanet.ui.screens.clasesvirtuales.ClasesVirtualesScreen
import com.example.educanet.ui.screens.libro.AddLibroScreen
import com.example.educanet.ui.screens.login.LoginScreen
import com.example.educanet.ui.screens.progresoacademico.ProgresoAcademicoScreen
import com.example.educanet.ui.screens.registro.RegistroScreen
import com.example.educanet.ui.screens.perfil.PerfilAdminScreen
import com.example.educanet.ui.screens.perfil.PerfilProfesorScreen
import com.example.educanet.ui.screens.perfil.PerfilApoderadoScreen
import com.example.educanet.ui.screens.perfil.PerfilAlumnoScreen
import com.example.educanet.ui.screens.menu.MenuScreen
import com.example.educanet.ui.screens.libro.LibroScreen
import com.example.educanet.ui.screens.notificaciones.NotificacionesScreen
import com.example.educanet.ui.screens.tutorias.TutoriasScreen
import com.example.educanet.ui.screens.videoApoyo.AddVideoScreen
import com.example.educanet.ui.screens.videoApoyo.VideoApoyoScreen

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

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

        composable("register") {
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }

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
                onLibroClick = { navController.navigate("libros/$rol") },
                onVideoClick = { navController.navigate("video_apoyo") },
                onClaseVirtualClick = { navController.navigate("clases_virtuales/$rol") },
                onTutoriaClick = { navController.navigate("tutorias") },
                onProgresoAcademicoClick = { navController.navigate("progreso_academico") },
                onVerNotificaciones = { navController.navigate("notificaciones") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            "libros/{rol}",
            arguments = listOf(navArgument("rol") { type = NavType.StringType })
        ) { backStackEntry ->
            val rol = backStackEntry.arguments?.getString("rol") ?: ""
            LibroScreen(
                rol = rol,
                onBack = { navController.popBackStack() },
                onAddLibro = { navController.navigate("add_libro") }
            )
        }

        composable("add_libro") {
            AddLibroScreen(onBack = { navController.popBackStack() })
        }

        composable("video_apoyo") {
            VideoApoyoScreen(
                onBack = { navController.popBackStack() },
                onAddVideo = { navController.navigate("add_video") }
            )
        }

        composable("add_video") {
            AddVideoScreen(onBack = { navController.popBackStack() })
        }

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

        composable("tutorias") {
            TutoriasScreen(onBack = { navController.popBackStack() })
        }

        composable("progreso_academico") {
            ProgresoAcademicoScreen(onBack = { navController.popBackStack() })
        }

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

        composable("notificaciones") {
            NotificacionesScreen(onBack = { navController.popBackStack() })
        }
    }
}
