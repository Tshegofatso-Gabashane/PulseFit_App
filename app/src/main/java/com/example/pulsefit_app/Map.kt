package com.example.pulsefit_app

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import java.util.Locale

class Map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_map)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("PulseFit", "Map screen opened")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // ---- Views ----
        val destinationInput     = findViewById<EditText>(R.id.destinationInput)
        val searchRouteButton    = findViewById<MaterialButton>(R.id.searchRouteButton)
        val startTrackingButton  = findViewById<MaterialButton>(R.id.startTrackingButton)
        val stopTrackingButton   = findViewById<MaterialButton>(R.id.stopTrackingButton)
        val bottomNav            = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.mapButton

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mapButton -> true   // already here

                R.id.homeButton     -> navigateTo(Dashboard::class.java)
                R.id.exerciseButton -> navigateTo(Exercises::class.java)
                R.id.dietButton     -> navigateTo(Diet::class.java)
                R.id.settingsButton -> navigateTo(Settings::class.java)
                else -> false
            }
        }

        searchRouteButton.setOnClickListener {
            val destination = destinationInput.text.toString().trim()
            when {
                destination.isEmpty() ->
                    Toast.makeText(this, "Enter a destination", Toast.LENGTH_SHORT).show()

                !::googleMap.isInitialized ->
                    Toast.makeText(this, "Map is still loading", Toast.LENGTH_SHORT).show()

                else -> searchDestination(destination)
            }
        }

        startTrackingButton.setOnClickListener {
            Toast.makeText(this, "Tracking started", Toast.LENGTH_SHORT).show()
            Log.d("PulseFitMap", "Tracking started")
        }

        stopTrackingButton.setOnClickListener {
            Toast.makeText(this, "Tracking paused/stopped", Toast.LENGTH_SHORT).show()
            Log.d("PulseFitMap", "Tracking stopped")
        }
    }

    private fun navigateTo(destination: Class<*>): Boolean {
        startActivity(Intent(this, destination))
        overridePendingTransition(0, 0)
        return true
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val johannesburg = LatLng(-26.2041, 28.0473)

        googleMap.addMarker(
            MarkerOptions().position(johannesburg).title("PulseFit")
        )

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(johannesburg, 11f)
        )

        Log.d("PulseFitMap", "Google Map loaded")
    }

    @Suppress("DEPRECATION")
    private fun searchDestination(destination: String) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(destination, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val location = LatLng(address.latitude, address.longitude)

                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions().position(location).title(destination)
                )
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(location, 14f)
                )

                Toast.makeText(this, "Destination found", Toast.LENGTH_SHORT).show()
                Log.d("PulseFitMap", "Destination found: $destination")
            } else {
                Toast.makeText(this, "Destination not found", Toast.LENGTH_SHORT).show()
                Log.w("PulseFitMap", "Destination not found")
            }
        } catch (e: Exception) {
            Log.e("PulseFitMap", "Destination search failed", e)
            Toast.makeText(this, "Unable to search destination", Toast.LENGTH_SHORT).show()
        }
    }
}