package com.example.catalogue.ui.enquiry

/**
 * Client-side validation for fast feedback. The backend re-validates everything
 * (never trust the client); keep these rules in sync with backend/src/app.ts.
 */
object EnquiryValidator {
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val PHONE = Regex("^[+0-9()\\-\\s]{5,30}$")

    const val MAX_NAME = 100
    const val MAX_MESSAGE = 1000

    fun validateName(value: String): String? = when {
        value.isBlank() -> "Please enter your name"
        value.trim().length > MAX_NAME -> "Name is too long"
        else -> null
    }

    fun validateEmail(value: String): String? = when {
        value.isBlank() -> "Please enter your email"
        !EMAIL.matches(value.trim()) -> "Please enter a valid email address"
        else -> null
    }

    /** Phone is optional. */
    fun validatePhone(value: String): String? = when {
        value.isBlank() -> null
        !PHONE.matches(value.trim()) -> "Please enter a valid phone number"
        else -> null
    }

    fun validateMessage(value: String): String? =
        if (value.trim().length > MAX_MESSAGE) "Message must be $MAX_MESSAGE characters or fewer" else null
}
