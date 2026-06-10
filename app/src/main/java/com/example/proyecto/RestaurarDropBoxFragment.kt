package com.example.proyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class RestaurarDropBoxFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_restaurar_drop_box, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnVincular = view.findViewById<Button>(R.id.btnVincularRestaurar)
        val btnDescargar = view.findViewById<Button>(R.id.btnDescargar)

        btnVincular.setOnClickListener {
            Toast.makeText(requireContext(), "Iniciando vinculación con Dropbox...", Toast.LENGTH_SHORT).show()
            // Simulación: Habilitar el botón de descargar después de "vincular"
            btnDescargar.isEnabled = true
        }

        btnDescargar.setOnClickListener {
            Toast.makeText(requireContext(), "Descargando y restaurando base de datos...", Toast.LENGTH_LONG).show()
        }
    }
}