package com.example.catalogue.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogue.data.CatalogueRepository
import com.example.catalogue.data.model.Product
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.common.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: CatalogueRepository,
    private val productId: Int,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<Product>>(UiState.Loading)
    val state: StateFlow<UiState<Product>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = repository.product(productId).toUiState()
        }
    }
}
