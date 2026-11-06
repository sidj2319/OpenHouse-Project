package com.example.catalogue.data

import com.example.catalogue.data.model.CategoryResponse
import com.example.catalogue.data.model.EnquiryRequest
import com.example.catalogue.data.model.Product
import com.example.catalogue.data.remote.CatalogueApi
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

const val GENERIC_ERROR = "Something went wrong. Please try again."

/** Failure carrying a message that is safe to show to the customer. */
class CatalogueException(message: String) : Exception(message)

/**
 * What the UI layer depends on. ViewModels talk to this interface only, so the
 * transport (Retrofit today) and the backend can change without touching screens.
 */
interface CatalogueRepository {
    suspend fun home(): Result<List<Product>>
    suspend fun categories(): Result<List<String>>
    suspend fun category(name: String): Result<CategoryResponse>
    suspend fun search(query: String): Result<List<Product>>
    suspend fun product(id: Int): Result<Product>
    suspend fun submitEnquiry(request: EnquiryRequest): Result<Unit>
}

class NetworkCatalogueRepository(private val api: CatalogueApi) : CatalogueRepository {
    override suspend fun home() = safeCall { api.home().featured }
    override suspend fun categories() = safeCall { api.categories().categories }
    override suspend fun category(name: String) = safeCall { api.category(name) }
    override suspend fun search(query: String) = safeCall { api.search(query).products }
    override suspend fun product(id: Int) = safeCall { api.product(id).product }
    override suspend fun submitEnquiry(request: EnquiryRequest) = safeCall {
        api.submitEnquiry(request)
        Unit
    }
}

/** Maps transport failures to customer-friendly messages; never swallows cancellation. */
internal suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(CatalogueException("Can't reach the server. Check your connection and try again."))
    } catch (e: HttpException) {
        Result.failure(CatalogueException(messageForHttpStatus(e.code())))
    } catch (e: Exception) {
        Result.failure(CatalogueException(GENERIC_ERROR))
    }

internal fun messageForHttpStatus(code: Int): String = when (code) {
    400 -> "Please check the details and try again."
    404 -> "We couldn't find that."
    429 -> "Too many requests. Please wait a moment and try again."
    in 500..599 -> "The service is having trouble right now. Please try again shortly."
    else -> GENERIC_ERROR
}
