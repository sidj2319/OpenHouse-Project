package com.example.catalogue.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogue.data.CatalogueRepository
import com.example.catalogue.data.model.CategoryResponse
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.common.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CatalogueRepository,
    private val categoryName: String,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<CategoryResponse>>(UiState.Loading)
    val state: StateFlow<UiState<CategoryResponse>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = repository.category(categoryName).toUiState()
        }
    }
}
