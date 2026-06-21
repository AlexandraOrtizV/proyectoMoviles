package com.example.proyecto

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ConexionSQLiteHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 5
        private const val DATABASE_NAME = "Eventos.db"

        // TABLA EVENTO
        const val TABLA_EVENTO = "evento"
        const val CAMPO_EV_ID = "id"
        const val CAMPO_EV_CAT = "cat"
        const val CAMPO_EV_FECHA = "fecha"
        const val CAMPO_EV_HORA = "hora"
        const val CAMPO_EV_UBI = "ubi"
        const val CAMPO_EV_CONTACTO = "contacto"
        const val CAMPO_EV_ESTATUS = "estatus"
        const val CAMPO_EV_RECORDATORIO = "recordatorio"
        const val CAMPO_EV_DESCRIP = "descrip"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Crear Tabla Evento
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

        // Inyectar datos iniciales de prueba
        inyectarDatosPrueba(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLA_EVENTO")
        onCreate(db)
    }

    private fun inyectarDatosPrueba(db: SQLiteDatabase?) {
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

        for (evento in eventosPrueba) {
            db?.insert(TABLA_EVENTO, null, evento)
        }
    }

    // Método para guardar un evento nuevo
    fun insertarEvento(
        cat: Categoria, fecha: String, hora: String, ubi: String,
        contacto: String, estatus: Status, recordatorio: String, descrip: String
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(CAMPO_EV_CAT, cat.name)
            put(CAMPO_EV_FECHA, fecha)
            put(CAMPO_EV_HORA, hora)
            put(CAMPO_EV_UBI, ubi)
            put(CAMPO_EV_CONTACTO, contacto)
            put(CAMPO_EV_ESTATUS, estatus.name)
            put(CAMPO_EV_RECORDATORIO, recordatorio)
            put(CAMPO_EV_DESCRIP, descrip)
        }
        val resultado = db.insert(TABLA_EVENTO, null, values)
        db.close()
        return resultado
    }

    // Función para actualizar un evento existente (desde el Modal)
    fun actualizarEvento(id: Int, estatus: Status, contacto: String, ubi: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(CAMPO_EV_ESTATUS, estatus.name)
            put(CAMPO_EV_CONTACTO, contacto)
            put(CAMPO_EV_UBI, ubi)
        }
        val resultado = db.update(TABLA_EVENTO, values, "$CAMPO_EV_ID = ?", arrayOf(id.toString()))
        db.close()
        return resultado > 0
    }

    // Función para eliminar un evento (desde el Modal)
    fun eliminarEvento(id: Int): Boolean {
        val db = this.writableDatabase
        val resultado = db.delete(TABLA_EVENTO, "$CAMPO_EV_ID = ?", arrayOf(id.toString()))
        db.close()
        return resultado > 0
    }

    // Función para la vista de inicio (Calendario principal)
    fun obtenerEventosPorFechas(fechas: List<String>): List<ContentValues> {
        val listaEventos = mutableListOf<ContentValues>()
        val db = this.readableDatabase

        if (fechas.isEmpty()) return listaEventos

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

    // Función para el buscador inteligente de la pantalla de Consultas
    fun consultarEventosAvanzado(
        tipoConsulta: String,
        fechaIni: String,
        fechaFin: String,
        categoria: String?,
        busqueda: String
    ): List<ContentValues> {
        val lista = mutableListOf<ContentValues>()
        val db = this.readableDatabase

        var query = "SELECT * FROM $TABLA_EVENTO WHERE 1=1 "
        val args = mutableListOf<String>()

        when (tipoConsulta) {
            "RANGO" -> {
                if (fechaIni.isNotEmpty() && fechaFin.isNotEmpty()) {
                    query += "AND $CAMPO_EV_FECHA BETWEEN ? AND ? "
                    args.add(fechaIni)
                    args.add(fechaFin)
                }
            }
            "DIA" -> {
                if (fechaIni.isNotEmpty()) {
                    query += "AND $CAMPO_EV_FECHA = ? "
                    args.add(fechaIni)
                }
            }
            "MES" -> {
                if (fechaIni.isNotEmpty() && fechaIni.length >= 7) {
                    val mesAnio = fechaIni.substring(0, 7)
                    query += "AND $CAMPO_EV_FECHA LIKE ? "
                    args.add("$mesAnio%")
                }
            }
            "ANIO" -> {
                if (fechaIni.isNotEmpty() && fechaIni.length >= 4) {
                    val anio = fechaIni.substring(0, 4)
                    query += "AND $CAMPO_EV_FECHA LIKE ? "
                    args.add("$anio%")
                }
            }
        }

        if (!categoria.isNullOrEmpty()) {
            query += "AND $CAMPO_EV_CAT = ? "
            args.add(categoria.uppercase())
        }

        if (busqueda.isNotEmpty()) {
            query += "AND $CAMPO_EV_DESCRIP LIKE ? "
            args.add("%$busqueda%")
        }

        query += " ORDER BY $CAMPO_EV_FECHA ASC, $CAMPO_EV_HORA ASC"

        val cursor = db.rawQuery(query, args.toTypedArray())
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
                lista.add(cv)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }


    //Funcion para calendario
    fun obtenerTodasLasFechasConEventos(): List<String> {
        val fechas = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT DISTINCT $CAMPO_EV_FECHA FROM $TABLA_EVENTO"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                fechas.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return fechas
    }
}