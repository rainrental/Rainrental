package org.rainrental.rainrentalrfid.rainrental.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import org.rainrental.rainrentalrfid.rainrental.data.model.DecodeEpcRequestDto
import org.rainrental.rainrentalrfid.rainrental.data.model.DecodeEpcResponseDto
import org.rainrental.rainrentalrfid.rainrental.data.model.EncodeEpcRequestDto
import org.rainrental.rainrentalrfid.rainrental.data.model.EncodeEpcResponseDto

interface RainRentalApi {
    @POST("api/v1/decode")
    suspend fun decodeEpc(@Body decodeEpcRequestDto: DecodeEpcRequestDto) : Response<DecodeEpcResponseDto>

    @POST("api/v1/encode")
    suspend fun encodeEpc(@Body encodeEpcRequestDto: EncodeEpcRequestDto) : Response<EncodeEpcResponseDto>
}
