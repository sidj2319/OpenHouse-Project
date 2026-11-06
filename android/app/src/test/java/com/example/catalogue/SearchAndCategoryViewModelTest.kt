package com.example.catalogue

import com.example.catalogue.data.model.CategoryResponse
import com.example.catalogue.ui.category.CategoryViewModel
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.search.SearchViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchAndCategoryViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `search sends the query and exposes the results`() {
        val repo = FakeCatalogueRepository().apply {
            searchResult = Result.success(listOf(product(2), product(3)))
        }

        val state = SearchViewModel(repo, "backpack").state.value

        assertEquals("backpack", repo.lastSearchQuery)
        assertEquals(UiState.Success(listOf(product(2), product(3))), state)
    }

    @Test
    fun `search with no matches is a successful empty result`() {
        val repo = FakeCatalogueRepository()

        val state = SearchViewModel(repo, "zeppelin").state.value

        assertEquals(UiState.Success(emptyList<Nothing>()), state)
    }

    @Test
    fun `category exposes top rated and additional products`() {
        val listing = CategoryResponse("bags", topRated = listOf(product(1)), others = listOf(product(2)))
        val repo = FakeCatalogueRepository().apply { categoryResult = Result.success(listing) }

        val state = CategoryViewModel(repo, "bags").state.value

        assertEquals(UiState.Success(listing), state)
    }
}
