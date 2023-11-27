package olegaches.jsonrpc_light

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

val requestId = AtomicLong(0)

class JsonRpcLight(
    private val baseUrl: String,
    private val jsonRpcResponseBodyConverter: JsonRpcResponseBodyConverter,
    private val jsonRpcRequestConverter: JsonRpcRequestConverter,
    private val client: OkHttpClient,
) {
    fun <T> create(service: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(
            service.classLoader,
            arrayOf(service),
        ) { _, method, args: Array<out Any?>? ->
            val functionName = method.name
            val methodReturnType = method.returnType
            val continuation = args?.lastOrNull() as? Continuation<Any?>
            val suspendCall = continuation != null
            val (methodName, requestId) = checkNotNull(
                method
                    .getAnnotation(JsonRpcNotification::class.java)
                    ?.methodName
                    ?.to(null) ?: method
                    .getAnnotation(JsonRpcCall::class.java)
                    ?.methodName
                    ?.to(requestId.addAndGet(1))
            ) { "Missing JsonRpc method annotation" }
            val notificationCall = requestId == null
            checkMethodReturnType(methodReturnType, notificationCall, functionName, suspendCall)
            val (actualArgs, actualParameterAnnotations) = if(suspendCall) {
                args?.copyOfRange(0, args.lastIndex) to method.parameterAnnotations.let { it.copyOfRange(0, it.lastIndex) }
            } else args to method.parameterAnnotations

            val params = getJsonRpcParameters(functionName, service.simpleName, actualParameterAnnotations, actualArgs)
            val jsonRpcRequest = (params as? Map<String, Any?>)?.let {
                JsonRpcRequest.NamedParamsRequest(
                    id = requestId,
                    method = methodName,
                    params = it,
                )
            } ?: JsonRpcRequest.PositionalParamsRequest(
                id = requestId,
                method = methodName,
                params = params as? List<Any?>,
            )
            val requestBody =
                jsonRpcRequestConverter.convert(jsonRpcRequest).toByteArray().toRequestBody()
            val request = Request.Builder()
                .post(requestBody)
                .url(baseUrl)
                .build()
            if(!suspendCall) {
                return@newProxyInstance createJsonRpcCall(notificationCall, request, method)
            } else {
                executeSuspendableCall(continuation, notificationCall, request, method)
                return@newProxyInstance kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
            }
        } as T
    }

    private fun executeSuspendableCall(
        continuation: Continuation<Any?>?,
        notificationCall: Boolean,
        request: Request,
        method: Method
    ) {
        CoroutineScope(continuation!!.context).launch(Dispatchers.IO) {
            try {
                val result = if (notificationCall) {
                    JsonRpcNotificationCall(
                        client.newCall(request),
                    ).await()
                } else {
                    val suspendReturnType = method.kotlinFunction!!.returnType
                    if (suspendReturnType.jvmErasure == JsonRpcResponse::class) {
                        RpcCall<Any>(
                            client.newCall(request),
                            jsonRpcResponseBodyConverter,
                            suspendReturnType.arguments.first().type!!.javaType
                        ).awaitResponse()
                    } else {
                        val rpcCall = RpcCall<Any>(
                            client.newCall(request),
                            jsonRpcResponseBodyConverter,
                            suspendReturnType.javaType
                        )
                        if (suspendReturnType.isMarkedNullable) {
                            @Suppress("UNCHECKED_CAST")
                            (rpcCall as Call<Any?>).await()
                        } else {
                            rpcCall.await()
                        }
                    }
                }
                continuation.resume(result)
            } catch (t: Throwable) {
                continuation.resumeWithException(t)
            }
        }
    }

    private fun createJsonRpcCall(
        notificationCall: Boolean,
        request: Request,
        method: Method
    ): Any {
        return if (notificationCall) {
            JsonRpcNotificationCall(
                client.newCall(request),
            )
        } else {
            val genericType =
                checkNotNull((method.genericReturnType as? ParameterizedType)?.actualTypeArguments?.firstOrNull()) {
                    "Incorrect generic type of ${Call::class.java.simpleName}"
                }
            RpcCall<Any>(
                client.newCall(request),
                jsonRpcResponseBodyConverter,
                genericType
            )
        }
    }

    private fun checkMethodReturnType(
        methodReturnType: Class<*>?,
        notificationCall: Boolean,
        functionName: String?,
        suspendCall: Boolean
    ) {
        when (methodReturnType) {
            Call::class.java -> {
                check(!notificationCall) { "Function $functionName must have a return type of ${NotificationCall::class.java.simpleName}" }
            }
            NotificationCall::class.java -> {
                check(notificationCall) { "Function $functionName must have a return type of ${Call::class.java.simpleName}" }
            }
            else -> {
                check(suspendCall) { "Function $functionName must be suspend or have a return type of ${Call::class.java.simpleName}" }
            }
        }
    }

    private fun getJsonRpcParameters(
        methodName: String,
        serviceName: String,
        parameterAnnotations: Array<out Array<out Annotation?>>,
        args: Array<out Any?>?
    ): Any? {
        if (args.isNullOrEmpty()) {
            return null
        }
        val firstAnnotation = checkNotNull(parameterAnnotations.firstOrNull()?.firstOrNull { it is JsonRpcParam || it is JsonRpcPosParam || it is JsonRpcList }) {
            "Argument #0 of $serviceName#$methodName() must be annotated with @${JsonRpcPosParam::class.java.simpleName} or @${JsonRpcParam::class.java.simpleName}"
        }
        return when(firstAnnotation) {
            is JsonRpcParam -> {
                createParamMap(parameterAnnotations, serviceName, methodName, args)
            }
            is JsonRpcPosParam -> {
                createParamList(parameterAnnotations, serviceName, methodName, args)
            }
            is JsonRpcList -> {
                createMultipleParamsList(args)
            }
            else -> null // never happens
        }
    }

    private fun createMultipleParamsList(
        args: Array<out Any?>
    ) : List<Any>? {
        val rpcList = args.first() as List<*>
        val firstElement = rpcList.firstOrNull() ?: return null
        val firstElementClass = firstElement::class.java
        val classFields = firstElementClass.declaredFields.filter {
            it.isAnnotationPresent(RpcParamField::class.java) || it.isAnnotationPresent(RpcPosParamField::class.java) }
        val firstFieldAnnotation = checkNotNull(classFields.firstOrNull()?.declaredAnnotations?.firstOrNull { it is RpcParamField || it is RpcPosParamField }) {
            "Field #0 of ${firstElementClass.simpleName} must be annotated with @${RpcPosParamField::class.java.simpleName} or @${RpcParamField::class.java.simpleName}"
        }
        return when(firstFieldAnnotation) {
            is RpcParamField -> {
                val listOfParams = mutableListOf<MutableMap<String, Any?>>(mutableMapOf())
                val firstMap = listOfParams.first()
                classFields.forEachIndexed { index, field ->
                    val paramName = checkNotNull(field.getAnnotation(RpcParamField::class.java)) {
                        "Field #$index of ${firstElementClass.simpleName} must be annotated with @${RpcParamField::class.java.simpleName}"
                    }.paramName
                    field.isAccessible = true
                    firstMap[paramName] = field.get(firstElement)
                }
                rpcList.drop(1).forEachIndexed { rpcListIndex, element ->
                    listOfParams.add(mutableMapOf())
                    classFields.forEach { field ->
                        listOfParams[rpcListIndex + 1][field.getAnnotation(RpcParamField::class.java).paramName] = field.get(element)
                    }
                }
                listOfParams
            }
            is RpcPosParamField -> {
                val listOfParams = mutableListOf<MutableList<Any?>>(mutableListOf())
                val firstList = listOfParams.first()
                classFields.forEachIndexed { index, field ->
                    check(field.isAnnotationPresent(RpcPosParamField::class.java)) {
                        "Field #$index of ${firstElementClass.simpleName} must be annotated with @${RpcPosParamField::class.java.simpleName}"
                    }
                    field.isAccessible = true
                    firstList.add(field.get(firstElement))
                }
                rpcList.drop(1).forEachIndexed { rpcListIndex, element ->
                    listOfParams.add(mutableListOf())
                    classFields.forEach { field ->
                        listOfParams[rpcListIndex + 1].add(field.get(element))
                    }
                }
                listOfParams
            } else -> null
        }
    }

    private fun createParamList(
        parameterAnnotations: Array<out Array<out Annotation?>>,
        serviceName: String,
        methodName: String,
        args: Array<out Any?>
    ) = parameterAnnotations.mapIndexed { index, paramAnnotations ->
        check(paramAnnotations.any { it is JsonRpcPosParam }) {
            "Argument #$index of $serviceName#$methodName()" +
                    " must be annotated with @${JsonRpcPosParam::class.java.simpleName}"
        }
        args[index]
    }

    private fun createParamMap(
        parameterAnnotations: Array<out Array<out Annotation?>>,
        serviceName: String,
        methodName: String,
        args: Array<out Any?>
    ) = parameterAnnotations.mapIndexed { index, paramAnnotations ->
        checkNotNull(paramAnnotations.firstNotNullOfOrNull { it as? JsonRpcParam }) {
            "Argument #$index of $serviceName#$methodName()" +
                    " must be annotated with @${JsonRpcParam::class.java.simpleName}"
        }.paramName to args[index]
    }.toMap()

    class Builder {
        private var baseUrl: String? = null
        private var okHttpClient: OkHttpClient? = null
        private var converterFactory: JsonRpcConverterFactory? = null

        fun baseUrl(baseUrl: String): Builder {
            this.baseUrl = baseUrl
            return this
        }

        fun converterFactory(converterFactory: JsonRpcConverterFactory): Builder {
            this.converterFactory = converterFactory
            return this
        }

        fun okHttpClient(okHttpClient: OkHttpClient): Builder {
            this.okHttpClient = okHttpClient
            return this
        }

        fun build(): JsonRpcLight {
            val converterFactory = converterFactory ?: JsonRpcMoshiConverterFactory()
            return JsonRpcLight(
                baseUrl = checkNotNull(baseUrl) { "Base URL required." },
                jsonRpcResponseBodyConverter = converterFactory.responseBodyConverter(),
                jsonRpcRequestConverter = converterFactory.requestConverter(),
                client = okHttpClient ?: OkHttpClient.Builder().build()
            )
        }
    }
}