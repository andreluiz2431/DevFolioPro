package com.example.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubRepo(
    val id: Long,
    val name: String,
    val description: String?,
    @Json(name = "stargazers_count") val stargazersCount: Int,
    val language: String?,
    @Json(name = "html_url") val htmlUrl: String
)
