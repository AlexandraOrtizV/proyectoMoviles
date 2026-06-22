package com.example.proyecto

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout = findViewById<DrawerLayout>(R.id.main)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            cambiarFragmento(InicioFragment())
        }

        // Solicitar permisos
        solicitarPermisos()

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> cambiarFragmento(InicioFragment())
                R.id.nav_calendario -> cambiarFragmento(CalendarioFragment())
                R.id.nav_consultar -> cambiarFragmento(ConsultarEventoFragment())
                R.id.nav_crear -> cambiarFragmento(CrearEventoFragment())
                R.id.nav_respaldo -> cambiarFragmento(RespandoDropboxFragment())
                R.id.nav_restaurar -> cambiarFragmento(RestaurarDropBoxFragment())
                R.id.nav_acerca_de -> cambiarFragmento(AcercaDeFragment())
                R.id.nav_salir -> finishAffinity()
            }
            drawerLayout.closeDrawers()
            true
        }

        // AQUÍ ESTABA EL ERROR: Ya agregamos las llaves con el 'true' al final de cada bloque
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    cambiarFragmento(InicioFragment())
                    true
                }
                R.id.nav_consultar -> {
                    cambiarFragmento(ConsultarEventoFragment())
                    true
                }
                R.id.nav_salir -> {
                    finishAffinity()
                    true
                }
                else -> false
            }
        }
    }

    private fun cambiarFragmento(fragmento: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragmento)
            .commit()
    }

    private fun solicitarPermisos() {
        val permisos = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permisos.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
        }

        if (permisos.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisos.toTypedArray(), 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos concedidos correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Aviso: Sin permisos, los recordatorios no funcionarán", Toast.LENGTH_LONG).show()
            }
        }
    }
}