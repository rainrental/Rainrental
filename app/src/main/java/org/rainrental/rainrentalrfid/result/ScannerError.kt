package org.rainrental.rainrentalrfid.result


enum class ScannerError : Error {
    NotInitialised,
    NoBarcode,
    TooLong,
    WrongFormat,
    UnknownError
}