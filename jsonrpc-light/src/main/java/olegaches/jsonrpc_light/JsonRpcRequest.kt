package olegaches.jsonrpc_light

sealed interface JsonRpcRequest {
    val id: Long?
    val method: String
    val jsonrpc: String

    data class PositionalParamsRequest(
        val params: List<Any?>? = null,
        override val jsonrpc: String = JSONRPC_VERSION,
        override val id: Long?,
        override val method: String,
    ): JsonRpcRequest

    data class NamedParamsRequest(
        val params: Map<String, Any?>? = null,
        override val id: Long?,
        override val method: String,
        override val jsonrpc: String = JSONRPC_VERSION,
    ): JsonRpcRequest

    companion object {
        const val JSONRPC_VERSION = "2.0"
    }
}