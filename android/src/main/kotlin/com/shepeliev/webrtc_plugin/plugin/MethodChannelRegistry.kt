package com.shepeliev.webrtc_plugin.plugin

import com.shepeliev.webrtc_plugin.*
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

internal interface MethodChannelRegistry {
    fun addPlugin(plugin: FlutterPlugin)
    fun removePlugin(plugin: FlutterPlugin)
}

internal class DefaultMethodChannelRegistry(
    private val registrar: PluginRegistry.Registrar,
    globalPlugins: Collection<FlutterPlugin>,
    private val methodChannelFactory: MethodChannelFactory = DefaultMethodChannelFactory()
) : MethodChannelRegistry {

    // open for testing
    val methodChannels = mutableMapOf<String, MethodChannel>()

    init {
        if (globalPlugins.isNotEmpty()) {
            methodChannels += createGlobalMethodChannel(globalPlugins)
        }
    }

    private fun createGlobalMethodChannel(globalPlugins: Collection<FlutterPlugin>): Map<String, MethodChannel> {
        val handlers: Map<String, MethodHandler<*>> =
            globalPlugins.fold(mapOf()) { acc, plugin ->
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

    override fun addPlugin(plugin: FlutterPlugin) {
        require(!methodChannels.containsKey(plugin.channelName)) {
            "Plugin with channel '${plugin.channelName}' already registered."
        }
        val methodChannel = methodChannelFactory.createMethodChannel(
            registrar,
            plugin.channelName,
            plugin.methodHandlers
        )
        val newEntry = plugin.channelName to methodChannel
        methodChannels += newEntry
    }

    override fun removePlugin(plugin: FlutterPlugin) {
        val channel = methodChannels[plugin.channelName]
        channel?.setMethodCallHandler(null)
        methodChannels -= plugin.channelName
    }
}

