package com.example.proyecto

import android.content.Context
import android.util.Log
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
    private val dbName = "Eventos.db"

    fun obtenerUltimoRespaldo(): String? {
        return prefs.getString("last_backup", null)
    }

    fun obtenerUltimaRestauracion(): String? {
        return prefs.getString("last_restore", null)
    }

    private fun guardarFechaRespaldo() {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = sdf.format(Date())
        prefs.edit().putString("last_backup", fecha).apply()
    }

    private fun guardarFechaRestauracion() {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = sdf.format(Date())
        prefs.edit().putString("last_restore", fecha).apply()
    }

    suspend fun backupToDrive(): Boolean = withContext(Dispatchers.IO) {
        try {
            val driveService = GoogleDriveService.getDriveService(context) ?: return@withContext false
            val dbFile = context.getDatabasePath(dbName)
            if (!dbFile.exists()) {
                Log.e("BackupManager", "Base de datos no encontrada en: ${dbFile.absolutePath}")
                return@withContext false
            }

            val metadata = File().apply {
                name = dbName
                parents = listOf("root")
            }

            val mediaContent = FileContent("application/octet-stream", dbFile)

            // Buscar si ya existe para actualizarlo o crear uno nuevo
            val query = "name = '$dbName' and 'root' in parents and trashed = false"
            val result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
            val existingFile = result.files.firstOrNull()

            if (existingFile != null) {
                driveService.files().update(existingFile.id, null, mediaContent).execute()
            } else {
                driveService.files().create(metadata, mediaContent).execute()
            }

            guardarFechaRespaldo()
            true
        } catch (e: Exception) {
            Log.e("BackupManager", "Error backup: ${e.message}", e)
            false
        }
    }

    suspend fun restoreFromDrive(): Boolean = withContext(Dispatchers.IO) {
        try {
            val driveService = GoogleDriveService.getDriveService(context) ?: return@withContext false
            val query = "name = '$dbName' and 'root' in parents and trashed = false"
            val result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
            val driveFile = result.files.firstOrNull() ?: return@withContext false

            val dbFile = context.getDatabasePath(dbName)
            
            // Cerrar conexiones antes de sobrescribir
            context.deleteDatabase(dbName) 
            
            val outputStream = FileOutputStream(dbFile)
            driveService.files().get(driveFile.id).executeMediaAndDownloadTo(outputStream)
            outputStream.close()

            guardarFechaRestauracion()
            true
        } catch (e: Exception) {
            Log.e("BackupManager", "Error restore: ${e.message}", e)
            false
        }
    }
}
