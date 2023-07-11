package com.omega.newland_tsd_scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.Serializable

class CustomBroadcastReceiver(
        val id: Int,
        private val names: List<String>,
        private val listener: (Any) -> Unit
) : BroadcastReceiver() {
    companion object {
        const val TAG: String = "CustomBroadcastReceiver"
    }

    private val intentFilter: IntentFilter by lazy {
        val intentFilter = IntentFilter()
        names.forEach { intentFilter.addAction(it) }
        intentFilter
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "received intent " + intent?.action)
        intent?.let {
            val bundle = it.extras
            val dataPairs = bundle?.keySet()?.map { key ->
                Pair(key, bundle.get(key))
            }
            val data = dataPairs?.toMap() ?: mapOf()
            listener(mapOf(
                    "receiverId" to id,
                    "name" to it.action!!,
                    "data" to normalize(data)
            ))
        }
    }

    fun start(context: Context) {
        context.registerReceiver(this, intentFilter)
        Log.d(TAG, "starting to listen for broadcasts: " + names.joinToString(";"))
    }

    fun stop(context: Context) {
        context.unregisterReceiver(this)
        Log.d(TAG, "stopped listening for broadcasts: " + names.joinToString(";"))
    }
}

class BroadcastManager(private val applicationContext: Context) {
    companion object {
        const val TAG: String = "BroadcastManager"
    }

    private var receivers: Map<Int, CustomBroadcastReceiver> = mapOf()

    fun startReceiver(receiver: CustomBroadcastReceiver) {
        Log.d(TAG, "starting receiver " + receiver.id.toString())
        // TODO: handle case when receiver exists
        receiver.start(applicationContext)
        receivers = receivers + Pair(receiver.id, receiver)
    }

    fun stopReceiver(id: Int) {
        Log.d(TAG, "stopping receiver $id")
        // TODO: handle non-existing case
        receivers[id]?.stop(applicationContext)
        receivers = receivers.filter { it.key != id }
    }

    fun stopAll() {
        receivers.forEach { it.value.stop(applicationContext) }
    }
}

class MethodCallHandlerImpl(
        private val context: Context,
        private val broadcastManager: BroadcastManager
) : MethodCallHandler {
    companion object {
        const val TAG: String = "MethodCallHandlerImpl"
    }

    private var channel: MethodChannel? = null

    private fun withReceiverArgs(
            call: MethodCall,
            result: Result,
            func: (id: Int, names: List<String>) -> Unit
    ) {
        val id = call.argument<Int>("id")
                ?: return result.error("1", "no receiver id provided", null)

        val names = call.argument<List<String>>("names")
                ?: return result.error("1", "no names provided", null)

        func(id, names)
    }

    private fun withBroadcastArgs(
            call: MethodCall,
            result: Result,
            func: (name: String, data: Map<String, Any>) -> Unit
    ) {
        val name = call.argument<String>("name")
                ?: return result.error("1", "no broadcast name provided", null)
        val data = call.argument<Map<String, Any>>("data") ?: mapOf()
        func(name, data)
    }

    private fun onStartReceiver(call: MethodCall, result: Result) {
        withReceiverArgs(call, result) { id, names ->
            broadcastManager.startReceiver(CustomBroadcastReceiver(id, names) { broadcast ->
                channel?.invokeMethod("receiveBroadcast", broadcast)
            })
            result.success(null)
        }
    }

    private fun onStopReceiver(call: MethodCall, result: Result) {
        withReceiverArgs(call, result) { id, _ ->
            broadcastManager.stopReceiver(id)
            result.success(null)
        }
    }

    private fun onSendBroadcast(call: MethodCall, result: Result) {
        withBroadcastArgs(call, result) { name, data ->
            Intent().also { intent ->
                intent.action = name
                data.forEach { entry ->
                    intent.putExtra(entry.key, entry.value as Serializable)
                }
                context.sendBroadcast(intent)
                Log.d(TAG, "sent broadcast: $name")
            }
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        Log.d(TAG, "received method call " + call.method)
        when (call.method) {
            "startReceiver" -> {
                onStartReceiver(call, result)
            }
            "stopReceiver" -> {
                onStopReceiver(call, result)
            }
            "sendBroadcast" -> {
                onSendBroadcast(call, result)
            }
            "init_scanner" -> {
            var intent:Intent = Intent ("ACTION_BAR_SCANCFG"); 
            intent.putExtra("EXTRA_SCAN_MODE", 3);
            context.sendBroadcast(intent)
            result.success(true)
            }
        }
    }

    fun startListening(messenger: BinaryMessenger) {
        if (channel != null) {
            Log.wtf(TAG, "Setting a method call handler before the last was disposed.")
            stopListening()
        }

        channel = MethodChannel(messenger, "newland_tsd_scanner")
        channel!!.setMethodCallHandler(this)
    }

    fun stopListening() {
        if (channel == null) {
            Log.d(TAG, "Tried to stop listening when no MethodChannel had been initialized.")
            return
        }

        channel!!.setMethodCallHandler(null)
        channel = null
    }
}

class NewlandTsdScannerPlugin : FlutterPlugin {
    companion object {
        const val TAG: String = "NewlandTsdScannerPlugin"
    }

    private var methodCallHandler: MethodCallHandlerImpl? = null
    private var broadcastManager: BroadcastManager? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        broadcastManager = BroadcastManager(flutterPluginBinding.applicationContext)
        methodCallHandler = MethodCallHandlerImpl(
                flutterPluginBinding.applicationContext,
                broadcastManager!!
        )
        methodCallHandler!!.startListening(flutterPluginBinding.binaryMessenger)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        if (methodCallHandler == null) {
            Log.wtf(TAG, "Already detached from engine.")
            return
        }
        methodCallHandler!!.stopListening()
        methodCallHandler = null
        broadcastManager?.stopAll()
        broadcastManager = null
    }    
}

/***
 * Normalize the intent data to types that Flutter's StandardMessageCodec can pass.
 *
 * Flutter's StandardMessageCodec is the mechanism used for passing intent contents to the Flutter
 * layer. As only specific types are supported by it, other types are normalized to their String
 * representation.
 *
 * This code should be updated when Flutter Engine's code supports additional types.
 * See https://github.com/flutter/engine/blob/main/shell/platform/android/io/flutter/plugin/common/StandardMessageCodec.java
 */
private fun normalize(x: Any?) : Any? {
	if (
        x == null 
        || x.equals(null)
        || x is Boolean
        || x is Int
        || x is Short
        || x is Byte
        || x is Long
        || x is Float
        || x is Double
        || x is java.math.BigInteger
        || x is CharSequence
        || x is ByteArray
        || x is IntArray
        || x is LongArray
        || x is DoubleArray
        || x is FloatArray
    ) {
    	return x
    } else if (x is List<*>) {
        return normalizeList(x)
    } else if (x is Map<*, *>) {
    	return normalizeMap(x)
    } else {
        return x.toString()
    }
}

private fun <V> normalizeList(x: List<V>) : List<Any?> {
    return x.map { item ->
    	normalize(item)
    }
}

private fun <K, V> normalizeMap(x: Map<K, V>) : Map<K, Any?> {
    val pairs = x.keys.map { key ->
        Pair(key, normalize(x[key]))
    }
    return pairs.toMap()
}


// import androidx.annotation.NonNull

// import io.flutter.embedding.engine.plugins.FlutterPlugin
// import io.flutter.plugin.common.MethodCall
// import io.flutter.plugin.common.MethodChannel
// import io.flutter.plugin.common.MethodChannel.MethodCallHandler
// import io.flutter.plugin.common.MethodChannel.Result
// import android.content.Context
// import android.content.Intent
// import android.content.IntentFilter
// import android.content.BroadcastReceiver
// import io.flutter.plugin.common.EventChannel
// import io.flutter.embedding.engine.plugins.activity.ActivityAware
// import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
// import android.app.Activity
// import io.flutter.plugin.common.PluginRegistry
// import android.util.Log
// import java.util.Iterator;
// import java.util.Set;
// import android.os.Bundle;

// class NewlandTsdScannerPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, EventChannel.StreamHandler {
//   private lateinit var channel : MethodChannel
//    private lateinit var eventChannel: EventChannel
//   private lateinit var context: Context
//   private var mFilter : IntentFilter? = null
//   // private var myReceiver:MyReceiver? = null
//   private var receiver: BroadcastReceiver? = null
//   private lateinit var activity: Activity

//   override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
//     channel = MethodChannel(flutterPluginBinding.binaryMessenger, "newland_tsd_scanner")
//     eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "newland_tsd_scanner/results")
//     eventChannel.setStreamHandler(this)
//     channel.setMethodCallHandler(this)
//     context = flutterPluginBinding.applicationContext
//   }

//       override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
//         when (call.method) {
//             "start_scanner" -> {
//                 startScan()
//                 Log.d("TAG", "startScan()")
//                 result.success("Ok")
//             }
//             "stop_scanner" -> {
//                 stopScan()
//                 Log.d("TAG", "stopScan()")
//                 result.success("Ok")
//             }
//             "subscribe"-> {
//                  Log.d("TAG", "subscribe")
//                  mFilter = IntentFilter("nlscan.action.SCANNER_RESULT");
//                  context.registerReceiver(receiver, mFilter);
//             }
//             "unsubscribe"-> {
//                  Log.d("TAG", "unsubscribe")
//                  context.unregisterReceiver(receiver)
//             }
//             else -> {
//                 result.notImplemented()
//             }
//         }
//     }

//   override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
//       Log.d("TAG", "listen")
//         receiver = createReceiver(events)
//     }
  
//   override fun onCancel(arguments: Any?) {
//       unregisterReceiver()
//       receiver = null
//     }

//   private fun createReceiver(events: EventChannel.EventSink?): BroadcastReceiver {
//         return object : BroadcastReceiver() {
//             override fun onReceive(context: Context?, intent: Intent?) {
//                 Log.d("TAG", "receive")
//             }

//         }
//     }

//     private fun unregisterReceiver() {
//         try {
//             if (receiver != null) {
//                 context.unregisterReceiver(receiver)
//             }
//         } catch (e: IllegalArgumentException) {
//             // Receiver not registered
//         }
//     }

//   override fun onAttachedToActivity(binding: ActivityPluginBinding) {
//         activity = binding.activity
//     }

//   override fun onDetachedFromActivityForConfigChanges() {
//     }

//   override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
//         activity = binding.activity
//     }

//   override fun onDetachedFromActivity() {
//     }

//   override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
//     channel.setMethodCallHandler(null)
//   }

//   fun startScan() {
//     var intent : Intent = Intent().also { intent ->
//         intent.setAction("nlscan.action.SCANNER_TRIG")
//         intent.putExtra("EXTRA_SCAN_MODE ", 3);
//         context.sendBroadcast(intent)
// };
//   }
//   fun stopScan() {
//     var intent : Intent = Intent().also { intent ->
//     intent.setAction("nlscan.action.STOP_SCAN")
//     context.sendBroadcast(intent)
// };
//   }
// }

// public class MyReceiver: BroadcastReceiver() {
//     override fun onReceive(context:Context, intent:Intent) {
//         Log.d("TAG", "Event received")
//     }
// }