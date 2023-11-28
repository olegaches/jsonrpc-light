# jsonrpc-light
A Retrofit-like library for JSON-RPC 2.0 requests.

```kotlin

val myService: MyService = JsonRpcLight
                            .Builder()
                            .baseUrl(MyService.BASE_URL)
                            .build()
                            .create()

interface MyService {

  @JsonRpcCall("method_name")
  suspend fun myFunction(
    @JsonRpcParam("first_param_name")
    firstParam: String,
    @JsonRpcParam("second_param")
    secondParam: Boolean
  ): MyDto

  companion object {
    const val BASE_URL = "https://my-base-url.com"
  }
}
```
