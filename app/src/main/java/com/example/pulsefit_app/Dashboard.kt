package com.example.pulsefit_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.firebase.firestore.FirebaseFirestore


class Dashboard : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        enableEdgeToEdge()

        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        Log.d("PulseFit", "Dashboard opened")

        val greetingText =
            findViewById<TextView>(R.id.greetingText)

        val weatherText =
            findViewById<TextView>(R.id.weatherText)

        val xpLevelText =
            findViewById<TextView>(R.id.xpLevelText)

        val startWorkoutButton =
            findViewById<Button>(R.id.startWorkoutButton)

        val homeButton =
            findViewById<Button>(R.id.homeButton)

        val mapButton =
            findViewById<Button>(R.id.mapButton)

        val exerciseButton =
            findViewById<Button>(R.id.exerciseButton)

        val dietButton =
            findViewById<Button>(R.id.dietButton)

        val settingsButton =
            findViewById<Button>(R.id.settingsButton)


        // Load user's name from Firestore
        val weatherApiKey = BuildConfig.WEATHER_API_KEY

        // Prototype gamification
        val currentXp = 250

        val currentLevel = when {
            currentXp >= 1000 -> 5
            currentXp >= 750 -> 4
            currentXp >= 500 -> 3
            currentXp >= 250 -> 2
            else -> 1
        }

        xpLevelText.text =
            "XP $currentXp | Level $currentLevel"

        Log.d(
            "PulseFit",
            "XP: $currentXp, Level: $currentLevel"
        )

        WeatherClient.apiService
            .getWeather(
                "Johannesburg",
                weatherApiKey
            )
            .enqueue(object : Callback<WeatherResponse> {

                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {

                    if (response.isSuccessful) {

                        val weather = response.body()

                        if (weather != null) {

                            weatherText.text =
                                "${weather.name}: " +
                                        "${weather.main.temp.toInt()}°C - " +
                                        weather.weather.firstOrNull()?.description

                            Log.d(
                                "WeatherAPI",
                                "Weather loaded successfully"
                            )
                        }
                    } else {

                        weatherText.text =
                            "Weather unavailable"

                        Log.e(
                            "WeatherAPI",
                            "Weather error: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<WeatherResponse>,
                    t: Throwable
                ) {

                    weatherText.text =
                        "Weather unavailable"

                    Log.e(
                        "WeatherAPI",
                        "Weather request failed",
                        t
                    )
                }
            })


        val userId = auth.currentUser?.uid

        if (userId != null) {

            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {

                        val name =
                            document.getString("name")

                        greetingText.text =
                            "Welcome, $name!"

                        Log.d(
                            "Dashboard",
                            "User profile loaded"
                        )
                    }
                }
                .addOnFailureListener { error ->

                    Log.e(
                        "Dashboard",
                        "Failed to load profile",
                        error
                    )
                }
        }


        startWorkoutButton.setOnClickListener {

            val intent =
                Intent(this, Map::class.java)

            startActivity(intent)
        }


        homeButton.setOnClickListener {

            Toast.makeText(
                this,
                "You are already on Home",
                Toast.LENGTH_SHORT
            ).show()
        }


        mapButton.setOnClickListener {

            val intent =
                Intent(this, Map::class.java)

            startActivity(intent)
        }


        exerciseButton.setOnClickListener {

            val intent = Intent(this, Exercises::class.java)

            startActivity(intent)
        }


        dietButton.setOnClickListener {

            val intent =
                Intent(this, Diet::class.java)

            startActivity(intent)
        }


        settingsButton.setOnClickListener {

            val intent =
                Intent(this, Settings::class.java)

            startActivity(intent)
        }
    }
}