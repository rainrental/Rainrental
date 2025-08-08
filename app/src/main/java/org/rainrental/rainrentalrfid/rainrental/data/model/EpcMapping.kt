package org.rainrental.rainrentalrfid.rainrental.data.model

data class EpcMapping(
    val startBit: Int,
    val lookupString:String?,   //  When using the lookup service on rain rental
    val id: Int?                //  When using a literal id
){
    val isValid:Boolean
        get() = (lookupString != null || id != null) && !(lookupString !=null && id != null)
}