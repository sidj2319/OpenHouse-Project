package com.example.catalogue.data.model

import kotlinx.serialization.Serializable

// Wire models: mirror the backend's JSON contract (see backend/src/app.ts).
// The app never sees the upstream product source's shape.

@Serializable
data class Rating(val rate: Double, val count: Int)

@Serializable
data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: Rating,
)

@Serializable
data class HomeResponse(val featured: List<Product>)

@Serializable
data class CategoriesResponse(val categories: List<String>)

@Serializable
data class CategoryResponse(
    val category: String,
    val topRated: List<Product>,
    val others: List<Product>,
)

@Serializable
data class SearchResponse(val query: String, val products: List<Product>)

@Serializable
data class ProductResponse(val product: Product)

@Serializable
data class EnquiryRequest(
    val productId: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val message: String? = null,
)

@Serializable
data class EnquiryAck(val ok: Boolean)
