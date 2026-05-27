package com.example.dietassist.data.remote

import com.example.dietassist.data.model.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface RetrofitApiService {

    @POST("api/auth/google")
    suspend fun verifyGoogleAuth(
        @Body body: AuthRequest
    ): Response<AuthResponse>

    @GET("api/food")
    suspend fun getFoodLogs(
        @Query("userId") userId: String,
        @Query("date") date: String? = null
    ): Response<FoodLogsListResponse>

    @POST("api/food")
    suspend fun addFoodLog(
        @Body body: FoodLog
    ): Response<FoodLogResponse>

    @PUT("api/food/{id}")
    suspend fun updateFoodLog(
        @Path("id") id: String,
        @Body body: FoodLog
    ): Response<FoodLogResponse>

    @DELETE("api/food/{id}")
    suspend fun deleteFoodLog(
        @Path("id") id: String
    ): Response<GenericResponse>

    @GET("api/water")
    suspend fun getWaterLogs(
        @Query("userId") userId: String,
        @Query("date") date: String? = null
    ): Response<WaterLogsResponse>

    @POST("api/water")
    suspend fun addWaterLog(
        @Body body: WaterLog
    ): Response<WaterLogItemResponse>

    @POST("api/ai/analyze")
    suspend fun analyzeFood(
        @Body body: AIAnalyzeRequest
    ): Response<AIAnalysisResponse>

    @POST("api/ai/chat")
    suspend fun chatWithAI(
        @Body body: ChatRequest
    ): Response<ChatResponse>
}

// REQUEST & RESPONSE DTOs
data class AuthRequest(
    @SerializedName("idToken") val idToken: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("userId") val userId: String,
    @SerializedName("daily_calorie_target") val dailyCalorieTarget: Int? = null,
    @SerializedName("weight") val weight: Float? = null,
    @SerializedName("height") val height: Float? = null
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: UserProfile
)

data class FoodLogsListResponse(
    val success: Boolean,
    val logs: List<FoodLog>
)

data class FoodLogResponse(
    val success: Boolean,
    val message: String,
    val log: FoodLog
)

data class WaterLogsResponse(
    val success: Boolean,
    val logs: List<WaterLog>,
    val total_ml: Int
)

data class WaterLogItemResponse(
    val success: Boolean,
    val message: String,
    val log: WaterLog
)

data class AIAnalyzeRequest(
    val textDescription: String?,
    val image: String?, // base64 string
    val mimeType: String? = "image/jpeg"
)

data class GenericResponse(
    val success: Boolean,
    val message: String
)

// DTO untuk Chat Kesehatan AI
data class ChatRequest(
    @SerializedName("messages") val messages: List<ChatDtoMessage>
)

data class ChatDtoMessage(
    @SerializedName("role") val role: String, // "user" atau "assistant"
    @SerializedName("content") val content: String
)

data class ChatResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("reply") val reply: String
)
