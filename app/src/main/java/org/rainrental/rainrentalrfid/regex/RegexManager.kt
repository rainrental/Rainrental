package org.rainrental.rainrentalrfid.regex

import kotlinx.coroutines.*
import java.util.regex.PatternSyntaxException

object RegexManager {

    suspend fun validateWithTimeout(input: String, regexString: String, timeoutMillis: Long): Boolean {
        return try {
            withTimeout(timeoutMillis) {
                val sanitisedRegex = sanitiseRegex(regexString) ?: throw SanitisationException()
                val regex = Regex(sanitisedRegex)
                regex.matches(input)
            }
        }catch (e:SanitisationException){
            println("Sanitisation failed")
            false
        }
        catch (e: TimeoutCancellationException) {
            println("Regex validation timed out")
            false
        } catch (e: PatternSyntaxException) {
            println("Invalid regex pattern")
            false
        }
    }

    private fun sanitiseRegex(regexString: String): String? {
        val allowedCharacters = Regex("^[\\w\\[\\]{}()^$.|*+?\\\\,-]+$")
        val result = if (regexString.matches(allowedCharacters)) regexString else null
        println("Sanitised regex: $result") // Debug print
        return result
    }
}

class SanitisationException: Exception()