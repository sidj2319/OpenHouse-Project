package com.example.catalogue

import com.example.catalogue.data.CatalogueException
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.home.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `shows featured products and categories`() {
        val repo = FakeCatalogueRepository().apply {
            homeResult = Result.success((1..5).map { product(it) })
            categoriesResult = Result.success(listOf("bags", "jewelery"))
        }

        val state = HomeViewModel(repo).state.value

        assertTrue(state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(5, data.featured.size)
        assertEquals(listOf("bags", "jewelery"), data.categories)
    }

    @Test
    fun `shows the error message when loading fails`() {
        val repo = FakeCatalogueRepository().apply {
            homeResult = Result.failure(CatalogueException("Can't reach the server."))
        }

        val state = HomeViewModel(repo).state.value

        assertEquals(UiState.Error("Can't reach the server."), state)
    }

    @Test
    fun `retry recovers after a failure`() {
        val repo = FakeCatalogueRepository().apply {
            homeResult = Result.failure(CatalogueException("boom"))
        }
        val viewModel = HomeViewModel(repo)
        assertTrue(viewModel.state.value is UiState.Error)

        repo.homeResult = Result.success(listOf(product(1)))
        viewModel.load()

        assertTrue(viewModel.state.value is UiState.Success)
    }
}
