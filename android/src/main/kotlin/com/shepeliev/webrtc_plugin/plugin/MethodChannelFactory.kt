package com.shepeliev.webrtc_plugin.plugin

import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry.Registrar

internal interface MethodChannelFactory {
    fun createMethodChannel(
        registrar: Registrar,
        channelName: String,
        handlers: Map<String, MethodHandler<*>>
    ): MethodChannel
}

internal class DefaultMethodChannelFactory : MethodChannelFactory {
    override fun createMethodChannel(
        registrar: Registrar,
        channelName: String,
        handlers: Map<String, MethodHandler<*>>
    ): MethodChannel = MethodChannel(registrar.messenger(), channelName).apply {
        setMethodCallHandler(MethodCallHandlers(handlers))
    }
}
