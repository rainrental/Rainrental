package org.rainrental.rainrentalrfid.result

import kotlinx.serialization.json.JsonObject
import retrofit2.Response

enum class ApiError : Error, GeneralError {
    UnknownError,
    UnknownHttpError,
    UnknownException,
    ResourceNotFound,
    EmptyResponse,
    APIError,
    Busy,
    AuthenticationError,
    NoInternet,
    CertificateError,
    Timeout,
    BadRequest,
    Conflict409,
    TagDeleted
}

data class ApiCallerApiError<T>(val apiErrorType: ApiError, val message: String? = null, val response: Response<T>? = null, val errorString: String? = null, val errorJson: JsonObject? = null) : Error,
    GeneralError