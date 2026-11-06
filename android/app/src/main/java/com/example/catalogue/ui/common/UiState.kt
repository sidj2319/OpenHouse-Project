package com.example.catalogue.ui.common

import com.example.catalogue.data.GENERIC_ERROR

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
}

fun <T> Result<T>.toUiState(): UiState<T> =
    fold(
        onSuccess = { UiState.Success(it) },
        onFailure = { UiState.Error(it.message ?: GENERIC_ERROR) },
    )
