package com.example.catalogue

import com.example.catalogue.data.CatalogueException
import com.example.catalogue.ui.enquiry.EnquiryValidator
import com.example.catalogue.ui.enquiry.EnquiryViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EnquiryValidatorTest {
    @Test
    fun `name is required`() {
        assertNotNull(EnquiryValidator.validateName("   "))
        assertNull(EnquiryValidator.validateName("Asha"))
    }

    @Test
    fun `email must look like an email`() {
        assertNotNull(EnquiryValidator.validateEmail(""))
        assertNotNull(EnquiryValidator.validateEmail("asha"))
        assertNotNull(EnquiryValidator.validateEmail("asha@"))
        assertNotNull(EnquiryValidator.validateEmail("a b@c.com"))
        assertNull(EnquiryValidator.validateEmail(" asha@example.com "))
    }

    @Test
    fun `phone is optional but validated when present`() {
        assertNull(EnquiryValidator.validatePhone(""))
        assertNull(EnquiryValidator.validatePhone("+91 98765-43210"))
        assertNotNull(EnquiryValidator.validatePhone("call me"))
    }

    @Test
    fun `message length is capped`() {
        assertNull(EnquiryValidator.validateMessage("x".repeat(1000)))
        assertNotNull(EnquiryValidator.validateMessage("x".repeat(1001)))
    }
}

class EnquiryViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun fill(vm: EnquiryViewModel) {
        vm.onNameChange("  Asha Rao ")
        vm.onEmailChange("asha@example.com")
        vm.onPhoneChange("")
        vm.onMessageChange("  ")
    }

    @Test
    fun `loads the product title for the form heading`() {
        val repo = FakeCatalogueRepository().apply { productResult = Result.success(product(7, "Red Backpack")) }
        assertEquals("Red Backpack", EnquiryViewModel(repo, 7).state.value.productTitle)
    }

    @Test
    fun `invalid form shows errors and does not call the backend`() {
        val repo = FakeCatalogueRepository()
        val vm = EnquiryViewModel(repo, 1)

        vm.submit()

        val state = vm.state.value
        assertNotNull(state.nameError)
        assertNotNull(state.emailError)
        assertTrue(repo.submittedEnquiries.isEmpty())
        assertFalse(state.submitted)
    }

    @Test
    fun `valid form submits trimmed values and shows thank you`() {
        val repo = FakeCatalogueRepository()
        val vm = EnquiryViewModel(repo, 3)
        fill(vm)

        vm.submit()

        assertTrue(vm.state.value.submitted)
        val sent = repo.submittedEnquiries.single()
        assertEquals(3, sent.productId)
        assertEquals("Asha Rao", sent.name)
        assertEquals("asha@example.com", sent.email)
        assertNull(sent.phone)
        assertNull(sent.message)
    }

    @Test
    fun `failed submission keeps the form and shows the error`() {
        val repo = FakeCatalogueRepository().apply {
            enquiryResult = Result.failure(CatalogueException("The service is having trouble right now."))
        }
        val vm = EnquiryViewModel(repo, 1)
        fill(vm)

        vm.submit()

        val state = vm.state.value
        assertFalse(state.submitted)
        assertFalse(state.submitting)
        assertEquals("The service is having trouble right now.", state.submitError)
        assertEquals("  Asha Rao ", state.name)
    }

    @Test
    fun `editing a field clears its error`() {
        val vm = EnquiryViewModel(FakeCatalogueRepository(), 1)
        vm.submit()
        assertNotNull(vm.state.value.nameError)

        vm.onNameChange("A")

        assertNull(vm.state.value.nameError)
    }
}
