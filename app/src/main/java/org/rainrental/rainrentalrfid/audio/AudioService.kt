package org.rainrental.rainrentalrfid.audio

import org.rainrental.rainrentalrfid.logging.Logger


interface AudioService : Logger {

    fun playSuccess()

    fun playWarning()

    fun playError()

    fun cleanupAudioService()
}