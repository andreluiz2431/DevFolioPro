package com.example.data.remote

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class LicenseCheckResponse(
    val uid: String,
    val unlocked: Boolean,
    val licenseType: String?,
    val licenseCode: String?,
    val updatedAt: String?,
    val paymentId: String?
)

interface LicenseApi {
    @GET("api/licenses/{uid}")
    suspend fun checkLicense(@Path("uid") uid: String): LicenseCheckResponse
}

object LicenseClient {
    private const val BASE_URL = "https://devfoliopro-production.up.railway.app/"

    val api: LicenseApi by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = com.squareup.moshi.Moshi.Builder()
            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LicenseApi::class.java)
    }
}
