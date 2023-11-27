package olegaches.jsonrpc_light

data class JsonRpcResponse<T>(
    val id: Long?,
    val result: T?,
    val error: JsonRpcError?
)
