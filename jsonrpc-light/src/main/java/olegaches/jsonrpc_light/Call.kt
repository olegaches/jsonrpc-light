package olegaches.jsonrpc_light

import okhttp3.Request

interface Call<T> {
    fun execute(): JsonRpcResponse<T>

    fun enqueue(callback: Callback<T>)

    fun cancel()

    fun request(): Request
}