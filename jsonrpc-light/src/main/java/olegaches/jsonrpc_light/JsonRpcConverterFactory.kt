package olegaches.jsonrpc_light

interface JsonRpcConverterFactory {
    fun requestConverter(): JsonRpcRequestConverter

    fun responseBodyConverter(): JsonRpcResponseBodyConverter
}