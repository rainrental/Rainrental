package org.rainrental.rainrentalrfid.rainrental.data.model

data class DecodeEpcResponseDto(
    val companyId: Int,
    val companyRainId: String,
    val companyName: String,
    val companyRainUrl: String? = null,
    val companyUrl: String? = null,
    val success: Boolean,
    val message: String,
    val epcHexString: String,
)