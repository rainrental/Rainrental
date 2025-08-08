package org.rainrental.rainrentalrfid.rainrental.data.model

data class EncodeEpcRequestDto(
    val companyId: Int,
    val data: List<EpcMapping>
)