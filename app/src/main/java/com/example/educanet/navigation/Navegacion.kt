package com.example.educanet.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.educanet.model.*
import com.example.educanet.ui.CameraScreen
import com.example.educanet.ui.screens.carrito.CarritoScreen
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
import com.example.educanet.ui.screens.reservas.ReservasScreen
import com.example.educanet.ui.screens.videoApoyo.AddVideoScreen
import com.example.educanet.ui.screens.videoApoyo.VideoApoyoScreen
import com.example.educanet.ui.screens.registro.RegistroScreen
import com.example.educanet.ui.screens.registro.SeleccionarAlumnoScreen
import com.example.educanet.ui.screens.imagen.ImagePickerScreen
import com.example.educanet.ui.screens.admin.*
import com.example.educanet.viewmodel.CarritoViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavegacion() {

    val navController = rememberNavController()
    val carritoViewModel: CarritoViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { user ->
                    val fotoUrlEncoded = if (user.fotoUrl != null) {
                        URLEncoder.encode(user.fotoUrl, StandardCharsets.UTF_8.toString())
                    } else {
                        ""
                    }

                    val correoEncoded = URLEncoder.encode(user.correo, StandardCharsets.UTF_8.toString())

                    val route = buildString {
                        append("menu/${user.nombre}/${user.rol}")
                        val params = mutableListOf<String>()
                        if (fotoUrlEncoded.isNotEmpty()) params.add("fotoUrl=$fotoUrlEncoded")
                        if (correoEncoded.isNotEmpty()) params.add("correo=$correoEncoded")
                        if (params.isNotEmpty()) append("?${params.joinToString("&")}")
                    }

                    navController.navigate(route) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() },
                onRegisterSuccessWithUser = { user ->
                    val fotoUrlEncoded = if (user.fotoUrl.isNotEmpty()) {
                        URLEncoder.encode(user.fotoUrl, StandardCharsets.UTF_8.toString())
                    } else {
                        ""
                    }
                    
                    val correoEncoded = URLEncoder.encode(user.correo, StandardCharsets.UTF_8.toString())

                    val route = buildString {
                        append("menu/${user.nombre}/${user.rol}")
                        val params = mutableListOf<String>()
                        if (fotoUrlEncoded.isNotEmpty()) params.add("fotoUrl=$fotoUrlEncoded")
                        if (correoEncoded.isNotEmpty()) params.add("correo=$correoEncoded")
                        if (params.isNotEmpty()) append("?${params.joinToString("&")}")
                    }

                    navController.navigate(route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onApoderadoRegistrado = { correoApoderado ->
                    val correoEncoded = URLEncoder.encode(correoApoderado, StandardCharsets.UTF_8.toString())
                    navController.navigate("seleccionar_alumno/$correoEncoded") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "seleccionar_alumno/{correoApoderado}",
            arguments = listOf(
                navArgument("correoApoderado") { type = NavType.StringType }
            )
        ) { entry ->
            val correoApoderado = entry.arguments?.getString("correoApoderado") ?: ""
            SeleccionarAlumnoScreen(
                correoApoderado = correoApoderado,
                onAlumnoSeleccionado = {
                    // Después de vincular, ir al login
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBack = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "menu/{nombre}/{rol}?fotoUrl={fotoUrl}&correo={correo}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType },
                navArgument("fotoUrl") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("correo") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val nombre = entry.arguments?.getString("nombre") ?: "Usuario"
            val rol = entry.arguments?.getString("rol") ?: "Alumno"
            val fotoUrl = entry.arguments?.getString("fotoUrl")
            val correo = entry.arguments?.getString("correo")

            var correoAlumnoVinculado by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(rol, correo) {
                if (rol == "Apoderado" && correo != null) {
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val decodedCorreo = java.net.URLDecoder.decode(correo, StandardCharsets.UTF_8.toString())
                        val snapshot = db.collection("usuario")
                            .whereEqualTo("correo", decodedCorreo)
                            .get()
                            .await()
                        
                        if (!snapshot.isEmpty) {
                            val doc = snapshot.documents.first()
                            correoAlumnoVinculado = doc.getString("alumnoVinculadoCorreo")
                            Log.d("Navegacion", "Alumno vinculado encontrado: $correoAlumnoVinculado")
                        }
                    } catch (e: Exception) {
                        Log.e("Navegacion", "Error buscando alumno vinculado: ${e.message}")
                    }
                }
            }

            MenuScreen(
                nombre = nombre,
                rol = rol,
                fotoUrl = fotoUrl,
                onLibroClick = { navController.navigate("libros/$rol/$nombre") },
                onVideoClick = { navController.navigate("video_apoyo/$rol") },
                onClaseVirtualClick = { navController.navigate("clases_virtuales/$rol") },
                onProgresoAcademicoClick = {
                    val correoParaProgreso = if (rol == "Apoderado" && correoAlumnoVinculado != null) {
                        URLEncoder.encode(correoAlumnoVinculado, StandardCharsets.UTF_8.toString())
                    } else {
                        correo?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                    }
                    val rolParaProgreso = if (rol == "Apoderado") "Alumno" else rol
                    navController.navigate("progreso_academico/$rolParaProgreso?correo=$correoParaProgreso") 
                },
                onVerNotificaciones = { navController.navigate("notificaciones") },
                onCameraClick = { navController.navigate("camera") },
                onPerfilClick = {
                    val perfilRoute = when (rol) {
                        "Administrador" -> "perfil_admin/$nombre"
                        "Profesor" -> "perfil_profesor/$nombre"
                        "Apoderado" -> "perfil_apoderado/$nombre"
                        else -> "perfil_alumno/$nombre"
                    }
                    navController.navigate(perfilRoute)
                },
                onAdminPanelClick = { navController.navigate("admin_panel") },
                onVerResenasClick = { navController.navigate("resenas_recientes") },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "libros/{rol}/{nombre}",
            arguments = listOf(
                navArgument("rol") { type = NavType.StringType },
                navArgument("nombre") { type = NavType.StringType }
            )
        ) { entry ->
            val rol = entry.arguments?.getString("rol") ?: ""
            val nombre = entry.arguments?.getString("nombre") ?: ""

            LibroScreen(
                rol = rol,
                nombre = nombre,
                carritoViewModel = carritoViewModel,
                onBack = { navController.popBackStack() },
                onAddLibro = { navController.navigate("add_libro") },
                onCarritoClick = {
                    navController.navigate("carrito/$nombre") {
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }

        composable(
            route = "carrito/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { entry ->
            val nombre = entry.arguments?.getString("nombre") ?: ""
            CarritoScreen(
                onBack = { navController.popBackStack() },
                carritoViewModel = carritoViewModel,
                userName = nombre
            )
        }

        composable("add_libro") {
            AddLibroScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "video_apoyo/{rol}",
            arguments = listOf(navArgument("rol") { type = NavType.StringType })
        ) { entry ->
            val rol = entry.arguments?.getString("rol") ?: ""
            VideoApoyoScreen(
                rol = rol,
                onBack = { navController.popBackStack() },
                onAddVideo = { navController.navigate("add_video") }
            )
        }

        composable("add_video") {
            AddVideoScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "clases_virtuales/{rol}",
            arguments = listOf(navArgument("rol") { type = NavType.StringType })
        ) { entry ->
            val rol = entry.arguments?.getString("rol") ?: ""
            ClasesVirtualesScreen(
                rol = rol,
                onBack = { navController.popBackStack() },
                onAddClase = { navController.navigate("add_clase_virtual") }
            )
        }

        composable("add_clase_virtual") {
            AddClaseVirtualScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "progreso_academico/{rol}?correo={correo}",
            arguments = listOf(
                navArgument("rol") { type = NavType.StringType },
                navArgument("correo") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val rol = entry.arguments?.getString("rol") ?: "Alumno"
            val correo = entry.arguments?.getString("correo")
            ProgresoAcademicoScreen(
                rol = rol,
                correoAlumno = correo,
                onBack = { navController.popBackStack() },
                onAddNota = { navController.navigate("add_progreso_academico") }
            )
        }

        composable("add_progreso_academico") {
            AddProgresoAcademicoScreen(onBack = { navController.popBackStack() })
        }

        composable(
            "perfil_admin/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            // Obtener imagen capturada de la cámara si existe
            val capturedImageUri = backStackEntry.savedStateHandle.get<String>("captured_image_uri")?.let {
                android.net.Uri.parse(it)
            }
            
            PerfilAdminScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onCameraClick = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("return_route", "perfil_admin/${backStackEntry.arguments?.getString("nombre") ?: ""}")
                    navController.navigate("camera")
                },
                onBack = { navController.popBackStack() },
                capturedImageUri = capturedImageUri
            )
        }

        composable(
            "perfil_profesor/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            // Obtener imagen capturada de la cámara si existe
            val capturedImageUri = backStackEntry.savedStateHandle.get<String>("captured_image_uri")?.let {
                android.net.Uri.parse(it)
            }
            
            PerfilProfesorScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onCameraClick = { navController.navigate("camera_perfil") },
                onBack = { navController.popBackStack() },
                capturedImageUri = capturedImageUri
            )
        }

        composable(
            "perfil_apoderado/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) {
            PerfilApoderadoScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "perfil_alumno/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            // Obtener imagen capturada de la cámara si existe
            val capturedImageUri = backStackEntry.savedStateHandle.get<String>("captured_image_uri")?.let {
                android.net.Uri.parse(it)
            }
            
            PerfilAlumnoScreen(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                },
                onCameraClick = { navController.navigate("camera_perfil") },
                onBack = { navController.popBackStack() },
                capturedImageUri = capturedImageUri
            )
        }

        composable("camera_perfil") {
            CameraScreen(
                onImageCaptured = { uri ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("captured_image_uri", uri.toString())
                    navController.popBackStack()
                },
                onError = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("notificaciones") {
            NotificacionesScreen(onBack = { navController.popBackStack() })
        }

        composable("resenas_recientes") {
            com.example.educanet.ui.screens.resenas.ResenasRecientesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("camera") {
            CameraScreen(
                onImageCaptured = { uri ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("captured_image_uri", uri.toString())
                    navController.popBackStack()
                },
                onError = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("imagePicker") {
            ImagePickerScreen(onBack = { navController.popBackStack() })
        }

        composable("reservas") {
            ReservasScreen(onBack = { navController.popBackStack() })
        }

        composable("admin_panel") {
            AdminPanelScreen(
                onBack = { navController.popBackStack() },
                onGestionLibros = { navController.navigate("gestion_libros") },
                onGestionUsuarios = { navController.navigate("gestion_usuarios") },
                onHistorialReservas = { navController.navigate("historial_reservas") },
                onEditarPerfil = { navController.navigate("editar_perfil_admin") }
            )
        }

        composable("gestion_libros") {
            GestionLibrosScreen(onBack = { navController.popBackStack() })
        }

        composable("gestion_usuarios") {
            GestionUsuariosScreen(onBack = { navController.popBackStack() })
        }

        composable("historial_reservas") {
            HistorialReservasScreen(onBack = { navController.popBackStack() })
        }

        composable("editar_perfil_admin") {
            EditarPerfilAdminScreen(onBack = { navController.popBackStack() })
        }
    }
}