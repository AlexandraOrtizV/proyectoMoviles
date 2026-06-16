package com.example.proyecto

import android.content.Context

enum class Categoria(val stringResId: Int) {
    CITA(R.string.cita),
    JUNTA(R.string.junta),
    PROYECTO(R.string.proyecto),
    EXAMEN(R.string.examen),
    OTRO(R.string.otro);

    // Método para obtener el texto real en el idioma del dispositivo
    fun getTexto(context: Context): String {
        return context.getString(this.stringResId)
    }
}

enum class Status(val stringResId: Int) {
    PENDIENTE(R.string.pendiente),
    REALIZADO(R.string.realizado),
    APLAZADO(R.string.aplazado);

    fun getTexto(context: Context): String {
        return context.getString(this.stringResId)
    }
}