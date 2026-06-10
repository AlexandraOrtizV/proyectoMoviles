package com.example.proyecto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class RespandoDropboxFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_respando_dropbox, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnVincular = view.findViewById<Button>(R.id.btnVincular)
        val btnSubir = view.findViewById<Button>(R.id.btnSubir)

        btnVincular.setOnClickListener {
            Toast.makeText(requireContext(), "Iniciando vinculación con Dropbox...", Toast.LENGTH_SHORT).show()
            // Simulación: Habilitar el botón de subir después de "vincular"
            btnSubir.isEnabled = true
        }

        btnSubir.setOnClickListener {
            Toast.makeText(requireContext(), "Subiendo respaldo a Dropbox...", Toast.LENGTH_SHORT).show()
        }
    }
}