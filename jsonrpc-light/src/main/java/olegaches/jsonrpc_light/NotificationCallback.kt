package olegaches.jsonrpc_light

interface NotificationCallback {
    fun onResponse(call: NotificationCall) = Unit

    fun onFailure(call: NotificationCall, t: Throwable)
}