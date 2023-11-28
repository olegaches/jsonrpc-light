# jsonrpc-light
A Retrofit-like library for JSON-RPC 2.0 requests.

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
          when(t) {
            is IOException -> {
              TODO("Handle IOException. For example no internet connection.")
            }
            is JsonRpcException -> {
              TODO("Handle JsonRpcException. Exception is thrown when the error body in the JSON RPC Response object is not null.")
            }
            is TransportException -> {
              TODO("Handle TransportException. Exception is thrown when the http response body is null and not successful")
            }
            else -> {
              TODO("Handle unknown exception.")
            }
          }
      }
    } // !!!!!!!!!!!!!!!!!!!you should not call this on Main Thread. Use Dispatchers.IO
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

The targetCompatibility and sourceCompatibility of this library are set to JavaVersion.VERSION_17.

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
