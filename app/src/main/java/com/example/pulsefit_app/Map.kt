package com.example.pulsefit_app

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
import java.util.Locale
import com.google.android.material.bottomnavigation.BottomNavigationView

class Map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_map)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        Log.d("PulseFit", "Map screen opened")


        // Load Google Map
        val mapFragment =
            supportFragmentManager
                .findFragmentById(R.id.googleMap)
                    as SupportMapFragment

        mapFragment.getMapAsync(this)


        // Connect XML components
        val destinationInput =
            findViewById<EditText>(
                R.id.destinationInput
            )

        val searchRouteButton =
            findViewById<Button>(
                R.id.searchRouteButton
            )

        val startTrackingButton =
            findViewById<Button>(
                R.id.startTrackingButton
            )

        val stopTrackingButton =
            findViewById<Button>(
                R.id.stopTrackingButton
            )

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.homeButton   // highlight the current tab

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeButton -> true   // already here, do nothing

                R.id.mapButton -> {
                    startActivity(Intent(this, Map::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.exerciseButton -> {
                    startActivity(Intent(this, Exercises::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.dietButton -> {
                    startActivity(Intent(this, Diet::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.settingsButton -> {
                    startActivity(Intent(this, Settings::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }


        // Search for destination
        searchRouteButton.setOnClickListener {

            val destination =
                destinationInput.text
                    .toString()
                    .trim()

            if (destination.isEmpty()) {

                Toast.makeText(
                    this,
                    "Enter a destination",
                    Toast.LENGTH_SHORT
                ).show()

            } else if (!::googleMap.isInitialized) {

                Toast.makeText(
                    this,
                    "Map is still loading",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                searchDestination(destination)
            }
        }


        // Start tracking prototype
        startTrackingButton.setOnClickListener {

            Toast.makeText(
                this,
                "Tracking started",
                Toast.LENGTH_SHORT
            ).show()

            Log.d(
                "PulseFitMap",
                "Tracking started"
            )
        }


        // Stop tracking prototype
        stopTrackingButton.setOnClickListener {

            Toast.makeText(
                this,
                "Tracking paused/stopped",
                Toast.LENGTH_SHORT
            ).show()

            Log.d(
                "PulseFitMap",
                "Tracking stopped"
            )
        }



    }


    // Called when Google Map has loaded
    override fun onMapReady(map: GoogleMap) {

        googleMap = map

        val johannesburg =
            LatLng(
                -26.2041,
                28.0473
            )

        googleMap.addMarker(
            MarkerOptions()
                .position(johannesburg)
                .title("PulseFit")
        )

        googleMap.moveCamera(
            CameraUpdateFactory
                .newLatLngZoom(
                    johannesburg,
                    11f
                )
        )

        Log.d(
            "PulseFitMap",
            "Google Map loaded"
        )
    }


    // Search for a destination
    @Suppress("DEPRECATION")
    private fun searchDestination(
        destination: String
    ) {

        try {

            val geocoder =
                Geocoder(
                    this,
                    Locale.getDefault()
                )

            val searchQuery = "$destination, South Africa"

            val addresses =
                geocoder.getFromLocationName(
                    searchQuery,
                    5
                )

            if (!addresses.isNullOrEmpty()) {

                val address =
                    addresses[0]

                val location =
                    LatLng(
                        address.latitude,
                        address.longitude
                    )

                googleMap.clear()

                googleMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(destination)
                )

                googleMap.animateCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(
                            location,
                            14f
                        )
                )

                Toast.makeText(
                    this,
                    "Destination found",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(
                    "PulseFitMap",
                    "Destination found: $destination"
                )

            } else {

                Toast.makeText(
                    this,
                    "Destination not found",
                    Toast.LENGTH_SHORT
                ).show()

                Log.w(
                    "PulseFitMap",
                    "Destination not found"
                )
            }

        } catch (e: Exception) {

            Log.e(
                "PulseFitMap",
                "Destination search failed",
                e
            )

            Toast.makeText(
                this,
                "Unable to search destination",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}