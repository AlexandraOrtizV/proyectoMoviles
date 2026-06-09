package com.example.proyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class InicioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos la vista del fragmento
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Encontrar el TextView en la vista inflada
        val tvTituloHoy = view.findViewById<TextView>(R.id.tvTituloHoy)

        // 2. Obtener la fecha actual del sistema
        val fechaActual = LocalDate.now()

        // 3. Crear el formateador en español: "EEEE dd 'de' MMMM 'del' yyyy"
        // EEEE = Nombre del día completo (Martes)
        // dd = Día en dos dígitos (09)
        // MMMM = Nombre del mes completo (junio)
        // yyyy = Año (2026)
        val formateador = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM 'del' yyyy", Locale("es", "MX"))

        // 4. Formatear la fecha y capitalizar la primera letra (opcional, para que quede "Martes" y no "martes")
        val fechaFormateada = fechaActual.format(formateador).replaceFirstChar { it.uppercase() }

        // 5. Asignar el texto final combinándolo con la palabra "Hoy, "
        tvTituloHoy.text = "Hoy, $fechaFormateada"
    }
}