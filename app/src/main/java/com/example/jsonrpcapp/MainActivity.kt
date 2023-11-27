package com.example.jsonrpcapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.jsonrpcapp.ui.Gg
import com.example.jsonrpcapp.ui.theme.JsonRpcAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import olegaches.jsonrpc_light.RpcCall
import olegaches.jsonrpc_light.JsonRpcLight
import olegaches.jsonrpc_light.JsonRpcNotification
import olegaches.jsonrpc_light.JsonRpcParam
import olegaches.jsonrpc_light.create
import olegaches.jsonrpc_light.Call
import olegaches.jsonrpc_light.Callback
import olegaches.jsonrpc_light.JsonRpcCall
import olegaches.jsonrpc_light.JsonRpcList
import olegaches.jsonrpc_light.JsonRpcPosParam
import olegaches.jsonrpc_light.JsonRpcResponse
import olegaches.jsonrpc_light.NotificationCall
import olegaches.jsonrpc_light.NotificationCallback
import olegaches.jsonrpc_light.RpcParamField
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JsonRpcAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        val sf = rememberCoroutineScope()
                        var err_eth_blockNumber = remember {
                            mutableStateOf("not yet")
                        }
                        var sus_eth_blockNumber by remember {
                            mutableStateOf("not yet")
                        }
                        var call_eth_blockNumber by remember {
                            mutableStateOf("not yet")
                        }
                        var note_eth_blockNumber by remember {
                            mutableStateOf("not yet")
                        }
                        var note_eth_blockNumber_sus by remember {
                            mutableStateOf("not yet")
                        }
                        val lol: TestRpc = JsonRpcLight.Builder().baseUrl(TestRpc.BASE_URL).build().create()
                        Button(onClick = {
//                            sf.launch(Dispatchers.IO) {
//                                try {
//                                    err_eth_blockNumber.value = lol.err_eth_blockNumber()
//                                } catch (t: Throwable) {
//                                    Log.w("666", t.message ?: "err_eth_blockNumber null message")
//                                }
//                            }
//                            sf.launch(Dispatchers.IO) {
//                                try {
//                                    sus_eth_blockNumber = lol.sus_eth_blockNumber().toString()
//                                } catch (t: Throwable) {
//                                    val a = t is IOException
//                                    val b = a
//                                    Log.w("666", t.message ?: "sus_eth_blockNumber null message")
//                                }
//                            }
//                            sf.launch(Dispatchers.IO) {
//                                try {
//                                    call_eth_blockNumber = lol.call_eth_blockNumber().execute().result?: "was null"
//                                } catch (t: Throwable) {
//                                    Log.w("666", t.message ?: "call_eth_blockNumber null message")
//                                }
//                            }
//                            run {
//                                Log.w("666", "start enqueue")
//                                lol.call_eth_blockNumber().enqueue(object : Callback<String> {
//                                    override fun onFailure(call: Call<String>, t: Throwable) {
//                                        Log.w(
//                                            "666",
//                                            t.message ?: "call_eth_blockNumber null message"
//                                        )
//                                    }
//
//                                    override fun onResponse(
//                                        call: Call<String>,
//                                        response: JsonRpcResponse<String>
//                                    ) {
//                                        Log.w("666", "onResponse enqueue")
//                                        call_eth_blockNumber =
//                                            response.result ?: "response was null"
//                                    }
//                                })
//                                Log.w("666", "end enqueue")
//                            }
//                            sf.launch(Dispatchers.IO) {
//                                try {
//                                    lol.note_eth_blockNumber().execute()
//                                } catch (t: Throwable) {
//                                    Log.w("666", t.message ?: "call_eth_blockNumber null message")
//                                }
//                            }
//                            sf.launch(Dispatchers.IO) {
//                                try {
//                                    lol.note_eth_blockNumber().enqueue(
//                                        object: NotificationCallback {
//                                            override fun onFailure(
//                                                call: NotificationCall,
//                                                t: Throwable
//                                            ) {
//                                                val a = t
//                                            }
//
//                                            override fun onResponse(call: NotificationCall) {
//                                                val a = call
//                                            }
//                                        }
//                                    )
//                                } catch (t: Throwable) {
//                                    Log.w("666", t.message ?: "call_eth_blockNumber null message")
//                                }
//                            }
//                            sf.launch(Dispatchers.IO) {
//                                try {
//                                    lol.note_eth_blockNumber_sus()
//                                } catch (t: Throwable) {
//                                    Log.w("666", t.message ?: "call_eth_blockNumber null message")
//                                }
//                            }
                            sf.launch(Dispatchers.IO) {
                                try {
                                    val a = lol.eth_getLogs(listOf(WTF(
                                        address = listOf("0xb59f67a8bff5d8cd03f6ac17265c550ed8f33907"),
                                        fromBlock = "0x429d3b",
                                        toBlock = "latest",
                                        topics = listOf("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                                            "0x00000000000000000000000000b46c2526e227482e2ebb8f4c69e4674d262e75",
                                            "0x00000000000000000000000054a2d42a40f51259dedd1978f6c118a0f0eff078")
                                    ))).execute()
                                    val b = a.result?.first()?.topics
                                    val r = a.result?.first()?.removed
                                    val c = b to r
                                } catch (t: Throwable) {
                                    Log.w("666", t.message ?: "eth_getLogs null message")
                                }
                            }
                            sf.launch(Dispatchers.IO) {
                                try {
                                    val a = lol.sus_eth_getLogs(
                                        listOf(WTF(
                                            address = listOf("0xb59f67a8bff5d8cd03f6ac17265c550ed8f33907"),
                                            fromBlock = "0x429d3b",
                                            toBlock = "latest",
                                            topics = listOf("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                                                "0x00000000000000000000000000b46c2526e227482e2ebb8f4c69e4674d262e75",
                                                "0x00000000000000000000000054a2d42a40f51259dedd1978f6c118a0f0eff078")
                                        ),
                                            WTF(
                                                address = listOf("0xb59f67a8bff5d8cd03f6ac17265c550ed8f33907"),
                                                fromBlock = "0x429d3b",
                                                toBlock = "latest",
                                                topics = listOf("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                                                    "0x00000000000000000000000000b46c2526e227482e2ebb8f4c69e4674d262e75",
                                                    "0x00000000000000000000000054a2d42a40f51259dedd1978f6c118a0f0eff078")
                                            ))
                                    ).result!!.first()
                                    val b = a
                                } catch (t: Throwable) {
                                    Log.w("666", t.message ?: "sus_eth_getLogs null message")
                                }
                            }
                        }) {
                            Text("Click me")
                        }
                        Text(text = err_eth_blockNumber.value)
                        Text(text = sus_eth_blockNumber)
                        Text(text = call_eth_blockNumber)
                    }
                }
            }
        }
    }
}
// test complex json object fields for request and dto
// mb Batch support
// test awaitRespnse

data class WTF(
    @RpcParamField("address")
    val address: List<String>,
    @RpcParamField("fromBlock")
    val fromBlock: String,
    @RpcParamField("toBlock")
    val toBlock: String,
    @RpcParamField("topics")
    val topics: List<String>
        )

interface TestRpc {
    @JsonRpcNotification("eth_newFilter")
    fun getSome(
        @JsonRpcParam("address")
        a: String,
        @JsonRpcParam("fromBlock")
        b: String,
        @JsonRpcParam("toBlock")
        c: String,
    ): Unit

    @JsonRpcCall("eth_blockNumber")
    fun err_eth_blockNumber(): String

    @JsonRpcCall("eth_blockNumber")
    suspend fun sus_eth_blockNumber(): String

    @JsonRpcCall("eth_blockNumber")
    fun call_eth_blockNumber(): Call<String>

    @JsonRpcCall("eth_blockNumber")
    fun note_eth_blockNumber(): NotificationCall

    @JsonRpcNotification("eth_blockNumber")
    suspend fun note_eth_blockNumber_sus(): NotificationCall

    @JsonRpcCall("eth_getLogs")
    fun eth_getLogs(
        @JsonRpcList
        listWtf: List<WTF>
    ): Call<List<eth_getLogsDto>>

    @JsonRpcCall("eth_getLogs")
    suspend fun sus_eth_getLogs(
        @JsonRpcList
        listWtf: List<WTF>
    ): JsonRpcResponse<List<eth_getLogsDto>>

    companion object {
        const val BASE_URL = "https://eth-mainnet.g.alchemy.com/v2/AVsgOxVzvNGMrfwXwjyyzWlpNnF7KOoX"
    }
}

data class eth_getLogsDto(
    val address: String,
    val blockHash: String,
    val blockNumber: String,
    val data: String,
    val logIndex: String,
    val removed: Boolean,
    val topics: List<String>,
    val transactionHash: String,
    val transactionIndex: String
)