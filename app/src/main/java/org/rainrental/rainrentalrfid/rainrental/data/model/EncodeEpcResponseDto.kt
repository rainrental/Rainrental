package org.rainrental.rainrentalrfid.rainrental.data.model

data class EncodeEpcResponseDto(
    val success:Boolean,
    val message: String,
    val data: List<EpcDecoding>,
    val epcHexString: String,
    val epcBase64: String
)