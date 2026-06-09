package com.example.proyecto // 👈 Asegúrate de que esta línea coincida con tu paquete real

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Encontrar los componentes por su ID (Dejamos que Kotlin detecte el tipo automático)
        val drawerLayout = findViewById<DrawerLayout>(R.id.main)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 2. Configurar la Toolbar como la barra de soporte de la actividad
        setSupportActionBar(toolbar)
        // 👈 AGREGA ESTA LÍNEA AQUÍ:
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 3. Crear el "Toggle" (el puente entre la Toolbar y el Drawer)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        // 4. Conectar el toggle al DrawerLayout y sincronizarlo
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 5. Configurar qué pasa cuando das clic a una opción del menú hamburguesa
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                // Aquí manejas los clics de tu drawer_menu.xml
                // R.id.tu_item_id -> { }
            }
            drawerLayout.closeDrawers()
            true
        }

        // 6. Configurar qué pasa cuando das clic a la barra inferior
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Aquí manejas los clics de tu bottom_menu.xml
                // R.id.tu_item_id -> { true }
                else -> false
            }
        }
    }
}