package olegaches.jsonrpc_light

import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type

class RpcCall<T>(
    private val okHttpCall: okhttp3.Call,
    private val responseBodyParser: JsonRpcResponseBodyConverter,
    private val returnType: Type
): Call<T> {
    @Volatile
    private var cancelled = false

    private var executed = false

    private val executionLock = Any()

    @Synchronized
    override fun request(): Request {
        return try {
            okHttpCall.request()
        } catch (e: IOException) {
            throw RuntimeException("Unable to create request.", e)
        }
    }

    override fun execute(): JsonRpcResponse<T> {
        synchronized(executionLock) {
            check(!executed) { "Already executed." }
            executed = true
        }
        if(cancelled) {
            throw IOException("Canceled")
        }
        val response = okHttpCall.execute()
        val responseBody = response.body
        return if(responseBody != null) {
            responseBodyParser.convert(responseBody.bytes(), returnType)
        } else if(response.isSuccessful) {
            throw IllegalStateException("Response body is null")
        } else {
            throw TransportException(
                httpCode = response.code,
                message = "HTTP ${response.code}. ${response.message}",
                response = response,
            )
        }
    }

    override fun cancel() {
        if(cancelled) return
        cancelled = true
        okHttpCall.cancel()
    }

    override fun enqueue(callback: Callback<T>) {
        synchronized(executionLock) {
            check(!executed) { "Already executed." }
            executed = true
        }
        if(cancelled) {
            return callback.onFailure(this, IOException("Canceled"))
        }
        okHttpCall.enqueue(
            object: okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    callback.onFailure(this@RpcCall, e)
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    val responseBody = response.body
                    return if(responseBody != null) {
                        callback.onResponse(this@RpcCall, responseBodyParser.convert(responseBody.bytes(), returnType))
                    } else if(response.isSuccessful) {
                        throw IllegalStateException("Response body is null")
                    } else {
                        callback.onFailure(
                            call = this@RpcCall,
                            t = TransportException(
                                httpCode = response.code,
                                message = "HTTP ${response.code}. ${response.message}",
                                response = response,
                            )
                        )
                    }
                }
            }
        )
    }
}