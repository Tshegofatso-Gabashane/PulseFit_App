package com.example.pulsefit_app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Exercises : AppCompatActivity() {

    private var allExercises: List<ExerciseData> = emptyList()

    // One entry per card (3 cards total)
    private lateinit var nameViews:   List<TextView>
    private lateinit var targetViews: List<TextView>
    private lateinit var metViews:    List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_exercises)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("PulseFit", "Exercise screen opened")

        // ---- Views ----
        val searchInput = findViewById<EditText>(R.id.exerciseSearchInput)
        val filterGroup = findViewById<ChipGroup>(R.id.filterChipGroup)
        val bottomNav   = findViewById<BottomNavigationView>(R.id.bottomNav)

        nameViews = listOf(
            findViewById(R.id.exerciseA),
            findViewById(R.id.exerciseB),
            findViewById(R.id.exerciseC)
        )
        targetViews = listOf(
            findViewById(R.id.exerciseATarget),
            findViewById(R.id.exerciseBTarget),
            findViewById(R.id.exerciseCTarget)
        )
        metViews = listOf(
            findViewById(R.id.exerciseAMet),
            findViewById(R.id.exerciseBMet),
            findViewById(R.id.exerciseCMet)
        )

        // ---- Bottom navigation ----
        bottomNav.selectedItemId = R.id.exerciseButton

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.exerciseButton -> true   // already here

                R.id.homeButton     -> navigateTo(Dashboard::class.java)
                R.id.mapButton      -> navigateTo(Map::class.java)
                R.id.dietButton     -> navigateTo(Diet::class.java)
                R.id.settingsButton -> navigateTo(Settings::class.java)
                else -> false
            }
        }

        // ---- API: load exercises ----
        if (!NetworkUtils.isInternetAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            Log.w("PulseFit", "Exercise API unavailable - no internet")
        } else {
            ApiClient.apiService.getExercises().enqueue(object : Callback<List<ExerciseData>> {

                override fun onResponse(
                    call: Call<List<ExerciseData>>,
                    response: Response<List<ExerciseData>>
                ) {
                    if (response.isSuccessful) {
                        val exercises = response.body()
                        if (!exercises.isNullOrEmpty()) {
                            allExercises = exercises
                            displayExercises(allExercises)
                            Log.d("PulseFitAPI", "Exercises loaded: ${exercises.size}")
                        } else {
                            displayExercises(emptyList())
                            Log.w("PulseFitAPI", "No exercises returned")
                        }
                    } else {
                        Toast.makeText(this@Exercises, "Unable to load exercises", Toast.LENGTH_SHORT).show()
                        Log.e("PulseFitAPI", "API error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<ExerciseData>>, t: Throwable) {
                    Toast.makeText(this@Exercises, "Unable to load exercises", Toast.LENGTH_SHORT).show()
                    Log.e("PulseFitAPI", "Could not connect to REST API", t)
                }
            })
        }

        // ---- Search ----
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val search = s.toString().trim()
                val filtered = allExercises.filter {
                    it.name.contains(search, ignoreCase = true) ||
                            it.muscleGroup.contains(search, ignoreCase = true)
                }
                displayExercises(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ---- Filter chips ----
        filterGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            when (id) {
                R.id.allFilterButton -> {
                    searchInput.text.clear()
                    displayExercises(allExercises)
                    Log.d("PulseFit", "All filter selected")
                }
                R.id.cardioFilterButton -> {
                    displayExercises(allExercises.filter {
                        it.category.equals("Cardio", ignoreCase = true)
                    })
                    Log.d("PulseFit", "Cardio filter selected")
                }
                R.id.strengthFilterButton -> {
                    displayExercises(allExercises.filter {
                        it.category.equals("Strength", ignoreCase = true)
                    })
                    Log.d("PulseFit", "Strength filter selected")
                }
            }
        }
    }

    // ---- Helpers ----
    private fun navigateTo(destination: Class<*>): Boolean {
        startActivity(Intent(this, destination))
        overridePendingTransition(0, 0)
        return true
    }

    /**
     * Show up to 3 exercises. Each card has:
     *   - name   (TextView)
     *   - target (TextView)  ← muscle group, uppercased
     *   - met    (TextView)  ← "MET x.y" badge
     *
     * Any unused card slots get hidden.
     */
    private fun displayExercises(exercises: List<ExerciseData>) {

        // Hide all 3 cards by default
        for (i in 0..2) {
            nameViews[i].visibility   = View.GONE
            targetViews[i].visibility = View.GONE
            metViews[i].visibility    = View.GONE
        }

        // Show up to 3 results
        exercises.take(3).forEachIndexed { index, exercise ->
            nameViews[index].text = exercise.name
            nameViews[index].visibility = View.VISIBLE

            targetViews[index].text = exercise.muscleGroup.uppercase()
            targetViews[index].visibility = View.VISIBLE

            metViews[index].text = "MET ${exercise.difficulty}"
            metViews[index].visibility = View.VISIBLE
        }

        // Empty state — reuse the first name slot to say "No exercises found"
        if (exercises.isEmpty()) {
            nameViews[0].text = "No exercises found"
            nameViews[0].visibility = View.VISIBLE
        }
    }
}