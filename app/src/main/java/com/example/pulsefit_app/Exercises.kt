package com.example.pulsefit_app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Exercises : AppCompatActivity() {

    private var allExercises: List<ExerciseData> = emptyList()

    private lateinit var exerciseA: TextView
    private lateinit var exerciseB: TextView
    private lateinit var exerciseC: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_exercises)

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

        Log.d("PulseFit", "Exercise screen opened")

        val searchInput =
            findViewById<EditText>(R.id.exerciseSearchInput)

        val allButton =
            findViewById<Button>(R.id.allFilterButton)

        val cardioButton =
            findViewById<Button>(R.id.cardioFilterButton)

        val strengthButton =
            findViewById<Button>(R.id.strengthFilterButton)

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

        exerciseA =
            findViewById(R.id.exerciseA)

        exerciseB =
            findViewById(R.id.exerciseB)

        exerciseC =
            findViewById(R.id.exerciseC)


        // Load exercises from PulseFit REST API
        if (!NetworkUtils.isInternetAvailable(this)) {

            Toast.makeText(
                this,
                "No internet connection",
                Toast.LENGTH_SHORT
            ).show()

            Log.w(
                "PulseFit",
                "Exercise API unavailable - no internet"
            )

        } else {

            ApiClient.apiService
                .getExercises()
                .enqueue(
                    object : Callback<List<ExerciseData>> {

                        override fun onResponse(
                            call: Call<List<ExerciseData>>,
                            response: Response<List<ExerciseData>>
                        ) {

                            if (response.isSuccessful) {

                                val exercises =
                                    response.body()

                                if (!exercises.isNullOrEmpty()) {

                                    allExercises = exercises

                                    displayExercises(
                                        allExercises
                                    )

                                    Log.d(
                                        "PulseFitAPI",
                                        "Exercises loaded: ${exercises.size}"
                                    )

                                } else {

                                    displayExercises(
                                        emptyList()
                                    )

                                    Log.w(
                                        "PulseFitAPI",
                                        "No exercises returned"
                                    )
                                }

                            } else {

                                Toast.makeText(
                                    this@Exercises,
                                    "Unable to load exercises",
                                    Toast.LENGTH_SHORT
                                ).show()

                                Log.e(
                                    "PulseFitAPI",
                                    "API error: ${response.code()}"
                                )
                            }
                        }


                        override fun onFailure(
                            call: Call<List<ExerciseData>>,
                            t: Throwable
                        ) {

                            Toast.makeText(
                                this@Exercises,
                                "Unable to load exercises",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.e(
                                "PulseFitAPI",
                                "Could not connect to REST API",
                                t
                            )
                        }
                    }
                )
        }


        // Search by exercise name or muscle group
        searchInput.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val search =
                        s.toString().trim()

                    val filtered =
                        allExercises.filter {

                            it.name.contains(
                                search,
                                ignoreCase = true
                            ) ||
                                    it.muscleGroup.contains(
                                        search,
                                        ignoreCase = true
                                    )
                        }

                    displayExercises(filtered)
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )


        // Show all exercises
        allButton.setOnClickListener {

            searchInput.text.clear()

            displayExercises(
                allExercises
            )

            Log.d(
                "PulseFit",
                "All exercise filter selected"
            )
        }


        // Show cardio exercises
        cardioButton.setOnClickListener {

            val cardioExercises =
                allExercises.filter {

                    it.category.equals(
                        "Cardio",
                        ignoreCase = true
                    )
                }

            displayExercises(
                cardioExercises
            )

            Log.d(
                "PulseFit",
                "Cardio filter selected"
            )
        }


        // Show strength exercises
        strengthButton.setOnClickListener {

            val strengthExercises =
                allExercises.filter {

                    it.category.equals(
                        "Strength",
                        ignoreCase = true
                    )
                }

            displayExercises(
                strengthExercises
            )

            Log.d(
                "PulseFit",
                "Strength filter selected"
            )
        }


        // Bottom navigation
        homeButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Dashboard::class.java
                )
            )

            finish()
        }


        mapButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Map::class.java
                )
            )

            finish()
        }


        exerciseButton.setOnClickListener {

            Toast.makeText(
                this,
                "You are already on Exercises",
                Toast.LENGTH_SHORT
            ).show()
        }


        dietButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Diet::class.java
                )
            )

            finish()
        }


        settingsButton.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    Settings::class.java
                )
            )

            finish()
        }
    }


    // Display a maximum of three exercises
    private fun displayExercises(
        exercises: List<ExerciseData>
    ) {

        val exerciseViews =
            listOf(
                exerciseA,
                exerciseB,
                exerciseC
            )

        exerciseViews.forEach {
            it.visibility = View.GONE
        }


        exercises
            .take(3)
            .forEachIndexed { index, exercise ->

                exerciseViews[index].text =
                    "${exercise.name}\n" +
                            "Target: ${exercise.muscleGroup}\n" +
                            "Difficulty: ${exercise.difficulty}\n" +
                            "Category: ${exercise.category}"

                exerciseViews[index].visibility =
                    View.VISIBLE
            }


        if (exercises.isEmpty()) {

            exerciseA.text =
                "No exercises found"

            exerciseA.visibility =
                View.VISIBLE
        }
    }
}