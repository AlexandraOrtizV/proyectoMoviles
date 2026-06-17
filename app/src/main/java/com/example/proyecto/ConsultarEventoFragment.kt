package com.example.proyecto

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class ConsultarEventoFragment : Fragment() {

    private lateinit var adapter: EventoAdapter
    private lateinit var dbHelper: ConexionSQLiteHelper
    private lateinit var btnConsultarFiltro: MaterialButton // Lo hacemos global para simular clics

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_consultar_evento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = ConexionSQLiteHelper(requireContext())

        val chipGroupTipo = view.findViewById<ChipGroup>(R.id.chipGroupTipoConsulta)
        val chipGroupCat = view.findViewById<ChipGroup>(R.id.chipGroupCategoriaConsulta)
        val etFechaInicial = view.findViewById<TextInputEditText>(R.id.etFechaInicial)
        val etFechaFinal = view.findViewById<TextInputEditText>(R.id.etFechaFinal)
        val etBusquedaRapida = view.findViewById<TextInputEditText>(R.id.etBusquedaRapida)
        btnConsultarFiltro = view.findViewById(R.id.btnConsultarFiltro)
        val rvEventos = view.findViewById<RecyclerView>(R.id.rvEventos)

        rvEventos.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventoAdapter(listOf()) { eventoTocado ->
            mostrarOpcionesDeEvento(eventoTocado)
        }
        rvEventos.adapter = adapter

        chipGroupTipo.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                chip.setChipBackgroundColorResource(if (chip.id == checkedId) R.color.botones else R.color.navy)
            }
        }

        chipGroupCat.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                chip.setChipBackgroundColorResource(if (chip.id == checkedId) R.color.botones else R.color.navy)
            }
        }

        etFechaInicial.setOnClickListener { mostrarCalendario(etFechaInicial) }
        etFechaFinal.setOnClickListener { mostrarCalendario(etFechaFinal) }

        btnConsultarFiltro.setOnClickListener {
            val idTipoSeleccionado = chipGroupTipo.checkedChipId
            val tipoConsultaStr = when (idTipoSeleccionado) {
                R.id.chipPorDia -> "DIA"
                R.id.chipPorMes -> "MES"
                R.id.chipPorAnio -> "ANIO"
                else -> "RANGO"
            }

            val idCatSeleccionada = chipGroupCat.checkedChipId
            val categoriaSeleccionadaStr = when (idCatSeleccionada) {
                R.id.chipCatCita -> "CITA"
                R.id.chipCatJunta -> "JUNTA"
                R.id.chipCatProyecto -> "PROYECTO"
                R.id.chipCatExamen -> "EXAMEN"
                R.id.chipCatOtro -> "OTRO"
                else -> null
            }

            val resultados = dbHelper.consultarEventosAvanzado(
                tipoConsulta = tipoConsultaStr,
                fechaIni = etFechaInicial.text.toString().trim(),
                fechaFin = etFechaFinal.text.toString().trim(),
                categoria = categoriaSeleccionadaStr,
                busqueda = etBusquedaRapida.text.toString().trim()
            )

            adapter.actualizarLista(resultados)
        }
    }

    private fun mostrarCalendario(editText: TextInputEditText) {
        val calendario = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            editText.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth))
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
    }

    // AQUI SE ABRE EL MODAL MÁGICO
    private fun mostrarOpcionesDeEvento(evento: ContentValues) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_modificar_evento)

        // Hacer que el fondo sea transparente para que se vean los bordes redondeados
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 1. Extraer los datos del evento seleccionado
        val idEvento = evento.getAsInteger(ConexionSQLiteHelper.CAMPO_EV_ID)
        val estatusActual = evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_ESTATUS)
        val contactoActual = evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_CONTACTO)
        val ubiActual = evento.getAsString(ConexionSQLiteHelper.CAMPO_EV_UBI)

        // 2. Vincular los elementos del Modal
        val spinnerStatus = dialog.findViewById<Spinner>(R.id.spinnerDialogStatus)
        val etContacto = dialog.findViewById<TextInputEditText>(R.id.etDialogContacto)
        val etUbicacion = dialog.findViewById<TextInputEditText>(R.id.etDialogUbicacion)
        val btnActualizar = dialog.findViewById<MaterialButton>(R.id.btnDialogActualizar)
        val btnEliminar = dialog.findViewById<MaterialButton>(R.id.btnDialogEliminar)

        // 3. Pre-llenar los datos actuales en el Modal
        etContacto.setText(contactoActual)
        etUbicacion.setText(ubiActual)

        // Seleccionar automáticamente el estatus correcto en el Spinner
        val opcionesSpinner = resources.getStringArray(R.array.opciones_status)
        val posicionEstatus = opcionesSpinner.indexOfFirst { it.equals(estatusActual, ignoreCase = true) }
        if (posicionEstatus >= 0) {
            spinnerStatus.setSelection(posicionEstatus)
        }

        // 4. Lógica de "Actualizar"
        btnActualizar.setOnClickListener {
            val nuevoEstatusText = spinnerStatus.selectedItem.toString()
            val nuevoContacto = etContacto.text.toString().trim()
            val nuevaUbi = etUbicacion.text.toString().trim()

            val estatusEnum = when (nuevoEstatusText.uppercase()) {
                "PENDIENTE" -> Status.PENDIENTE
                "APLAZADO" -> Status.APLAZADO
                "REALIZADO" -> Status.REALIZADO
                else -> Status.PENDIENTE
            }

            if (dbHelper.actualizarEvento(idEvento, estatusEnum, nuevoContacto, nuevaUbi)) {
                Toast.makeText(requireContext(), "Evento actualizado correctamente", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                btnConsultarFiltro.performClick() // Simulamos un clic en consultar para refrescar la tabla
            } else {
                Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Lógica de "Eliminar"
        btnEliminar.setOnClickListener {
            if (dbHelper.eliminarEvento(idEvento)) {
                Toast.makeText(requireContext(), "Evento eliminado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                btnConsultarFiltro.performClick() // Refresca la tabla automáticamente
            } else {
                Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}