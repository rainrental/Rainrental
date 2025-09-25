package org.rainrental.rainrentalrfid.apis

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import retrofit2.HttpException
import retrofit2.Response
import org.rainrental.rainrentalrfid.logging.Logger
import org.rainrental.rainrentalrfid.result.ApiCallerApiError
import org.rainrental.rainrentalrfid.result.ApiError
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException
import org.rainrental.rainrentalrfid.result.Result


class ApiCaller @Inject constructor() : Logger {

    suspend operator fun <T : Any> invoke(apiCall: suspend () -> Response<T>): Result<T, ApiCallerApiError<T>> = withContext(Dispatchers.IO){
        val t = System.currentTimeMillis()

        val logName = apiCall.javaClass.name.split(".").last().split("$").subList(0,2).joinToString(".")
        logd("[${this.hashCode()}] Making request $logName")

        val returnValue =  try {
            val response = apiCall()
            val d = System.currentTimeMillis() - t
            logd("[${this.hashCode()}] Finished in ${d}ms")
            if (response.isSuccessful) {
                // Handle the case where the API response is successful but may be empty
                response.body()?.let {
                    Result.Success(data = it)
                } ?: Result.Error(ApiCallerApiError(apiErrorType = ApiError.EmptyResponse))
            }
            else {
                val responseErrorString = response.errorBody()?.string()
                loge("response !isSuccessful[${response.code()}]: $responseErrorString")

                val errorJson = responseErrorString?.let{
                    val json = JsonParserUseCase().invoke(it)
                    json
                }

                when (response.code()){
                    400 -> Result.Error(ApiCallerApiError(apiErrorType = ApiError.BadRequest, message = response.message(), response = response, errorJson = errorJson, errorString = responseErrorString))
                    401 -> {Result.Error(ApiCallerApiError(apiErrorType = ApiError.AuthenticationError, message = response.message(), response = response, errorJson = errorJson, errorString = responseErrorString)) }
                    404 -> Result.Error(ApiCallerApiError(apiErrorType = ApiError.ResourceNotFound, message = response.message(), response = response, errorJson = errorJson, errorString = responseErrorString))
                    409 -> Result.Error(ApiCallerApiError(apiErrorType = ApiError.Conflict409, message = response.message(), response = response, errorJson = errorJson, errorString = responseErrorString))
                    410 -> Result.Error(ApiCallerApiError(apiErrorType = ApiError.TagDeleted, message = response.message(), response = response, errorJson = errorJson, errorString = responseErrorString))
                    else -> {
                        loge("Unknown HTTP Error ${response.code()} ${response.toString()}")
                        Result.Error(ApiCallerApiError(apiErrorType = ApiError.UnknownHttpError, message = response.message(), response = response))
                    }
                }
            }
        }



        catch(ioException:IOException){
            loge("ioException: ${ioException.message.toString()}")
            loge("ioException: ${ioException.javaClass.simpleName}")
            when(ioException){
                is SocketTimeoutException -> Result.Error(ApiCallerApiError(apiErrorType = ApiError.Timeout))
                is UnknownHostException -> Result.Error(ApiCallerApiError(apiErrorType = ApiError.NoInternet))
                is SSLHandshakeException -> Result.Error(ApiCallerApiError(apiErrorType = ApiError.CertificateError))
                else -> Result.Error(ApiCallerApiError(apiErrorType = ApiError.UnknownException))
            }
        }

        catch (cancellationException:CancellationException){
            logd("Cancellation Exception: ${cancellationException.message.toString()}")
            Result.Error(ApiCallerApiError(apiErrorType = ApiError.UnknownException))
        }
        catch(httpError: HttpException){
            throw(Exception("API CALLER CAUGHT HttpException"))
        }
        catch (e:Exception){
            loge("Exception: ${e.message.toString()}")
            Result.Error(ApiCallerApiError(apiErrorType = ApiError.UnknownException))
        }

        val dur = System.currentTimeMillis() - t

        logd("[${this.hashCode()}] Return ${returnValue.javaClass.simpleName} for endpoint [${logName}] in ${dur}ms")

        return@withContext returnValue
    }
}