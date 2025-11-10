package com.example.educanet.ui.screens.clasesvirtuales

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.model.ClaseVirtual
import com.example.educanet.ui.common.Logo
import androidx.compose.ui.graphics.Color
import com.example.educanet.viewmodel.ClaseVirtualViewModel

@Composable
fun ClasesVirtualesScreen(
    rol: String,
    onBack: () -> Unit,
    onAddClase: () -> Unit,
    claseVirtualViewModel: ClaseVirtualViewModel = viewModel()
) {
    val clases by claseVirtualViewModel.clases.collectAsState()
    val cargando by claseVirtualViewModel.cargando.collectAsState()

    // 🔹 Fondo con imagen + contenido principal
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.logo), // tu drawable
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.08f // transparencia del fondo
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo superior pequeño y con transparencia
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo EducaNet",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Clases Virtuales",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(clases) { clase ->
                        ClaseVirtualItem(clase = clase)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (rol == "Profesor") {
                Button(
                    onClick = onAddClase,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Agregar Clase")
                }
            }
        }
    }
}

@Composable
fun ClaseVirtualItem(
    clase: ClaseVirtual
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(clase.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Nivel: ${clase.nivel}")
            clase.profesor?.let {
                Text("Profesor: ${it.nombre}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clase.meet))
                context.startActivity(intent)
            }) {
                Text("Iniciar Clase")
            }
        }
    }
}
