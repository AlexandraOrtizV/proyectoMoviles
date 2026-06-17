package com.example.proyecto

import android.content.ContentValues
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventoAdapter(
    private var listaEventos: List<ContentValues>,
    // Esta variable nos permitirá detectar cuando el usuario toque una fila de la tabla
    private val onRowClick: (ContentValues) -> Unit
) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    // 1. Enlazamos los componentes visuales de nuestro item_evento.xml
    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvItemFecha)
        val tvHora: TextView = view.findViewById(R.id.tvItemHora)
        val tvCategoria: TextView = view.findViewById(R.id.tvItemCategoria)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
        // NUEVO: Agregamos la referencia a la descripción
        val tvDesc: TextView = view.findViewById(R.id.tvItemDescripcion)
    }

    // 2. Le decimos qué diseño (XML) usar para cada fila
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    // 3. Acomodamos los datos de la base de datos en los TextViews
    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val eventoActual = listaEventos[position]

        holder.tvFecha.text = eventoActual.getAsString(ConexionSQLiteHelper.CAMPO_EV_FECHA)
        holder.tvHora.text = eventoActual.getAsString(ConexionSQLiteHelper.CAMPO_EV_HORA)
        holder.tvCategoria.text = eventoActual.getAsString(ConexionSQLiteHelper.CAMPO_EV_CAT)
        holder.tvStatus.text = eventoActual.getAsString(ConexionSQLiteHelper.CAMPO_EV_ESTATUS)

        // NUEVO: Extraemos la descripción de la BD y la ponemos en el TextView
        holder.tvDesc.text = eventoActual.getAsString(ConexionSQLiteHelper.CAMPO_EV_DESCRIP)

        // Si el usuario toca toda la fila, mandamos el evento completo para poder editarlo/borrarlo después
        holder.itemView.setOnClickListener {
            onRowClick(eventoActual)
        }
    }

    // 4. Le decimos cuántas filas hay en total
    override fun getItemCount(): Int {
        return listaEventos.size
    }

    // 5. Función que usaremos para refrescar la tabla cuando hagamos una nueva consulta
    fun actualizarLista(nuevaLista: List<ContentValues>) {
        this.listaEventos = nuevaLista
        notifyDataSetChanged()
    }
}