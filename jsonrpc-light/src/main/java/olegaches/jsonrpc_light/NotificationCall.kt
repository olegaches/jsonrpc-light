package olegaches.jsonrpc_light

import okhttp3.Request

interface NotificationCall {
    fun execute()

    fun enqueue(callback: NotificationCallback)

    fun request(): Request

    fun cancel()
}