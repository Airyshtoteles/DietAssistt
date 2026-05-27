package com.example.dietassist.data.model

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("daily_calorie_target") val dailyCalorieTarget: Int = 2000,
    @SerializedName("weight") val weight: Float = 60.0f,
    @SerializedName("height") val height: Float = 165.0f,
    @SerializedName("updated_at") val updatedAt: String? = null
)
