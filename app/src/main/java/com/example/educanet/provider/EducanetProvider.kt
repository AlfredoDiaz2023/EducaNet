package com.example.educanet.provider


import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class EducanetProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.educanet.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/libros")
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        // Simulamos exponer datos de libros para otras apps
        val columnas = arrayOf("id", "nombre", "nivel")
        val cursor = MatrixCursor(columnas)

        // Aquí podrías poner datos reales si tuvieras una base de datos local (Room/SQLite)
        // Como usas Firebase, exponemos datos de ejemplo para cumplir el requerimiento académico
        cursor.addRow(arrayOf("1", "Historia de Chile", "5to Basico"))
        cursor.addRow(arrayOf("2", "Matematicas Avanzadas", "4to Medio"))

        return cursor
    }

    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.example.libros"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
}