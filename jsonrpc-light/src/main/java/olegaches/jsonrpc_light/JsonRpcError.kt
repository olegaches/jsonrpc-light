package olegaches.jsonrpc_light

data class JsonRpcError(
    val message: String,
    val code: Int,
    val data: Any?
)
