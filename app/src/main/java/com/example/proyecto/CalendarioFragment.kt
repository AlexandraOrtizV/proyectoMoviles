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
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class CalendarioFragment : Fragment() {

    private lateinit var helper: ConexionSQLiteHelper
    private lateinit var containerEventos: LinearLayout
    private lateinit var tvNoEventos: TextView
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var calendarView: CalendarView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendario, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Enlaces normales
        calendarView = view.findViewById(R.id.calendarView)
        tvFechaSeleccionada = view.findViewById(R.id.tvFechaSeleccionada)
        tvNoEventos = view.findViewById(R.id.tvNoEventos)
        containerEventos = view.findViewById(R.id.containerEventosCalendario)

        helper = ConexionSQLiteHelper(requireContext())

        val formatoBD = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatoBonito = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM", Locale("es", "MX"))

        // 2. 🔥 PINTAR LOS DISTINTIVOS EN LOS DÍAS CON EVENTOS 🔥
        marcarDiasConEventos()

        // 3. Cargar eventos de hoy por defecto
        val hoy = LocalDate.now()
        actualizarPantallaConEventos(hoy.format(formatoBD), hoy.format(formatoBonito))

        // 4. Configurar el Listener de click usando la interfaz de la librería
        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val cal = eventDay.calendar

                // Convertir el Calendar seleccionado a LocalDate de Java 8
                val fechaSeleccionada = LocalDate.of(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1, // Calendar cuenta meses de 0 a 11
                    cal.get(Calendar.DAY_OF_MONTH)
                )

                val stringFechaBD = fechaSeleccionada.format(formatoBD)
                val stringFechaBonita = fechaSeleccionada.format(formatoBonito)

                actualizarPantallaConEventos(stringFechaBD, stringFechaBonita)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun marcarDiasConEventos() {
        val fechasConEventos = helper.obtenerTodasLasFechasConEventos() // Devuelve strings "yyyy-MM-dd"
        val listaEventosCalendario = mutableListOf<EventDay>()

        val formatoBD = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        for (fechaStr in fechasConEventos) {
            try {
                val localDate = LocalDate.parse(fechaStr, formatoBD)

                // Creamos un clon de Calendar para asignarle la fecha precisa del evento
                val calendarInstance = Calendar.getInstance().apply {
                    set(localDate.year, localDate.monthValue - 1, localDate.dayOfMonth)
                }

                // Vinculamos esa fecha exacta con nuestro drawable de fondo translúcido
                val eventDay = EventDay(calendarInstance, R.drawable.background_dia_evento)
                listaEventosCalendario.add(eventDay)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Le inyectamos la lista de marcas de golpe al componente visual
        calendarView.setEvents(listaEventosCalendario)
    }

    private fun actualizarPantallaConEventos(fechaBD: String, fechaBonita: String) {
        containerEventos.removeAllViews()
        val tituloFinal = fechaBonita.replaceFirstChar { it.uppercase() }
        tvFechaSeleccionada.text = "Eventos para el $tituloFinal"

        val eventos = helper.obtenerEventosPorFechas(listOf(fechaBD))

        if (eventos.isEmpty()) {
            tvNoEventos.visibility = View.VISIBLE
        } else {
            tvNoEventos.visibility = View.GONE
            val inflaterLayout = LayoutInflater.from(requireContext())

            for (evento in eventos) {
                val cardView = inflaterLayout.inflate(R.layout.item_evento_hoy, containerEventos, false)

                cardView.findViewById<TextView>(R.id.tvHora).text = "Hora: ${evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_HORA)}"
                cardView.findViewById<TextView>(R.id.tvResponsable).text = evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_CONTACTO)
                cardView.findViewById<TextView>(R.id.tvDireccion).text = evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_UBI)
                cardView.findViewById<TextView>(R.id.tvDescripcion).text = evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_DESCRIP)

                val catEnum = Categoria.valueOf(evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_CAT))
                val estEnum = Status.valueOf(evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_ESTATUS))

                cardView.findViewById<TextView>(R.id.tvCategoria).text = catEnum.getTexto(requireContext())

                val tvEstatusCard = cardView.findViewById<TextView>(R.id.tvEstatus)
                tvEstatusCard.text = estEnum.getTexto(requireContext())

                val colorId = when (estEnum) {
                    Status.PENDIENTE -> R.color.vino
                    Status.REALIZADO -> R.color.verde_azulado
                    Status.APLAZADO -> R.color.verde_amarillo
                }
                tvEstatusCard.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), colorId))

                containerEventos.addView(cardView)
            }
        }
    }
}