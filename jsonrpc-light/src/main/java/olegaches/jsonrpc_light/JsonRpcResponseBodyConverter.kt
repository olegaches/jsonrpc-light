package olegaches.jsonrpc_light

import java.lang.reflect.Type

interface JsonRpcResponseBodyConverter {
    fun <T> convert(data: ByteArray, type: Type): JsonRpcResponse<T>
}
