package org.rainrental.rainrentalrfid.inputmanager.domain.use_case

import org.rainrental.rainrentalrfid.result.BarcodeValidationError
import org.rainrental.rainrentalrfid.result.Result
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


//TODO Change the validation logic depending on company usage
class ValidateBarcode @Inject constructor(

)  {
    operator fun invoke(barcode:String) : Result<String, BarcodeValidationError> {
        if (barcode.isBlank()){
            return Result.Error(BarcodeValidationError.NoBarcodeDetected)
        }

        val containsNonAlphaNumeric = NonAlphaNumRegexChecker.containsNonAlphanumeric(barcode)
        if (containsNonAlphaNumeric){
            return Result.Error(BarcodeValidationError.ContainsSpecialChars)
        }
        if (barcode.length < 10){
            if (barcode.length == 8){
                return Result.Success(barcode)
            }
            return Result.Error(BarcodeValidationError.TooShort)
        }
        if (barcode.length > 10){
            return Result.Error(BarcodeValidationError.TooLong)
        }

        return Result.Success(barcode)
    }
}


object NonAlphaNumRegexChecker {
    private val PATTERN_NON_ALPHNUM: Pattern =
        Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}]")

    fun containsNonAlphanumeric(input: String?): Boolean {
        return input?.let{
            val matcher: Matcher = PATTERN_NON_ALPHNUM.matcher(input)
            matcher.find()
        }?:false
    }
}

fun String.isValidBarcode():Boolean {
    val result = ValidateBarcode().invoke(this)
    return when (result){
        is Result.Error -> false
        is Result.Success -> true
    }
}