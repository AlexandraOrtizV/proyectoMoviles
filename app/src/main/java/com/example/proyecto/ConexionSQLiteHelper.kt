package com.example.proyecto

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ConexionSQLiteHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 4
        private const val DATABASE_NAME = "Eventos.db" // O el nombre que prefieras

        // TABLA EVENTO
        const val TABLA_EVENTO = "evento"
        const val CAMPO_EV_ID = "id"
        const val CAMPO_EV_CAT = "cat" //------
        const val CAMPO_EV_FECHA = "fecha"
        const val CAMPO_EV_HORA = "hora"
        const val CAMPO_EV_UBI = "ubi"
        const val CAMPO_EV_CONTACTO = "contacto"
        const val CAMPO_EV_ESTATUS = "estatus" //-----
        const val CAMPO_EV_RECORDATORIO = "recordatorio"
        const val CAMPO_EV_DESCRIP = "descrip"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Crear Tabla Evento (Con llaves foráneas)
        val crearTablaEvento = ("CREATE TABLE $TABLA_EVENTO ("
                + "$CAMPO_EV_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$CAMPO_EV_CAT TEXT, "
                + "$CAMPO_EV_FECHA TEXT, "
                + "$CAMPO_EV_HORA TEXT, "
                + "$CAMPO_EV_UBI TEXT, "
                + "$CAMPO_EV_CONTACTO TEXT, "
                + "$CAMPO_EV_ESTATUS TEXT, "
                + "$CAMPO_EV_RECORDATORIO TEXT, "
                + "$CAMPO_EV_DESCRIP TEXT)")

        db?.execSQL(crearTablaEvento)

        inyectarDatosPrueba(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_EVENTO")
        onCreate(db)
    }

    private fun inyectarDatosPrueba(db: SQLiteDatabase?) {
        // Creamos una lista con eventos ficticios usando tus Enums
        val eventosPrueba = listOf(
            ContentValues().apply {
                put(CAMPO_EV_CAT, Categoria.EXAMEN.name)
                put(CAMPO_EV_FECHA, "2026-06-22")
                put(CAMPO_EV_HORA, "10:00")
                put(CAMPO_EV_UBI, "Salón de clases / Edificio 1")
                put(CAMPO_EV_CONTACTO, "Prof. Martínez")
                put(CAMPO_EV_ESTATUS, Status.PENDIENTE.name)
                put(CAMPO_EV_RECORDATORIO, "1 día antes")
                put(CAMPO_EV_DESCRIP, "Examen parcial teórico de Sistemas.")
            },
            ContentValues().apply {
                put(CAMPO_EV_CAT, Categoria.CITA.name)
                put(CAMPO_EV_FECHA, "2026-06-18")
                put(CAMPO_EV_HORA, "10:00")
                put(CAMPO_EV_UBI, "Televisión de la Sala")
                put(CAMPO_EV_CONTACTO, "Armando Dávila")
                put(CAMPO_EV_ESTATUS, Status.APLAZADO.name)
                put(CAMPO_EV_RECORDATORIO, "1 día antes")
                put(CAMPO_EV_DESCRIP, "Partido México vs Korea")
            },
            ContentValues().apply {
                put(CAMPO_EV_CAT, Categoria.JUNTA.name)
                put(CAMPO_EV_FECHA, "2026-06-17")
                put(CAMPO_EV_HORA, "16:00")
                put(CAMPO_EV_UBI, "Cubículo de proyectos")
                put(CAMPO_EV_CONTACTO, "Equipo de desarrollo")
                put(CAMPO_EV_ESTATUS, Status.PENDIENTE.name)
                put(CAMPO_EV_RECORDATORIO, "1 hora antes")
                put(CAMPO_EV_DESCRIP, "Reunión de seguimiento para revisión de la arquitectura móvil.")
            },
            ContentValues().apply {
                put(CAMPO_EV_CAT, Categoria.CITA.name)
                put(CAMPO_EV_FECHA, "2026-06-16")
                put(CAMPO_EV_HORA, "09:30")
                put(CAMPO_EV_UBI, "Laboratorio de Cómputo")
                put(CAMPO_EV_CONTACTO, "Asesor escolar")
                put(CAMPO_EV_ESTATUS, Status.REALIZADO.name)
                put(CAMPO_EV_RECORDATORIO, "Ninguno")
                put(CAMPO_EV_DESCRIP, "Firma y validación de los requerimientos iniciales.")
            }
        )

        // Insertamos cada uno en la BD
        for (evento in eventosPrueba) {
            db?.insert(TABLA_EVENTO, null, evento)
        }
    }

    // Método de inserción aceptando los Enums directamente de forma fuertemente tipada
    fun insertarEvento(
        cat: Categoria, fecha: String, hora: String, ubi: String,
        contacto: String, estatus: Status, recordatorio: String, descrip: String
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(CAMPO_EV_CAT, cat.name)         // Guarda el string del enum (ej: "CITA")
            put(CAMPO_EV_FECHA, fecha)
            put(CAMPO_EV_HORA, hora)
            put(CAMPO_EV_UBI, ubi)
            put(CAMPO_EV_CONTACTO, contacto)
            put(CAMPO_EV_ESTATUS, estatus.name) // Guarda el string del enum (ej: "PENDIENTE")
            put(CAMPO_EV_RECORDATORIO, recordatorio)
            put(CAMPO_EV_DESCRIP, descrip)
        }
        val resultado = db.insert(TABLA_EVENTO, null, values)
        db.close()
        return resultado
    }

    //Función para la vista de inicio
    fun obtenerEventosPorFechas(fechas: List<String>): List<ContentValues> {
        val listaEventos = mutableListOf<ContentValues>()
        val db = this.readableDatabase

        // Generamos los signos de interrogación para el query 'IN (?, ?, ...)'
        val args = fechas.toTypedArray()
        val signos = fechas.joinToString(",") { "?" }

        val query = "SELECT * FROM $TABLA_EVENTO WHERE $CAMPO_EV_FECHA IN ($signos) ORDER BY $CAMPO_EV_FECHA ASC, $CAMPO_EV_HORA ASC"

        val cursor = db.rawQuery(query, args)
        if (cursor.moveToFirst()) {
            do {
                val cv = ContentValues().apply {
                    put(CAMPO_EV_ID, cursor.getInt(cursor.getColumnIndexOrThrow(CAMPO_EV_ID)))
                    put(CAMPO_EV_CAT, cursor.getString(cursor.getColumnIndexOrThrow(CAMPO_EV_CAT)))
                    put(CAMPO_EV_FECHA, cursor.getString(cursor.getColumnIndexOrThrow(CAMPO_EV_FECHA)))
                    put(CAMPO_EV_HORA, cursor.getString(cursor.getColumnIndexOrThrow(CAMPO_EV_HORA)))
                    put(CAMPO_EV_UBI, cursor.getString(cursor.getColumnIndexOrThrow(CAMPO_EV_UBI)))
                    put(CAMPO_EV_CONTACTO, cursor.getString(cursor.getColumnIndexOrThrow(CAMPO_EV_CONTACTO)))
                    put(CAMPO_EV_ESTATUS, cursor.getString(cursor.getColumnIndexOrThrow(CAMPO_EV_ESTATUS)))
                    put(CAMPO_EV_RECORDATORIO, cursor.getString(cursor.getColumnIndexOrThrow(CAMPO_EV_RECORDATORIO)))
                    put(CAMPO_EV_DESCRIP, cursor.getString(cursor.getColumnIndexOrThrow(CAMPO_EV_DESCRIP)))
                }
                listaEventos.add(cv)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return listaEventos
    }
}