package org.example.frontend.progresstracking

import com.google.gson.annotations.SerializedName

data class DyslexiaError(
    @SerializedName("source_category")
    val sourceCategory: String = "",

    @SerializedName("level_title")
    val levelTitle: String = "",

    @SerializedName("error_concepts")
    val errorConcepts: List<String> = emptyList()
)