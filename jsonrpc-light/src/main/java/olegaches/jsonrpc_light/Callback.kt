package olegaches.jsonrpc_light

interface Callback<T> {
    fun onResponse(call: Call<T>, response: JsonRpcResponse<T>)

    fun onFailure(call: Call<T>, t: Throwable)
}