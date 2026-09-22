package com.example.pulsefit_app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.POST

// Exercise data received from PulseFit REST API
data class ExerciseData(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val difficulty: String,
    val category: String
)

// Meal data received from PulseFit REST API
data class MealData(
    val id: String,
    val name: String,
    val calories: Int
)

data class AddMealRequest(
    val name: String,
    val calories: Int
)

// Weather data
data class WeatherResponse(
    val main: MainWeather,
    val weather: List<WeatherDescription>,
    val name: String
)

data class MainWeather(
    val temp: Double
)

data class WeatherDescription(
    val description: String
)

interface ApiService {

    // PulseFit exercise endpoint
    @GET("exercises")
    fun getExercises(): Call<List<ExerciseData>>

    // PulseFit meal endpoint
    @GET("meals")
    fun getMeals(): Call<List<MealData>>

    @POST("meals")
    fun addMeal(
        @Body meal: AddMealRequest
    ): Call<MealData>

    // OpenWeather endpoint
    @GET("data/2.5/weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Call<WeatherResponse>
}