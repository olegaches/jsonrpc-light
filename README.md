[![Release](https://jitpack.io/v/olegaches/jsonrpc-light.svg)](https://jitpack.io/#olegaches/jsonrpc-light)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://github.com/olegaches/jsonrpc-light/blob/main/LICENSE)

# jsonrpc-light
A Retrofit-like library for JSON-RPC 2.0 requests. Supports Kotlin and Java.

## Usage in Kotlin
```kotlin
interface MyService {
    @JsonRpcCall("method_name")
    suspend fun myFunction(
        @JsonRpcParam("first_param_name")
        firstParam: String,
        @JsonRpcParam("second_param_name")
        secondParam: Boolean
    ): MyDto

    companion object {
        const val BASE_URL = "https://my-base-url.com"
    }
}


val myService: MyService = JsonRpcLight
    .Builder()
    .baseUrl(MyService.BASE_URL)
    .build()
    .create()

class MyRepository(private val myService: MyService) {
    fun getSomeInfo(firstParam: String, secondParam: Boolean): Flow<MyDto> {
        return flow {
            try {
                val myDtoResponse = myService.myFunction(firstParam, secondParam)
                emit(myDtoResponse)
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> {
                        TODO("Handle IOException. For example no internet connection.")
                    }

                    is JsonRpcException -> {
                        TODO(
                            "Handle JsonRpcException. Exception is thrown when" +
                                    " the error body in the JSON RPC Response object is not null."
                        )
                    }

                    is TransportException -> {
                        TODO(
                            "Handle TransportException. Exception is thrown" +
                                    " when the http response body is null and not successful"
                        )
                    }

                    else -> {
                        TODO("Handle unknown exception.")
                    }
                }
            }
        } // !!!!!!!!!!!you should not call this on Main Thread. Use Dispatchers.IO
    }
}
```

## Usage in Java

```Java
interface TestRpc {
    @JsonRpcNotification(methodName = "method_name")
    Call<MyDto> myMethod(
            @JsonRpcParam(paramName = "first_param_name")
            String firstParam,
            @JsonRpcParam(paramName = "second_param_name")
            boolean secondParam
            );

    String BASE_URL = "https://my-base-url.com";
}

class SomeClass {
    void someMethodInSomeClass() {
        final MyService myService = new JsonRpcLight
                .Builder()
                .baseUrl(TestRpc.BASE_URL)
                .build()
                .create(MyService.class);

        myService.myMethod("firstParam", true).enqueue(new Callback<MyDto>() {
            @Override
            public void onResponse(@NonNull Call<MyDto> call, @NonNull JsonRpcResponse<MyDto> jsonRpcResponse) {
    	        /* !!!!!!!!
    	        Note that requests are executed in the background thread;
    	        however, despite this, the library DOES NOT switch
    	        the thread to the Main one after receiving the response.
    	        You have to do it yourself, for example, through mainHandler.post(...). */

                final JsonRpcError error;
                final MyDto result;
                if((error = jsonRpcResponse.getError()) != null) {
                    // Handle JsonRpcError
                } else if ((result = jsonRpcResponse.getResult()) != null) {
                    System.out.println(result.getFirstField());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyDto> call, @NonNull Throwable throwable) {
                TODO("handle throwable")
            }
        });
    }
}
```

## Download 

Using Kotlin DSL

`settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven(url = "https://jitpack.io")
        mavenCentral()
    }
}
```

`build.gradle.kts`

```kotlin
dependencies {
  implementation("com.github.olegaches:jsonrpc-light:1.0.0")
  ...
}
```

Using Groovy

`settings.gradle`

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
        mavenCentral()
    }
}
```

`build.gradle`

```groovy
dependencies {
    implementation 'com.github.olegaches:jsonrpc-light:1.0.0'
    ...
}
```

The targetCompatibility and sourceCompatibility of this library are set to JavaVersion.VERSION_17.

## Features

### Synchronous call
Kotlin:
```kotlin
@JsonRpcCall("method_name")
fun myFunction(...): MyDto // just don't use suspend keyword
```
Java:
```java
myService.execute()
```
### Positional parameters
If the JSON RPC 2.0 method takes parameters, but the parameters do not have names, use JsonRpcPosParam annotation instead of JsonRpcParam.
```kotlin
@JsonRpcCall("method_name")
suspend fun getSome(
    @JsonRpcPosParam
    firstParam: String,
    @JsonRpcPosParam
    secondParam: Boolean,
): Any
```

### Notification call
If you don't expect any response from the request, use JsonRpcNotification annotation instead of JsonRpcCall.

Kotlin:
```kotlin
@JsonRpcNotification("method_name")
suspend fun notify(@JsonRpcParam("paramName") param: String)
```

Java:
```java
@JsonRpcNotification(methodName = "method_name")
NotificationCall notify(@JsonRpcParam(paramName = "param_name") String param);
```

```java
myService.notify("param").enqueue(new NotificationCallback() {
    @Override
    public void onFailure(@NonNull NotificationCall notificationCall, @NonNull Throwable throwable) {
        // handle errors
    }
    /* You can also optionally override onResponse
    to know when the request is completed. */
});
```

### Multiple params objects (list of dto objects) and multiple result objects
Example of a request with multiple parameters and a response in the form of a list:
```
json request
{
   "id": 123,
   "jsonrpc": "2.0",
   "method": "method_name",
   "params": [{
         "param": "param_value_1"
      },
      {
         "param": "param_value_2"
      }
   ]
}
```
```
json response
{
  "jsonrpc": "2.0",
  "id": 123,
  "result": [
    {
      "result_field": "some text 1",
      "additional_info_field": "some info 1"
    },
    {
      "result_field": "some text 2",
      "additional_info_field": "some info 2"
    },
  ]
}
```
#### How to send list of params objects and get list of results:
```kotlin
@JsonRpcCall("method_name")
suspend fun getSome(
    @JsonRpcList
    myList: List<ParamDto>
): List<ResultDto>
```
```kotlin
data class ResultDto(
    val address: String,
    val removed: Boolean,
    val topics: List<String>,
    ...
)
```
```kotlin
data class ParamDto(
    @RpcParamField("fromBlock")
    val fromBlock: String,
    @RpcParamField("topics")
    val topics: List<String>,
    ...
)
```

### How to get JsonRpcResponse object in Kotlin
```kotlin
@JsonRpcCall("method_name")
suspend fun getSome(...): JsonRpcResponse<MyDto> // or JsonRpcResponse<List<MyDto>>
```
