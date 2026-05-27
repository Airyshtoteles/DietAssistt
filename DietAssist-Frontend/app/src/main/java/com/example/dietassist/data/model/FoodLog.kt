package com.example.dietassist.data.model

import com.google.gson.annotations.SerializedName

data class FoodLog(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("calories") val calories: Float,
    @SerializedName("protein") val protein: Float,
    @SerializedName("carbs") val carbs: Float,
    @SerializedName("fats") val fats: Float,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("image") val image: String? = null
)
