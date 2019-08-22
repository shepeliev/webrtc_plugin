package com.shepeliev.webrtc_plugin.plugin

internal interface PluginRegistry {
    val allPlugins: Collection<FlutterPlugin>

    fun add(plugin: FlutterPlugin)

    fun remove(plugin: FlutterPlugin)

    operator fun <T> get(id: String): T
}

internal class DefaultPluginRegistry(private val methodChannelRegistry: MethodChannelRegistry) : PluginRegistry {
    private val plugins = mutableMapOf<String, FlutterPlugin>()

    override val allPlugins: Collection<FlutterPlugin>
        get() = plugins.values

    override fun add(plugin: FlutterPlugin) {
        plugins += (plugin.id to plugin)
        methodChannelRegistry.addPlugin(plugin)
    }

    override fun remove(plugin: FlutterPlugin) {
        check(!plugin.isGlobal) { "Global plugin can't be removed." }
        methodChannelRegistry.removePlugin(plugin)
        plugins.remove(plugin.id) ?: return
    }

    @Suppress("UNCHECKED_CAST")
    override operator fun <T> get(id: String): T = plugins.getValue(id) as T
}
