package org.rainrental.rainrentalrfid.chainway.data

import com.rscja.deviceapi.entity.UHFTAGInfo

data class TagWithOrientation(
    val tag: UHFTAGInfo,
    val timestamp: Long,
    val orientation: Orientation
)

data class Orientation(
    val azimuth: Float, // Rotation angle around the z-axis
    val pitch: Float, // Tilt angle
    val roll: Float // Side-to-side angle
)