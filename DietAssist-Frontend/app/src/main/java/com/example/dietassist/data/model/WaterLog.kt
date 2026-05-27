package com.example.dietassist.data.model

import com.google.gson.annotations.SerializedName

data class WaterLog(
    @SerializedName("id") val id: String? = null,
    @SerializedName("user_id") val userId: String,
    @SerializedName("amount_ml") val amountMl: Int,
    @SerializedName("created_at") val createdAt: String? = null
)
