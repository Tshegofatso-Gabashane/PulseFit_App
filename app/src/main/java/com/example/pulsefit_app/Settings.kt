package com.example.pulsefit_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)


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



        Log.d("PulseFit", "Settings screen opened")

        // Settings controls
        val languageSpinner =
            findViewById<Spinner>(R.id.languageSpinner)

        val unitsSpinner =
            findViewById<Spinner>(R.id.unitsSpinner)

        val syncSwitch =
            findViewById<Switch>(R.id.syncSwitch)

        val profileDetailsButton =
            findViewById<Button>(R.id.profileDetailsButton)

        val manualSyncButton =
            findViewById<Button>(R.id.manualSyncButton)

        val logoutButton =
            findViewById<Button>(R.id.logoutButton)

        // Bottom navigation
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


        // Language options
        val languages = arrayOf(
            "English",
            "isiZulu",
            "Sesotho"
        )

        val languageAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        )

        languageAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        languageSpinner.adapter = languageAdapter


        // Unit options
        val units = arrayOf(
            "Metric (kg/km)",
            "Imperial (lb/mi)"
        )

        val unitsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            units
        )

        unitsAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        unitsSpinner.adapter = unitsAdapter

        val preferences =
            getSharedPreferences(
                "PulseFitSettings",
                MODE_PRIVATE
            )

        val savedLanguage =
            preferences.getInt(
                "language_position",
                0
            )

        val savedUnits =
            preferences.getInt(
                "units_position",
                0
            )

        languageSpinner.setSelection(savedLanguage)
        unitsSpinner.setSelection(savedUnits)

        languageSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    preferences.edit()
                        .putInt(
                            "language_position",
                            position
                        )
                        .apply()

                    Log.d(
                        "Settings",
                        "Language preference saved"
                    )
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }

        unitsSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    preferences.edit()
                        .putInt(
                            "units_position",
                            position
                        )
                        .apply()

                    Log.d(
                        "Settings",
                        "Unit preference saved"
                    )
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }

        // Sync preference switch
        syncSwitch.setOnCheckedChangeListener { _, isChecked ->

            preferences.edit()
                .putBoolean(
                    "sync_enabled",
                    isChecked
                )
                .apply()

            if (isChecked) {

                Toast.makeText(
                    this,
                    "Cloud sync preference enabled",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Cloud sync preference disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }

            Log.d(
                "PulseFit",
                "Sync preference: $isChecked"
            )
        }

        val savedSyncPreference =
            preferences.getBoolean(
                "sync_enabled",
                false
            )

        syncSwitch.isChecked =
            savedSyncPreference

        // Profile details
        profileDetailsButton.setOnClickListener {

            val currentUser =
                FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {

                Toast.makeText(
                    this,
                    "Please log in again",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                val db =
                    FirebaseFirestore.getInstance()

                db.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->

                        if (document.exists()) {

                            val name =
                                document.getString("name")
                                    ?: "Not provided"

                            val email =
                                document.getString("email")
                                    ?: currentUser.email
                                    ?: "Not provided"

                            val weight =
                                document.get("weight")
                                    ?.toString()
                                    ?: "Not provided"

                            val height =
                                document.get("height")
                                    ?.toString()
                                    ?: "Not provided"

                            val age =
                                document.get("age")
                                    ?.toString()
                                    ?: "Not provided"

                            val goal =
                                document.getString("goal")
                                    ?: "Not provided"

                            val profileMessage =
                                """
                        Name: $name
                        Email: $email
                        Weight: $weight
                        Height: $height
                        Age: $age
                        Goal: $goal
                        """.trimIndent()

                            AlertDialog.Builder(this)
                                .setTitle("Profile Details")
                                .setMessage(profileMessage)
                                .setPositiveButton(
                                    "CLOSE",
                                    null
                                )
                                .show()

                            Log.d(
                                "PulseFit",
                                "Profile details loaded"
                            )

                        } else {

                            Toast.makeText(
                                this,
                                "Profile details not found",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.w(
                                "PulseFit",
                                "User profile document not found"
                            )
                        }
                    }
                    .addOnFailureListener { exception ->

                        Toast.makeText(
                            this,
                            "Unable to load profile details",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.e(
                            "PulseFit",
                            "Profile details error",
                            exception
                        )
                    }
            }
        }

        // Manual cloud sync
        manualSyncButton.setOnClickListener {

            if (!NetworkUtils.isInternetAvailable(this)) {

                Toast.makeText(
                    this,
                    "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                val db =
                    FirebaseFirestore.getInstance()

                db.collection("exercises")
                    .get()
                    .addOnSuccessListener { snapshot ->

                        Toast.makeText(
                            this,
                            "Cloud connection successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d(
                            "PulseFit",
                            "Cloud check successful: ${snapshot.size()} exercises found"
                        )
                    }
                    .addOnFailureListener { exception ->

                        Toast.makeText(
                            this,
                            "Cloud connection failed",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.e(
                            "PulseFit",
                            "Cloud check failed",
                            exception
                        )
                    }
            }
        }


        // Logout
        logoutButton.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            Log.d("PulseFit", "User logged out")

            Toast.makeText(
                this,
                "Logged out successfully",
                Toast.LENGTH_SHORT
            ).show()

            val intent =
                Intent(this, login::class.java)

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

    }
}