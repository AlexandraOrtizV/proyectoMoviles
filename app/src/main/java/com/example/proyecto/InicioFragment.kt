package com.example.proyecto

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class InicioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Referencias de la vista contenedora principal
        val tvTituloHoy = view.findViewById<TextView>(R.id.tvTituloHoy)
        val tvVacioHoy = view.findViewById<TextView>(R.id.tvVacioHoy)
        val tvVacioProximos = view.findViewById<TextView>(R.id.tvVacioProximos)

        val eventosHoyLayout = view.findViewById<LinearLayout>(R.id.eventosHoyLayout)
        val eventosProxLayout = view.findViewById<LinearLayout>(R.id.eventosProxLayout)

        // 2. Fechas del sistema (Formato de base de datos: YYYY-MM-DD)
        val hoy = LocalDate.now()
        val formatoBD = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val stringHoy = hoy.format(formatoBD)

        // Calcular los próximos 4 días
        val proximos4DiasStrings = mutableListOf<String>()
        for (i in 1..4) {
            proximos4DiasStrings.add(hoy.plusDays(i.toLong()).format(formatoBD))
        }

        // Setear título de Hoy
        val formateadorEspanol =
            DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM 'del' yyyy", Locale("es", "MX"))
        tvTituloHoy.text =
            "Hoy, " + hoy.format(formateadorEspanol).replaceFirstChar { it.uppercase() }

        // 3. Consultar Base de Datos
        val helper = ConexionSQLiteHelper(requireContext())

        // Todas las fechas que nos importan juntas (Hoy + los 4 días siguientes)
        val todasLasFechas = listOf(stringHoy) + proximos4DiasStrings
        val eventosBD = helper.obtenerEventosPorFechas(todasLasFechas)

        // Separar eventos locales de la lista devuelta
        val listaHoy =
            eventosBD.filter { it.getAsString(ConexionSQLiteHelper.CAMPO_EV_FECHA) == stringHoy }
        val listaProximos =
            eventosBD.filter { it.getAsString(ConexionSQLiteHelper.CAMPO_EV_FECHA) != stringHoy }

        // 4. Pintar Eventos de Hoy
        if (listaHoy.isEmpty()) {
            tvVacioHoy.visibility = View.VISIBLE
        } else {
            tvVacioHoy.visibility = View.GONE
            val inflaterLayout = LayoutInflater.from(requireContext())

            for (evento in listaHoy) {
                val cardView =
                    inflaterLayout.inflate(R.layout.item_evento_hoy, eventosHoyLayout, false)

                // Mapear campos normales...
                cardView.findViewById<TextView>(R.id.tvHora).text =
                    "Hora: ${evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_HORA)}"
                cardView.findViewById<TextView>(R.id.tvResponsable).text =
                    evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_CONTACTO)
                cardView.findViewById<TextView>(R.id.tvDireccion).text =
                    evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_UBI)
                cardView.findViewById<TextView>(R.id.tvDescripcion).text =
                    evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_DESCRIP)

                // Obtener Enums
                val catEnum =
                    Categoria.valueOf(evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_CAT))
                val estEnum =
                    Status.valueOf(evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_ESTATUS))

                cardView.findViewById<TextView>(R.id.tvCategoria).text =
                    catEnum.getTexto(requireContext())

                // 🔥 CAMBIO DINÁMICO DE COLOR PARA EL ESTATUS 🔥
                val tvEstatusCard = cardView.findViewById<TextView>(R.id.tvEstatus)
                tvEstatusCard.text = estEnum.getTexto(requireContext())

                val colorEstatus = when (estEnum) {
                    Status.PENDIENTE -> androidx.core.content.ContextCompat.getColor(
                        requireContext(),
                        R.color.vino
                    )

                    Status.REALIZADO -> androidx.core.content.ContextCompat.getColor(
                        requireContext(),
                        R.color.verde_azulado
                    )

                    Status.APLAZADO -> androidx.core.content.ContextCompat.getColor(
                        requireContext(),
                        R.color.verde_amarillo
                    )
                }
                tvEstatusCard.setTextColor(colorEstatus)

                eventosHoyLayout.addView(cardView)
            }
        }

// 5. Pintar Eventos Próximos
        if (listaProximos.isEmpty()) {
            tvVacioProximos.visibility = View.VISIBLE
        } else {
            tvVacioProximos.visibility = View.GONE
            val inflaterLayout = LayoutInflater.from(requireContext())

            for (evento in listaProximos) {
                val cardView =
                    inflaterLayout.inflate(R.layout.item_evento_proximo, eventosProxLayout, false)

                // Mapear campos de fecha y normales...
                val fechaOriginal = LocalDate.parse(
                    evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_FECHA),
                    formatoBD
                )
                val formatoBonitoCard =
                    DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM", Locale("es", "MX"))
                cardView.findViewById<TextView>(R.id.tvFechaEvento).text =
                    fechaOriginal.format(formatoBonitoCard).replaceFirstChar { it.uppercase() }

                cardView.findViewById<TextView>(R.id.tvHora).text =
                    "Hora: ${evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_HORA)}"
                cardView.findViewById<TextView>(R.id.tvResponsable).text =
                    evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_CONTACTO)
                cardView.findViewById<TextView>(R.id.tvDireccion).text =
                    evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_UBI)
                cardView.findViewById<TextView>(R.id.tvDescripcion).text =
                    evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_DESCRIP)

                val catEnum =
                    Categoria.valueOf(evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_CAT))
                val estEnum =
                    Status.valueOf(evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_ESTATUS))

                cardView.findViewById<TextView>(R.id.tvCategoria).text =
                    catEnum.getTexto(requireContext())

                // 🔥 CAMBIO DINÁMICO DE COLOR PARA EL ESTATUS 🔥
                val tvEstatusCard = cardView.findViewById<TextView>(R.id.tvEstatus)
                tvEstatusCard.text = estEnum.getTexto(requireContext())

                val colorEstatus = when (estEnum) {
                    Status.PENDIENTE -> androidx.core.content.ContextCompat.getColor(
                        requireContext(),
                        R.color.vino
                    )

                    Status.REALIZADO -> androidx.core.content.ContextCompat.getColor(
                        requireContext(),
                        R.color.verde_azulado
                    )

                    Status.APLAZADO -> androidx.core.content.ContextCompat.getColor(
                        requireContext(),
                        R.color.verde_amarillo
                    )
                }
                tvEstatusCard.setTextColor(colorEstatus)

                eventosProxLayout.addView(cardView)
            }
        }
    }
}