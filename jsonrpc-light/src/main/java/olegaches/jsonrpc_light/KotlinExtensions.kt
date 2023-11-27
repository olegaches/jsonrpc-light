package olegaches.jsonrpc_light

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

inline fun <reified T> JsonRpcLight.create(): T = create(T::class.java)

suspend fun <T> Call<T>.awaitResponse(): JsonRpcResponse<T> {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: JsonRpcResponse<T>) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }
}

suspend fun <T : Any> Call<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: JsonRpcResponse<T>) {
                if(response.error != null) {
                    continuation.resumeWithException(
                        JsonRpcException(
                            response.error.message,
                            response.error.code,
                            response.error.data
                        )
                    )
                } else if(response.result == null) {
                    val e = KotlinNullPointerException(
                        "Response from " +
                                call.request().method +
                                " was null but response body type was declared as non-null"
                    )
                    continuation.resumeWithException(e)
                } else {
                    continuation.resume(response.result)
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }
}

@JvmName("awaitNullable")
suspend fun <T : Any> Call<T?>.await(): T? {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback<T?> {
            override fun onResponse(call: Call<T?>, response: JsonRpcResponse<T?>) {
                if(response.error != null) {
                    continuation.resumeWithException(
                        JsonRpcException(
                            response.error.message,
                            response.error.code,
                            response.error.data
                        )
                    )
                } else {
                    continuation.resume(response.result)
                }
            }

            override fun onFailure(call: Call<T?>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }
}

suspend fun NotificationCall.await() {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : NotificationCallback {
            override fun onResponse(call: NotificationCall) {
                continuation.resume(Unit)
            }

            override fun onFailure(call: NotificationCall, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }
}