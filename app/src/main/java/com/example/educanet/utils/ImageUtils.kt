package com.example.educanet.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

            // 1. REDIMENSIONAR AGRESIVAMENTE
            // 300x300 es perfecto para una foto de perfil redonda y genera un texto corto.
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 300, 300, true)

            val byteArrayOutputStream = ByteArrayOutputStream()

            // 2. COMPRIMIR CALIDAD
            // Bajamos al 50%. La imagen se verá bien en el celular pero pesará poquísimo.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // 3. CONVERTIR
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            // Retornamos el formato listo
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}