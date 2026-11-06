package com.example.catalogue

import com.example.catalogue.data.CatalogueException
import com.example.catalogue.data.GENERIC_ERROR
import com.example.catalogue.data.messageForHttpStatus
import com.example.catalogue.data.safeCall
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class RepositoryErrorMappingTest {
    @Test
    fun `success is passed through`() = runBlocking {
        assertEquals(42, safeCall { 42 }.getOrThrow())
    }

    @Test
    fun `network failures become a friendly connectivity message`() = runBlocking {
        val result = safeCall<Unit> { throw IOException("socket closed") }
        val error = result.exceptionOrNull()
        assertTrue(error is CatalogueException)
        assertTrue(error!!.message!!.contains("Can't reach the server"))
    }

    @Test
    fun `unexpected exceptions never leak their message to the customer`() = runBlocking {
        val result = safeCall<Unit> { error("secret internals") }
        assertEquals(GENERIC_ERROR, result.exceptionOrNull()?.message)
    }

    @Test
    fun `cancellation is rethrown, not swallowed`() = runBlocking {
        try {
            safeCall<Unit> { throw CancellationException("cancelled") }
            fail("expected CancellationException")
        } catch (e: CancellationException) {
            // expected
        }
    }

    @Test
    fun `http statuses map to messages`() {
        assertEquals("We couldn't find that.", messageForHttpStatus(404))
        assertEquals("Please check the details and try again.", messageForHttpStatus(400))
        assertTrue(messageForHttpStatus(502).contains("trouble"))
        assertEquals(GENERIC_ERROR, messageForHttpStatus(418))
    }
}
