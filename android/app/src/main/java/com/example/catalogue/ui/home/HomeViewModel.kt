package com.example.catalogue.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogue.data.CatalogueRepository
import com.example.catalogue.data.GENERIC_ERROR
import com.example.catalogue.data.model.Product
import com.example.catalogue.ui.common.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeData(val featured: List<Product>, val categories: List<String>)

class HomeViewModel(private val repository: CatalogueRepository) : ViewModel() {
    private val _state = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val state: StateFlow<UiState<HomeData>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val (featured, categories) = coroutineScope {
                val f = async { repository.home() }
                val c = async { repository.categories() }
                f.await() to c.await()
            }
            _state.value = if (featured.isSuccess && categories.isSuccess) {
                UiState.Success(HomeData(featured.getOrThrow(), categories.getOrThrow()))
            } else {
                UiState.Error(
                    (featured.exceptionOrNull() ?: categories.exceptionOrNull())?.message
                        ?: GENERIC_ERROR,
                )
            }
        }
    }
}
