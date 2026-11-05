package com.example.pulsefit_app

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val API_HOST = "pulsefit-api-uye8.onrender.com"

    private const val BASE_URL = "https://$API_HOST"

    // Adds the logged-in user's Firebase token so the API knows who is calling.
    // Only added for our own API, never for other sites such as OpenWeather.
    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null || request.url.host != API_HOST) {
                return chain.proceed(request)
            }

            val token = try {
                Tasks.await(user.getIdToken(false)).token
            } catch (e: Exception) {
                null
            }

            val signedRequest = if (token != null) {
                request.newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                request
            }
            return chain.proceed(signedRequest)
        }
    }

    // Free hosting puts the API to sleep, and waking it can take about a minute
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}