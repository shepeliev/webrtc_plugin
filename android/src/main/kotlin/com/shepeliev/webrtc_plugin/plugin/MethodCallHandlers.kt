package com.shepeliev.webrtc_plugin.plugin

import android.os.AsyncTask
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result

typealias MethodHandler<T> = (MethodCall) -> T?

private val TAG = MethodCallHandlers::class.java.simpleName

class MethodCallHandlers(vararg handlers: Map<String, MethodHandler<*>>) :
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
            ?.let { HandleMethodTask(it, result).execute(methodCall) }
            ?: run {
                Log.e(TAG, "Method '${methodCall.method}' is not implemented yet.")
                result.notImplemented()
            }
    }

}

private class HandleMethodTask(
    private val handler: MethodHandler<*>,
    private val methodChannelResult: Result
) : AsyncTask<MethodCall, Nothing, Pair<Throwable?, Any?>>() {

    override fun doInBackground(vararg params: MethodCall): Pair<Throwable?, Any?> {
        val methodCall = params[0]
        return try {
            val handlerResult = handler(methodCall)
            Pair(null, handlerResult)
        } catch (e: Throwable) {
            val message = "{method=\"${methodCall.method}\", arguments=${methodCall.arguments}}"
            Log.e(TAG, message, e)
            Pair(e, null)
        }
    }

    override fun onPostExecute(result: Pair<Throwable?, Any?>) {
        result.first?.let { methodChannelResult.error("UNHANDLED_EXCEPTION", it.message, it) }
            ?: methodChannelResult.success(result.second)
    }
}
