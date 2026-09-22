package com.example.pulsefit_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import android.app.AlertDialog
import android.text.InputType
import android.widget.LinearLayout

class Diet : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_diet)

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

        Log.d("PulseFit", "Diet screen opened")


        // Connect XML components
        val foodSearchInput =
            findViewById<EditText>(R.id.foodSearchInput)

        val barcodeButton =
            findViewById<Button>(R.id.barcodeButton)

        val addMealButton =
            findViewById<Button>(R.id.addMealButton)

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

        val breakfastText =
            findViewById<TextView>(R.id.breakfastText)

        val lunchText =
            findViewById<TextView>(R.id.lunchText)

        val dinnerText =
            findViewById<TextView>(R.id.dinnerText)


        // Check internet before requesting meal information
        if (!NetworkUtils.isInternetAvailable(this)) {

            Toast.makeText(
                this,
                "No internet connection",
                Toast.LENGTH_SHORT
            ).show()

            Log.w(
                "PulseFit",
                "Meal API unavailable - no internet"
            )

        } else {

            // Get meals from PulseFit REST API
            ApiClient.apiService.getMeals()
                .enqueue(object : Callback<List<MealData>> {

                    override fun onResponse(
                        call: Call<List<MealData>>,
                        response: Response<List<MealData>>
                    ) {

                        if (response.isSuccessful) {

                            val meals = response.body()

                            if (!meals.isNullOrEmpty()) {

                                breakfastText.text =
                                    "${meals[0].name} ${meals[0].calories} kcal"

                                if (meals.size > 1) {

                                    lunchText.text =
                                        "${meals[1].name} ${meals[1].calories} kcal"
                                }

                                if (meals.size > 2) {

                                    dinnerText.text =
                                        "${meals[2].name} ${meals[2].calories} kcal"
                                }

                                Log.d(
                                    "PulseFitAPI",
                                    "Meals loaded: ${meals.size}"
                                )

                            } else {

                                Log.w(
                                    "PulseFitAPI",
                                    "No meals returned by API"
                                )
                            }

                        } else {

                            Log.e(
                                "PulseFitAPI",
                                "Meal API error: ${response.code()}"
                            )

                            Toast.makeText(
                                this@Diet,
                                "Unable to load meals",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<List<MealData>>,
                        t: Throwable
                    ) {

                        Log.e(
                            "PulseFitAPI",
                            "Could not connect to meal API",
                            t
                        )

                        Toast.makeText(
                            this@Diet,
                            "Unable to load meals",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }


        // Barcode button
        barcodeButton.setOnClickListener {

            Toast.makeText(
                this,
                "Barcode feature will be connected later",
                Toast.LENGTH_SHORT
            ).show()
        }


        // Add meal button
        addMealButton.setOnClickListener {

            val mealNameInput = EditText(this)
            mealNameInput.hint = "Meal name"

            val caloriesInput = EditText(this)
            caloriesInput.hint = "Calories"
            caloriesInput.inputType = InputType.TYPE_CLASS_NUMBER

            val dialogLayout = LinearLayout(this)

            dialogLayout.orientation = LinearLayout.VERTICAL

            dialogLayout.setPadding(
                50,
                20,
                50,
                20
            )

            dialogLayout.addView(mealNameInput)
            dialogLayout.addView(caloriesInput)

            AlertDialog.Builder(this)
                .setTitle("Add Meal")
                .setView(dialogLayout)
                .setPositiveButton("ADD") { _, _ ->

                    val mealName =
                        mealNameInput.text.toString().trim()

                    val calories =
                        caloriesInput.text
                            .toString()
                            .toIntOrNull()

                    if (mealName.isEmpty() || calories == null) {

                        Toast.makeText(
                            this,
                            "Enter a meal name and valid calories",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else if (!NetworkUtils.isInternetAvailable(this)) {

                        Toast.makeText(
                            this,
                            "No internet connection",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        val newMeal =
                            AddMealRequest(
                                name = mealName,
                                calories = calories
                            )

                        ApiClient.apiService
                            .addMeal(newMeal)
                            .enqueue(
                                object : Callback<MealData> {

                                    override fun onResponse(
                                        call: Call<MealData>,
                                        response: Response<MealData>
                                    ) {

                                        if (response.isSuccessful) {

                                            val addedMeal =
                                                response.body()

                                            Toast.makeText(
                                                this@Diet,
                                                "Meal added successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            Log.d(
                                                "PulseFitAPI",
                                                "Meal added: ${addedMeal?.name}"
                                            )

                                        } else {

                                            Toast.makeText(
                                                this@Diet,
                                                "Unable to add meal",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            Log.e(
                                                "PulseFitAPI",
                                                "Add meal error: ${response.code()}"
                                            )
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<MealData>,
                                        t: Throwable
                                    ) {

                                        Toast.makeText(
                                            this@Diet,
                                            "Could not connect to meal API",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        Log.e(
                                            "PulseFitAPI",
                                            "Add meal request failed",
                                            t
                                        )
                                    }
                                }
                            )
                    }
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }


        // Navigation
        homeButton.setOnClickListener {

            val intent =
                Intent(this, Dashboard::class.java)

            startActivity(intent)
            finish()
        }


        mapButton.setOnClickListener {

            val intent =
                Intent(this, Map::class.java)

            startActivity(intent)
            finish()
        }


        exerciseButton.setOnClickListener {

            val intent =
                Intent(this, Exercises::class.java)

            startActivity(intent)
            finish()
        }


        dietButton.setOnClickListener {

            Toast.makeText(
                this,
                "You are already on Diet",
                Toast.LENGTH_SHORT
            ).show()
        }


        settingsButton.setOnClickListener {

            val intent =
                Intent(this, Settings::class.java)

            startActivity(intent)
            finish()
        }
    }
}