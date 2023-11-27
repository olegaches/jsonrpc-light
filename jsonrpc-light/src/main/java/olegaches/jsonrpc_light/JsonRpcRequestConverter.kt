package olegaches.jsonrpc_light

fun interface JsonRpcRequestConverter {
    fun convert(request: JsonRpcRequest): String
}
