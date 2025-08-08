package org.rainrental.rainrentalrfid.result



enum class BarcodeValidationError: Error {
    NoBarcodeDetected,
    TooShort,
    TooLong,
    ContainsSpecialChars,
    WrongFormat,
    OtherError
}