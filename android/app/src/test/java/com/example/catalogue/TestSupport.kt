package com.example.catalogue

import com.example.catalogue.data.CatalogueRepository
import com.example.catalogue.data.model.CategoryResponse
import com.example.catalogue.data.model.EnquiryRequest
import com.example.catalogue.data.model.Product
import com.example.catalogue.data.model.Rating
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

fun product(id: Int, title: String = "Product $id") = Product(
    id = id,
    title = title,
    price = 9.99,
    description = "Description $id",
    category = "bags",
    image = "https://img.test/$id.png",
    rating = Rating(4.0, 10),
)

/** Replaces Dispatchers.Main so viewModelScope runs eagerly in unit tests. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeCatalogueRepository : CatalogueRepository {
    var homeResult: Result<List<Product>> = Result.success(emptyList())
    var categoriesResult: Result<List<String>> = Result.success(emptyList())
    var categoryResult: Result<CategoryResponse> = Result.success(CategoryResponse("bags", emptyList(), emptyList()))
    var searchResult: Result<List<Product>> = Result.success(emptyList())
    var productResult: Result<Product> = Result.success(product(1))
    var enquiryResult: Result<Unit> = Result.success(Unit)

    var lastSearchQuery: String? = null
    val submittedEnquiries = mutableListOf<EnquiryRequest>()

    override suspend fun home() = homeResult
    override suspend fun categories() = categoriesResult
    override suspend fun category(name: String) = categoryResult
    override suspend fun search(query: String): Result<List<Product>> {
        lastSearchQuery = query
        return searchResult
    }

    override suspend fun product(id: Int) = productResult
    override suspend fun submitEnquiry(request: EnquiryRequest): Result<Unit> {
        submittedEnquiries += request
        return enquiryResult
    }
}
