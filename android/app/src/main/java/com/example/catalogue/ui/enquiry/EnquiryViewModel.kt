package com.example.catalogue.ui.enquiry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogue.data.CatalogueRepository
import com.example.catalogue.data.GENERIC_ERROR
import com.example.catalogue.data.model.EnquiryRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EnquiryFormState(
    val productTitle: String? = null,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val message: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val messageError: String? = null,
    val submitting: Boolean = false,
    val submitted: Boolean = false,
    val submitError: String? = null,
)

class EnquiryViewModel(
    private val repository: CatalogueRepository,
    private val productId: Int,
) : ViewModel() {
    private val _state = MutableStateFlow(EnquiryFormState())
    val state: StateFlow<EnquiryFormState> = _state.asStateFlow()

    init {
        // Best effort: the form still works if the title can't be loaded.
        viewModelScope.launch {
            repository.product(productId).onSuccess { product ->
                _state.update { it.copy(productTitle = product.title) }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, emailError = null) }
    fun onPhoneChange(value: String) = _state.update { it.copy(phone = value, phoneError = null) }
    fun onMessageChange(value: String) = _state.update { it.copy(message = value, messageError = null) }

    fun submit() {
        val current = _state.value
        if (current.submitting || current.submitted) return

        val nameError = EnquiryValidator.validateName(current.name)
        val emailError = EnquiryValidator.validateEmail(current.email)
        val phoneError = EnquiryValidator.validatePhone(current.phone)
        val messageError = EnquiryValidator.validateMessage(current.message)
        if (listOf(nameError, emailError, phoneError, messageError).any { it != null }) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    phoneError = phoneError,
                    messageError = messageError,
                )
            }
            return
        }

        _state.update { it.copy(submitting = true, submitError = null) }
        viewModelScope.launch {
            val result = repository.submitEnquiry(
                EnquiryRequest(
                    productId = productId,
                    name = current.name.trim(),
                    email = current.email.trim(),
                    phone = current.phone.trim().ifBlank { null },
                    message = current.message.trim().ifBlank { null },
                ),
            )
            _state.update { latest ->
                result.fold(
                    onSuccess = { latest.copy(submitting = false, submitted = true) },
                    onFailure = { latest.copy(submitting = false, submitError = it.message ?: GENERIC_ERROR) },
                )
            }
        }
    }
}
