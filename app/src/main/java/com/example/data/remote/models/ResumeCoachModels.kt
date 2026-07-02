package com.example.data.remote.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResumeImprovements(
    val improvedProfile: ImprovedProfileSuggestion?,
    val recommendedSkills: List<RecommendedSkillSuggestion>?,
    val improvedExperiences: List<ImprovedExperienceSuggestion>?
)

@JsonClass(generateAdapter = true)
data class ImprovedProfileSuggestion(
    val role: String?,
    val bio: String?
)

@JsonClass(generateAdapter = true)
data class RecommendedSkillSuggestion(
    val name: String?,
    val category: String?, // "Desenvolvimento" or "Infraestrutura"
    val justification: String?
)

@JsonClass(generateAdapter = true)
data class ImprovedExperienceSuggestion(
    val company: String?,
    val role: String?,
    val period: String?,
    val improvedDescription: String?
)
