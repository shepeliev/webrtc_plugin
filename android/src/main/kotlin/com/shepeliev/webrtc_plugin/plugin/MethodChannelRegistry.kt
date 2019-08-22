package com.shepeliev.webrtc_plugin.plugin

import com.shepeliev.webrtc_plugin.*
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

internal interface MethodChannelRegistry {
    fun addFlutterBackend(backend: FlutterBackend)
    fun removePlugin(backend: FlutterBackend)
}

internal class DefaultMethodChannelRegistry(
    private val registrar: PluginRegistry.Registrar,
    private val methodChannelFactory: MethodChannelFactory = DefaultMethodChannelFactory()
) : MethodChannelRegistry {

    // open for testing
    val methodChannels = mutableMapOf<String, MethodChannel>()

    fun addGlobalBackends(globalBackends: Collection<FlutterBackend>) {
        if (globalBackends.isNotEmpty()) {
            methodChannels += createGlobalMethodChannel(globalBackends)
        }
    }

    private fun createGlobalMethodChannel(globalBackends: Collection<FlutterBackend>): Map<String, MethodChannel> {
        val handlers: Map<String, MethodHandler<*>> =
            globalBackends.fold(mapOf()) { acc, plugin ->
                require(plugin.methodHandlers.keys.none { acc.containsKey(it) }) {
                    "$plugin has duplicated method handlers.\n" +
                            "Plugin methods: ${plugin.methodHandlers.keys}\n" +
                            "Registered methods: ${acc.keys}"
                }
                acc + plugin.methodHandlers
            }
        val methodChannel = methodChannelFactory.createMethodChannel(
            registrar,
            METHOD_CHANNEL_NAME,
            handlers
        )
        return mapOf(METHOD_CHANNEL_NAME to methodChannel)
    }

    override fun addFlutterBackend(backend: FlutterBackend) {
        require(!methodChannels.containsKey(backend.channelName)) {
            "Plugin with channel '${backend.channelName}' already registered."
        }
        val methodChannel = methodChannelFactory.createMethodChannel(
            registrar,
            backend.channelName,
            backend.methodHandlers
        )
        val newEntry = backend.channelName to methodChannel
        methodChannels += newEntry
    }

    override fun removePlugin(backend: FlutterBackend) {
        val channel = methodChannels[backend.channelName]
        channel?.setMethodCallHandler(null)
        methodChannels -= backend.channelName
    }
}

