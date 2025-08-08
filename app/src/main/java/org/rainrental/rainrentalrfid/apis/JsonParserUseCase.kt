package org.rainrental.rainrentalrfid.apis

import com.google.gson.JsonParseException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.rainrental.rainrentalrfid.logging.Logger
import javax.inject.Inject

class JsonParserUseCase @Inject constructor(
) : Logger {
    operator fun invoke(jsonString: String): JsonObject? {
        val json = Json { ignoreUnknownKeys = false }
        return try{
            val element = json.parseToJsonElement(jsonString)
            element.jsonObject
        }catch (e:JsonParseException){
            loge("JSON Parsing error: ${e.message}")
            null
        }catch (e:Exception){
            loge("JSON Parsing Other exception: ${e.message}")
            null
        }
    }
}