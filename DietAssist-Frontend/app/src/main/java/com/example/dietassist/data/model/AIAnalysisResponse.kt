package com.example.dietassist.data.model

import com.google.gson.annotations.SerializedName

data class AIAnalysisResult(
    @SerializedName("food_name") val foodName: String,
    @SerializedName("calories") val calories: Float,
    @SerializedName("protein") val protein: Float,
    @SerializedName("carbs") val carbs: Float,
    @SerializedName("fats") val fats: Float,
    @SerializedName("is_nutrition_label") val isNutritionLabel: Boolean,
    @SerializedName("confidence_score") val confidenceScore: Float
)

data class AIAnalysisResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("analysis") val analysis: AIAnalysisResult
)
