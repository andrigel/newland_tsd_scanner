package com.example.newland_tsd_scanner

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import io.flutter.plugin.common.EventChannel
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import android.app.Activity
import io.flutter.plugin.common.PluginRegistry
import android.util.Log

class NewlandTsdScannerPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel : MethodChannel
  private lateinit var mContext: Context
  private var mFilter : IntentFilter? = null
  private lateinit var activity: Activity

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "newland_tsd_scanner")
    channel.setMethodCallHandler(this)
    mContext = flutterPluginBinding.applicationContext
  }

      override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "start_scanner" -> {
                startScan()
                 Log.d("TAG", "startScan()")
                result.success("Ok")
            }
            "stop_scanner" -> {
                stopScan()
                Log.d("TAG", "stopScan()")
                result.success("Ok")
            }
            else -> {
                result.notImplemented()
            }
        }
    }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

  override fun onDetachedFromActivityForConfigChanges() {
    }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

  override fun onDetachedFromActivity() {
    }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  fun startScan() {
    var intent : Intent = Intent().also { intent ->
        intent.setAction("nlscan.action.SCANNER_TRIG")
        intent.putExtra("EXTRA_SCAN_MODE ", 3);
        intent.putExtra("EXTRA_TRIG_MODE ", 0);
        mContext.sendBroadcast(intent)
};
  }
  fun stopScan() {
    var intent : Intent = Intent().also { intent ->
    intent.setAction("nlscan.action.STOP_SCAN")
    mContext.sendBroadcast(intent)
};
  }
}