package olegaches.jsonrpc_light

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.asArrayType
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.GenericDeclaration
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class JsonRpcMoshiConverterFactory(
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build(),
): JsonRpcConverterFactory {
    override fun requestConverter(): JsonRpcRequestConverter {
        return JsonRpcRequestConverter { request: JsonRpcRequest ->
            when(request) {
                is JsonRpcRequest.NamedParamsRequest -> {
                    moshi.adapter(JsonRpcRequest.NamedParamsRequest::class.java).toJson(request)
                }
                is JsonRpcRequest.PositionalParamsRequest -> {
                    moshi.adapter(JsonRpcRequest.PositionalParamsRequest::class.java).toJson(request)
                }
            }
        }
    }

    override fun responseBodyConverter(): JsonRpcResponseBodyConverter {
        return object: JsonRpcResponseBodyConverter {
            override fun <T> convert(data: ByteArray, type: Type): JsonRpcResponse<T> {
                val typeGeneric = Types.newParameterizedType(JsonRpcResponse::class.java, Any::class.java)
                var response = moshi.adapter<JsonRpcResponse<Any>>(typeGeneric).fromJson(data.decodeToString())
                    ?: throw IllegalStateException("Unexpectedly null json parse result for value: $data!")
                response.result?.let {
                    val parsedResult = moshi.adapter<T>(type).fromJsonValue(it)
                        ?: throw IllegalStateException("Unexpectedly null json parse result for value: $it!")
                    response = response.copy(result = parsedResult)
                }
                @Suppress("UNCHECKED_CAST")
                return response as JsonRpcResponse<T>
            }
        }
    }
}