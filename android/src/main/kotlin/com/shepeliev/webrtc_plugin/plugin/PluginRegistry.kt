package com.shepeliev.webrtc_plugin.plugin

internal class PluginRegistry(private val methodChannelRegistry: MethodChannelRegistry) {
    private val plugins = mutableMapOf<String, FlutterPlugin>()

    val allPlugins: Collection<FlutterPlugin>
        get() = plugins.values

    fun add(plugin: FlutterPlugin) {
        plugins += (plugin.id to plugin)
        methodChannelRegistry.addPlugin(plugin)
    }

    fun remove(plugin: FlutterPlugin) {
        check(!plugin.isGlobal) { "Global plugin can't be removed." }
        methodChannelRegistry.removePlugin(plugin)
        plugins.remove(plugin.id) ?: return
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(id: String): T = plugins.getValue(id) as T
}
