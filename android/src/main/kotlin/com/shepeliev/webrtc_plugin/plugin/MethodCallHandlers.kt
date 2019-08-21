package com.shepeliev.webrtc_plugin.plugin

import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result

internal typealias MethodHandler<T> = (MethodCall) -> T?

private val TAG = MethodCallHandlers::class.java.simpleName

internal class MethodCallHandlers(vararg handlers: Map<String, MethodHandler<*>>) :
    MethodChannel.MethodCallHandler {

    private val handlers: Map<String, MethodHandler<*>>

    init {
        this.handlers = handlers.fold(mapOf()) { acc, item -> acc + item }
    }

    override fun onMethodCall(methodCall: MethodCall, result: Result) {
        Log.d(TAG, "Call of method '${methodCall.method}(${methodCall.arguments})")
        handlers.filterKeys { it == methodCall.method }
            .values
            .firstOrNull()
            ?.let { tryExecuteMethod(methodCall, result, it) }
            ?: run {
                Log.e(TAG, "Method '${methodCall.method}' is not implemented yet.")
                result.notImplemented()
            }
    }

    private fun tryExecuteMethod(
        methodCall: MethodCall,
        result: Result,
        handler: MethodHandler<*>
    ) {
        try {
            val handlerResult = handler(methodCall)
            result.success(handlerResult)
        } catch (e: Throwable) {
            val message =
                "{method=\"${methodCall.method}\", arguments=${methodCall.arguments}, errorMessage=\"${e.message}\"}"
            Log.e(TAG, message, e)
            result.error("UNHANDLED_EXCEPTION", message, e)
        }
    }
}
