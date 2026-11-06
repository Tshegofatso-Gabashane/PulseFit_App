package com.example.pulsefit_app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part

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
    val calories: Int,
    val createdAt: Long = 0
)

// The id is made on the phone, so sending the same meal twice never creates a duplicate
data class AddMealRequest(
    val id: String,
    val name: String,
    val calories: Int,
    val createdAt: Long
)

// Push notification token of this phone
data class DeviceRequest(
    val token: String
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

    // Upload profile image to Azure Blob Storage
    @Multipart
    @POST("api/uploads/image")
    fun uploadProfileImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Call<ImageUploadResponse>

    // Retrieve profile image
    @GET("api/profile/image")
    fun getProfileImage(
        @Header("Authorization") token: String
    ): Call<ResponseBody>

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

    @DELETE("meals/{id}")
    fun deleteMeal(
        @Path("id") id: String
    ): Call<Void>

    // Push notifications
    @POST("devices")
    fun registerDevice(
        @Body device: DeviceRequest
    ): Call<Void>

    @POST("notifications/test")
    fun sendTestNotification(): Call<Void>

    // OpenWeather endpoint
    @GET("data/2.5/weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Call<WeatherResponse>
}

data class ImageUploadResponse(
    val message: String,
    val blobName: String
)