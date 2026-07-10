package com.example.data.remote

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class PaymentResult(
    val id: Long?,
    val status: String?,
    val external_reference: String?,
    val status_detail: String?,
    val date_approved: String? = null
)

@JsonClass(generateAdapter = true)
data class PaymentSearchResponse(
    val results: List<PaymentResult>?
)

@JsonClass(generateAdapter = true)
data class PreferenceItem(
    val title: String,
    val quantity: Int,
    val unit_price: Double,
    val currency_id: String
)

@JsonClass(generateAdapter = true)
data class PreferencePayer(
    val email: String
)

@JsonClass(generateAdapter = true)
data class PreferenceBackUrls(
    val success: String,
    val pending: String,
    val failure: String
)

@JsonClass(generateAdapter = true)
data class PreferenceRequest(
    val items: List<PreferenceItem>,
    val payer: PreferencePayer,
    val back_urls: PreferenceBackUrls,
    val auto_return: String,
    val external_reference: String? = null
)

@JsonClass(generateAdapter = true)
data class PreferenceResponse(
    val id: String?,
    val init_point: String?,
    val sandbox_init_point: String?
)

interface MercadoPagoApi {
    @POST("checkout/preferences")
    suspend fun createPreference(
        @Header("Authorization") authorization: String,
        @Body request: PreferenceRequest
    ): PreferenceResponse

    @GET("v1/payments/search")
    suspend fun searchPayments(
        @Header("Authorization") authorization: String,
        @Query("external_reference") externalReference: String?,
        @Query("payer.email") payerEmail: String? = null
    ): PaymentSearchResponse
}

object MercadoPagoClient {
    private const val BASE_URL = "https://api.mercadopago.com/"

    val api: MercadoPagoApi by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        val moshi = com.squareup.moshi.Moshi.Builder()
            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MercadoPagoApi::class.java)
    }
}
