package org.example.frontend.api

import org.example.frontend.NetworkConfig
import org.example.frontend.progresstracking.DyslexiaError
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// 1. The Interface: Defines the URL paths for your Flask Backend
interface ApiService {
    // This matches the @app.route("/get_common_errors/<user_id>") in your Python code
    @GET("get_common_errors/{user_id}")
    suspend fun getCommonErrors(
        @Path("user_id") userId: String
    ): List<DyslexiaError>
}

// 2. The Singleton Client: Handles the actual connection
object RetrofitClient {
    // IMPORTANT: Use your specific Flask Server IP address here
    private  val BASE_URL = "http://${NetworkConfig.SERVER_IP}/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Converts JSON to Kotlin objects
            .build()
            .create(ApiService::class.java)
    }
}