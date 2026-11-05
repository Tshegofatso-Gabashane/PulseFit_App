
package com.example.pulsefit_app

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import okhttp3.ResponseBody

data class ExerciseData(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val difficulty: String,
    val category: String
)

data class MealData(
    val id: String,
    val name: String,
    val calories: Int
)

data class AddMealRequest(
    val name: String,
    val calories: Int
)

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

// Azure image upload response
data class ImageUploadResponse(
    val message: String,
    val uploadId: String,
    val blobName: String
)

interface ApiService {

    @GET("exercises")
    fun getExercises(): Call<List<ExerciseData>>

    @GET("meals")
    fun getMeals(): Call<List<MealData>>

    @POST("meals")
    fun addMeal(
        @Body meal: AddMealRequest
    ): Call<MealData>

    @Multipart
    @POST("api/uploads/image")
    fun uploadProfileImage(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part
    ): Call<ImageUploadResponse>

    @GET("api/profile/image")
    fun getProfileImage(
        @Header("Authorization") authorization: String
    ): Call<ResponseBody>

    @GET("data/2.5/weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Call<WeatherResponse>
}
