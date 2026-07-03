package com.example.shared

data class SharedProfile(
    val name: String,
    val role: String,
    val bio: String,
    val skills: List<String>
)

object SharedConstants {
    const val APP_VERSION = "1.0.0"
    const val PLATFORM_ANDROID = "Android"
    const val PLATFORM_WEB = "Web"
}
