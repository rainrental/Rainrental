package org.rainrental.rainrentalrfid.unified.data

data class AssetDetailsResponseDto(
    val barcode: String = "",
    val productName: String? = null,
    val epc: String = "",
    val sku: String = "",
    val skuId: Int = 0,
    val department: String = "",
    val departmentId: Int = 0,
    val serial: String = "",
    val serialId: String = "",
    val tags: List<TagInfo> = emptyList()
) {
    companion object {
        fun example(): AssetDetailsResponseDto {
            return AssetDetailsResponseDto(
                barcode = "123456789",
                productName = "Test product name here",
                epc = "E2003412010200000000000001",
                sku = "Sample SKU",
                skuId = 1,
                department = "Electronics",
                departmentId = 1,
                serial = "SERIAL123",
                serialId = "SER001",
                tags = listOf(
                    TagInfo(
                        tidHex = "E2003412010200000000000001",
                        epcHex = "E2003412010200000000000001"
                    )
                )
            )
        }
    }
}

data class TagInfo(
    val tidHex: String,
    val epcHex: String
) 