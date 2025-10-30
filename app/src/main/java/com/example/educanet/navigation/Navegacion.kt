package com.example.educanet.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.educanet.ui.screens.login.LoginScreen
import com.example.educanet.ui.screens.registro.RegistroScreen
import com.example.educanet.ui.screens.perfil.PerfilAdminScreen
import com.example.educanet.ui.screens.perfil.PerfilProfesorScreen
import com.example.educanet.ui.screens.perfil.PerfilApoderadoScreen
import com.example.educanet.ui.screens.perfil.PerfilAlumnoScreen
import com.example.educanet.ui.screens.menu.MenuScreen
import com.example.educanet.ui.screens.carrito.CarritoScreen
import com.example.educanet.ui.screens.libro.LibroScreen
import com.example.educanet.viewmodel.CarritoViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val carritoViewModel: CarritoViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // 🔹 Pantalla de login
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

        // 🔹 Pantalla de registro
        composable("register") {
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }

        // 🔹 Pantalla de menú principal
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
                onLibroClick = { navController.navigate("libros") }, // 👈 navegación hacia LibroScreen
                onVerCarrito = { navController.navigate("carrito") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = carritoViewModel
            )
        }

        // 🔹 Pantalla de libros (usa LibroRepository)
        composable("libros") { // 👈
            LibroScreen(onBack = { navController.popBackStack() })
        }

        // 🔹 Pantalla de carrito
        composable("carrito") {
            CarritoScreen(
                onVolverAlMenu = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = carritoViewModel
            )
        }

        // 🔹 Perfiles
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
    }
}
