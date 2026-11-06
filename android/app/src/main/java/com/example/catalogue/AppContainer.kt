package com.example.catalogue

import com.example.catalogue.data.CatalogueRepository
import com.example.catalogue.data.NetworkCatalogueRepository
import com.example.catalogue.data.remote.CatalogueApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Manual dependency wiring. Swap for Hilt/Koin if the graph grows. */
class AppContainer(baseUrl: String = BuildConfig.API_BASE_URL) {
    private val json = Json { ignoreUnknownKeys = true }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val api: CatalogueApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(CatalogueApi::class.java)

    val repository: CatalogueRepository = NetworkCatalogueRepository(api)
}
