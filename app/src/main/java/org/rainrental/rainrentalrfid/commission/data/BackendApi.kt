package org.rainrental.rainrentalrfid.commission.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import org.rainrental.rainrentalrfid.unified.data.AssetDetailsResponseDto


interface BackendApi {
    @POST("commissionTags")
    suspend fun commissionTag(@Body commissionTagRequestDto: CommissionTagRequestDto): Response<CommissionTagResponseDto>

//    @GET("getAsset/{barcode}")
//    suspend fun getAssetDetails(@Path("barcode") barcode: String) : Response<AssetDetailsResponseDto>

//    @GET("getAsset")
//    suspend fun getAssetDetails(@QueryMap getAssetRequest: Map<String, String>) : Response<AssetDetailsResponseDto>
    @POST("getAsset")
    suspend fun getAssetDetails(@Body getAssetRequest: GetAssetRequestDto) : Response<AssetDetailsResponseDto>

    @POST("isTagAvailable")
    suspend fun isTagAvailable(@Body isTagAvailableRequestDto: IsTagAvailableRequestDto): Response<IsTagAvailableResponseDto>


    @POST("logInventory")
    suspend fun logInventory(@Body logInventoryRequestDto: LogInventoryRequestDto) : Response<LogInventoryResponseDto>

    @POST("getAssetByTid")
    suspend fun getAssetByTid(@Body getAssetByTidRequest: GetAssetByTidRequestDto) : Response<AssetDetailsResponseDto>

    @POST("checkInBarcode")
    suspend fun checkInBarcode(@Body checkInBarcodeRequest: CheckInBarcodeRequestDto) : Response<CheckInBarcodeResponseDto>

    @POST("deleteTag")
    suspend fun deleteTag(@Body deleteTagRequest: DeleteTagRequestDto) : Response<DeleteTagResponseDto>

    @POST("appVersions")
    suspend fun getAppVersions(@Body appVersionsRequest: AppVersionsRequestDto) : Response<AppVersionsResponseDto>
}

data class GetAssetRequestDto(
    val barcode: String,
    val companyId: String
)

data class GetAssetByTidRequestDto(
    val tidHex: String,
    val companyId: String
)

data class CheckInBarcodeRequestDto(
    val barcode: String,
    val companyId: String,
    val location: String
)

data class CheckInBarcodeResponseDto(
    val success: Boolean,
    val message: String,
    val asset: AssetDetailsResponseDto? = null
)

data class IsTagAvailableRequestDto(
    val tidHex: String,
    val companyId:String
)

data class IsTagAvailableResponseDto(
    val success: Boolean,
    val message: String,
)

data class LogInventoryRequestDto(
    val epcFilter: String,
    val sku: String,
    val skuId: Int,
    val department: String,
    val departmentId: Int,
    val companyId: String,
    val admin: String,
    val inventory: List<InventoryRecord>
)

data class InventoryRecord(
    val tidHex: String,
    val epcHex: String,
)

data class LogInventoryResponseDto(
    val success: Boolean,
    val message: String,
    val validCount: Int? = null,
    val invalidCount: Int? = null
)

data class CommissionTagRequestDto(
    val barcode: String,
    val companyId: String,
    val tags: List<TagData>
)

data class TagData(
    val tidHex: String,
    val epcHex: String
)

data class CommissionTagResponseDto(
    val success: Boolean,
    val message: String,
){
    companion object {
        fun example(): CommissionTagResponseDto{
            return CommissionTagResponseDto(
                success = true,
                message = "Tags added successfully"
            )
        }
    }
}

data class DeleteTagRequestDto(
    val barcode: String,
    val tidHex: String,
    val companyId: String
)

data class DeleteTagResponseDto(
    val success: Boolean,
    val message: String
)

data class AppVersionsRequestDto(
    val companyId: String
)

data class AppVersionsResponseDto(
    val success: Boolean,
    val message: String,
    val versions: List<AppVersionDto>
)

data class AppVersionDto(
    val version: String,
    val versionCode: Int,
    val downloadUrl: String,
    val fileSize: Long,
    val releaseNotes: String = "",
    val minSdkVersion: Int = 30,
    val targetSdkVersion: Int = 34
)