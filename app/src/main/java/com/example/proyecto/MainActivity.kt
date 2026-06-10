package com.example.proyecto

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView

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
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Cargar el fragmento inicial por defecto (InicioFragment) si es la primera vez que se crea la actividad
        if (savedInstanceState == null) {
            cambiarFragmento(InicioFragment())
        }

        // Solicitar permisos de notificación para Android 13+
        solicitarPermisosNotificacion()

        // 5. Configurar menú hamburguesa (Navigation Drawer)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> cambiarFragmento(InicioFragment())
                R.id.nav_calendario -> cambiarFragmento(CalendarioFragment())
                R.id.nav_consultar -> cambiarFragmento(ConsultarEventoFragment())
                R.id.nav_crear -> cambiarFragmento(CrearEventoFragment())
                R.id.nav_respaldo -> cambiarFragmento(RespandoDropboxFragment())
                R.id.nav_restaurar -> cambiarFragmento(RestaurarDropBoxFragment())
                R.id.nav_acerca_de -> cambiarFragmento(AcercaDeFragment())
                R.id.nav_salir -> finishAffinity() // Cierra la app por completo limpiando la pila
                //R.id.nav_salir -> finishAffinity() // Cierra la app por completo limpiando la pila
            }
            drawerLayout.closeDrawers()
            true
        }

        // 6. Configurar barra inferior (Bottom Navigation View)
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

    /**
     * Función auxiliar para reemplazar el fragmento actual dentro del contenedor
     */
    private fun cambiarFragmento(fragmento: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragmento)
            .commit()
    }

    private fun solicitarPermisosNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }
}