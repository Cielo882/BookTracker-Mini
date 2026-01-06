package com.cielo.applibros.data.local
import android.content.Context
import android.net.Uri
import com.cielo.applibros.domain.model.Book
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class ExportImportHelper(private val context: Context) {

    private val gson = Gson()

    /**
     * Exporta la biblioteca a JSON
     * Retorna el URI del archivo creado
     */
    fun exportToJson(books: List<Book>): Uri? {
        try {
            val fileName = "BookTracker_backup_${getCurrentTimestamp()}.json"
            val file = File(context.getExternalFilesDir(null), fileName)

            val json = gson.toJson(books)
            file.writeText(json)

            return Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Importa biblioteca desde JSON
     * Retorna la lista de libros o null si falla
     */
    fun importFromJson(uri: Uri): List<Book>? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader()?.use { it.readText() }

            val type = object : TypeToken<List<Book>>() {}.type
            return gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getCurrentTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        return formatter.format(Date())
    }
}