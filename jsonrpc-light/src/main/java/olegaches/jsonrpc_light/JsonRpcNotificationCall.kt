package olegaches.jsonrpc_light

import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class JsonRpcNotificationCall(
    private val okHttpCall: okhttp3.Call,
) : NotificationCall {

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

    override fun cancel() {
        if (cancelled) return
        cancelled = true
        okHttpCall.cancel()
    }

    override fun enqueue(callback: NotificationCallback) {
        synchronized(executionLock) {
            check(!executed) { "Already executed." }
            executed = true
        }
        if (cancelled) {
            throw IOException("Canceled")
        }
        okHttpCall.enqueue(
            object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    callback.onFailure(this@JsonRpcNotificationCall, e)
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    if (!response.isSuccessful) {
                        callback.onFailure(
                            this@JsonRpcNotificationCall,
                            TransportException(
                                httpCode = response.code,
                                message = "HTTP ${response.code}. ${response.message}",
                                response = response,
                            )
                        )
                    } else {
                        callback.onResponse(this@JsonRpcNotificationCall)
                    }
                }
            }
        )
    }

    override fun execute() {
        synchronized(executionLock) {
            check(!executed) { "Already executed." }
            executed = true
        }
        if (cancelled) {
            throw IOException("Canceled")
        }
        val response = okHttpCall.execute()
        if (!response.isSuccessful) {
            throw TransportException(
                httpCode = response.code,
                message = "HTTP ${response.code}. ${response.message}",
                response = response,
            )
        }
    }
}