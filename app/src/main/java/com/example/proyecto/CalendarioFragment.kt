package com.example.proyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Locale

class CalendarioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val tvFechaSeleccionada = view.findViewById<TextView>(R.id.tvFechaSeleccionada)
        val tvNoEventos = view.findViewById<TextView>(R.id.tvNoEventos)

        // Configurar el listener para cambios de fecha
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // El mes en CalendarView va de 0 a 11
            val mesActualizado = month + 1
            val fecha = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, mesActualizado, year)
            
            tvFechaSeleccionada.text = getString(R.string.eventos_para_hoy, fecha)
            
            // Simulación: mostrar siempre que no hay eventos por ahora
            tvNoEventos.visibility = View.VISIBLE
        }
    }
}