package com.example.proyecto

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.json.JSONArray
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.widget.EditText
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Locale

class CrearEventoFragment : Fragment() {

    // Lanzador para abrir la agenda de contactos nativa
    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        uri?.let { leerNombreContacto(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crear_evento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ⚠️ MUY IMPORTANTE PARA OSMDROID: Configurar el User Agent antes de usar el mapa
        Configuration.getInstance().userAgentValue = requireContext().packageName

        // ... (Tu vinculación de vistas se queda igual) ...
        val etUbicacion = view.findViewById<TextInputEditText>(R.id.etUbicacion)

        // NUEVO: Mostrar el diálogo del mapa al hacer click
        etUbicacion.setOnClickListener {
            mostrarDialogoMapa(etUbicacion)
        }

        // 1. Vinculación de todos los componentes visuales del XML
        val chipGroupCategoria = view.findViewById<ChipGroup>(R.id.chipGroupCategoria)
        val etDescripcion = view.findViewById<TextInputEditText>(R.id.etDescripcion)
        val etFecha = view.findViewById<TextInputEditText>(R.id.etFecha)
        val etHora = view.findViewById<TextInputEditText>(R.id.etHora)
        val etContacto = view.findViewById<TextInputEditText>(R.id.etContacto)
        val spinnerStatus = view.findViewById<Spinner>(R.id.spinnerStatus)
        val spinnerRecordatorio = view.findViewById<Spinner>(R.id.spinnerRecordatorio)
        val btnGuardar = view.findViewById<MaterialButton>(R.id.btnGuardar)

        // 2. Lógica para cambiar de color los chips visualmente (Confirmación para el usuario)
        chipGroupCategoria.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                if (chip.id == checkedId) {
                    // Pintamos de tu color "botones" el que acaba de seleccionar
                    chip.setChipBackgroundColorResource(R.color.botones)
                } else {
                    // Los demás regresan a color "navy"
                    chip.setChipBackgroundColorResource(R.color.navy)
                }
            }
        }

        // 3. Evento para abrir la agenda al presionar el campo de Contacto
        etContacto.setOnClickListener {
            pickContactLauncher.launch(null)
        }

        // 4. Evento para abrir el selector de fecha (Calendario)
        etFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val fechaFormateada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                etFecha.setText(fechaFormateada)
            }, anio, mes, dia)
            datePickerDialog.show()
        }

        // 5. Evento para abrir el selector de hora (Reloj)
        etHora.setOnClickListener {
            val calendario = Calendar.getInstance()
            val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
            val minutoActual = calendario.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                val horaFormateada = String.format("%02d:%02d", hourOfDay, minute)
                etHora.setText(horaFormateada)
            }, horaActual, minutoActual, true)
            timePickerDialog.show()
        }

        // 6. Gestión del botón Guardar para almacenar el evento en SQLite
        btnGuardar.setOnClickListener {

            // A. Verificar qué Chip está seleccionado mediante su ID directo
            val idChipSeleccionado = chipGroupCategoria.checkedChipId
            if (idChipSeleccionado == View.NO_ID) {
                Toast.makeText(requireContext(), "Por favor, selecciona una categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // B. Recopilar la información ingresada por el usuario
            val descripcion = etDescripcion.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val hora = etHora.text.toString().trim()
            val ubicacion = etUbicacion.text.toString().trim()
            val contacto = etContacto.text.toString().trim()
            val statusText = spinnerStatus.selectedItem.toString()
            val recordatorio = spinnerRecordatorio.selectedItem.toString()

            // C. Validar la existencia de campos obligatorios
            if (descripcion.isEmpty() || fecha.isEmpty() || hora.isEmpty() || contacto.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, llena los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // D. Operación de inserción en la Base de Datos
            try {
                // Mapeo inequívoco basado en la referencia ID del recurso XML
                val categoriaEnum = when (idChipSeleccionado) {
                    R.id.chipCita -> Categoria.CITA
                    R.id.chipJunta -> Categoria.JUNTA
                    R.id.chipProyecto -> Categoria.PROYECTO
                    R.id.chipExamen -> Categoria.EXAMEN
                    R.id.chipOtro -> Categoria.OTRO
                    else -> Categoria.OTRO
                }

                // Mapeo del estatus según el Spinner
                val statusEnum = when (statusText.uppercase()) {
                    "PENDIENTE" -> Status.PENDIENTE
                    "APLAZADO" -> Status.APLAZADO
                    "REALIZADO" -> Status.REALIZADO
                    else -> Status.PENDIENTE
                }

                // Llamada a tu clase Helper para registrar el evento
                val conexionHelper = ConexionSQLiteHelper(requireContext())
                val resultado = conexionHelper.insertarEvento(
                    cat = categoriaEnum,
                    fecha = fecha,
                    hora = hora,
                    ubi = ubicacion,
                    contacto = contacto,
                    estatus = statusEnum,
                    recordatorio = recordatorio,
                    descrip = descripcion
                )

                // Confirmación al usuario
                if (resultado != -1L) {
                    // AQUÍ ESTÁ LA INTEGRACIÓN: Programamos la alarma justo después de guardar en BD
                    programarNotificacion(fecha, hora, recordatorio, descripcion)

                    Toast.makeText(requireContext(), "¡Evento guardado exitosamente!", Toast.LENGTH_SHORT).show()
                    // Limpieza integral de la interfaz
                    limpiarCampos(etDescripcion, etFecha, etHora, etUbicacion, etContacto, chipGroup = chipGroupCategoria)
                } else {
                    Toast.makeText(requireContext(), "Error al guardar el evento en la BD", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error en la BD: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Función encargada de restaurar los valores iniciales de los campos tras guardar
    private fun limpiarCampos(vararg campos: TextInputEditText, chipGroup: ChipGroup) {
        for (campo in campos) {
            campo.setText("")
        }
        chipGroup.clearCheck()
    }

    private fun mostrarDialogoMapa(etUbicacionDestino: TextInputEditText) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_mapa, null)
        val etBuscarMapa = dialogView.findViewById<EditText>(R.id.etBuscarMapa)
        val btnBuscarMapa = dialogView.findViewById<Button>(R.id.btnBuscarMapa)
        val mapView = dialogView.findViewById<MapView>(R.id.mapView)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmarUbicacion)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Configuración básica del mapa
        mapView.setMultiTouchControls(true)
        val mapController = mapView.controller
        mapController.setZoom(15.0)

        // Coordenadas iniciales por defecto (CDMX como ejemplo)
        val startPoint = GeoPoint(19.4326, -99.1332)
        mapController.setCenter(startPoint)

        var ubicacionSeleccionada: String? = null

        btnBuscarMapa.setOnClickListener {
            val query = etBuscarMapa.text.toString()
            if (query.isNotEmpty()) {
                Toast.makeText(requireContext(), "Buscando...", Toast.LENGTH_SHORT).show()
                buscarEnNominatim(query, mapView) { nombreLugar ->
                    ubicacionSeleccionada = nombreLugar
                    btnConfirmar.isEnabled = true
                }
            }
        }

        btnConfirmar.setOnClickListener {
            // Pasamos el texto formateado al campo original
            etUbicacionDestino.setText(ubicacionSeleccionada)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun buscarEnNominatim(query: String, mapView: MapView, onResult: (String) -> Unit) {
        // Usamos Corrutinas para no congelar la pantalla durante la petición de red
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Endpoint de la API gratuita de OpenStreetMap (Nominatim)
                val urlEncoded = URLEncoder.encode(query, "UTF-8")
                val url = URL("https://nominatim.openstreetmap.org/search?q=$urlEncoded&format=json&limit=1")
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", requireContext().packageName)

                val response = connection.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)

                if (jsonArray.length() > 0) {
                    val result = jsonArray.getJSONObject(0)
                    val lat = result.getDouble("lat")
                    val lon = result.getDouble("lon")
                    val displayName = result.getString("display_name")

                    // Volvemos al hilo principal para actualizar la interfaz
                    withContext(Dispatchers.Main) {
                        val geoPoint = GeoPoint(lat, lon)
                        mapView.controller.animateTo(geoPoint)
                        mapView.controller.setZoom(18.0)

                        // Limpiar pines anteriores y colocar uno nuevo
                        mapView.overlays.clear()
                        val marker = Marker(mapView)
                        marker.position = geoPoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = displayName
                        mapView.overlays.add(marker)
                        mapView.invalidate() // Refrescar el mapa

                        onResult(displayName)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No se encontró el lugar", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Extracción de metadatos de contacto a partir del URI provisto por el proveedor de contenido
    @SuppressLint("Range")
    private fun leerNombreContacto(contactoUri: Uri) {
        val cursor = requireContext().contentResolver.query(contactoUri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val nombre = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            val etContacto = requireView().findViewById<TextInputEditText>(R.id.etContacto)
            etContacto.setText(nombre)
            cursor.close()
        }
    }

    // NUEVA FUNCIÓN AÑADIDA AQUÍ ABAJO PARA LAS NOTIFICACIONES
    private fun programarNotificacion(fecha: String, hora: String, tipo: String, desc: String) {
        if (tipo == "Sin recordatorio") return

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val cal = Calendar.getInstance()
        try {
            cal.time = sdf.parse("$fecha $hora") ?: return
        } catch (e: Exception) {
            return
        }

        when (tipo) {
            "10 minutos antes" -> cal.add(Calendar.MINUTE, -10)
            "1 día antes" -> cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("titulo", "Recordatorio")
            putExtra("desc", desc)
        }

        val pi = PendingIntent.getBroadcast(
            requireContext(),
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            } else {
                // Si el permiso falla, usamos setWindow que no crashea la app
                am.setWindow(AlarmManager.RTC_WAKEUP, cal.timeInMillis, 1000, pi)
            }
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }
}