package com.example.catalogue.data.remote

import com.example.catalogue.data.model.CategoriesResponse
import com.example.catalogue.data.model.CategoryResponse
import com.example.catalogue.data.model.EnquiryAck
import com.example.catalogue.data.model.EnquiryRequest
import com.example.catalogue.data.model.HomeResponse
import com.example.catalogue.data.model.ProductResponse
import com.example.catalogue.data.model.SearchResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CatalogueApi {
    @GET("api/home")
    suspend fun home(): HomeResponse

    @GET("api/categories")
    suspend fun categories(): CategoriesResponse

    @GET("api/categories/{name}")
    suspend fun category(@Path("name") name: String): CategoryResponse

    @GET("api/products")
    suspend fun search(@Query("q") query: String): SearchResponse

    @GET("api/products/{id}")
    suspend fun product(@Path("id") id: Int): ProductResponse

    @POST("api/enquiries")
    suspend fun submitEnquiry(@Body body: EnquiryRequest): EnquiryAck
}
