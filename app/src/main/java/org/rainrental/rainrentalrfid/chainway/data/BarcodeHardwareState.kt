package org.rainrental.rainrentalrfid.chainway.data

enum class BarcodeHardwareState{
    Startup,
    Initialising,
    Ready,
    Sleeping,
    Busy,
    Error,
    TimedOut
}