package org.rainrental.rainrentalrfid

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.rainrental.rainrentalrfid.regex.RegexManager

class RegexManagerUnitTests {

    @Test
    fun `validate valid regex and matching input`() = runTest {
        val input = "AbC1234567"
        val regexString = "^[a-zA-Z0-9]{8,10}$"
        val result = RegexManager.validateWithTimeout(input, regexString, timeoutMillis = 1000L)
        assertTrue("Expected input to match the regex", result)
    }

    @Test
    fun `validate valid regex and non-matching input`() = runTest {
        val input = "AbC1234" // Too short
        val regexString = "^[a-zA-Z0-9]{8,10}$"
        val result = RegexManager.validateWithTimeout(input, regexString, timeoutMillis = 1000L)
        assertFalse("Expected input to not match the regex", result)
    }

    @Test
    fun `validate invalid regex pattern`() = runTest {
        val input = "AbC1234567"
        val regexString = "[a-zA-Z{8,10}" // Missing closing bracket
        val result = RegexManager.validateWithTimeout(input, regexString, timeoutMillis = 1000L)
        assertFalse("Expected invalid regex pattern to return false", result)
    }

    @Test
    fun `validate regex sanitisation failure`() = runTest {
        val input = "AbC1234567"
        val regexString = "^[a-zA-Z0-9]{8,10}$+" // Contains disallowed `+` at the end
        val result = RegexManager.validateWithTimeout(input, regexString, timeoutMillis = 1000L)
        assertFalse("Expected sanitisation failure to return false", result)
    }

    @Test
    fun `validate timeout for regex matching`() = runTest {
        val input = "AbC1234567"
        val regexString = "^[a-zA-Z0-9]{8,10}$"
        val result = RegexManager.validateWithTimeout(input, regexString, timeoutMillis = 1L) // Very short timeout
        assertFalse("Expected timeout to return false", result)
    }

    @Test
    fun `validate regex matches edge case`() = runTest {
        val input = "12345678"
        val regexString = "^[0-9]{8,10}$"
        val result = RegexManager.validateWithTimeout(input, regexString, timeoutMillis = 1000L)
        assertTrue("Expected input to match the numeric regex", result)
    }

    @Test
    fun `validate regex does not match when sanitisation passes`() = runTest {
        val input = "special@char"
        val regexString = "^[a-zA-Z0-9]{8,10}$" // Regex expects no special characters
        val result = RegexManager.validateWithTimeout(input, regexString, timeoutMillis = 1000L)
        assertFalse("Expected sanitisation to pass but matching to fail", result)
    }
}