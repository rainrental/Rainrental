package org.rainrental.rainrentalrfid.result

enum class InputError : Error {
    NoBarcode,
    NoRfidTag,
    InvalidRfidTag,
    HardwareError,
    WriteEpcError,
    Busy,
    ValidationError,
    TagAlreadyInUse,
    FormatError,
    WaitingForHardware,
    WrongImplementation,
    LifecycleError,
    HardwareTimeout
}