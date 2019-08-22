package com.shepeliev.webrtc_plugin.plugin

internal interface PluginRegistry {
    val allBackends: Collection<FlutterBackend>

    fun add(backend: FlutterBackend)

    fun remove(backend: FlutterBackend)

    operator fun <T> get(id: String): T
}

internal class DefaultPluginRegistry(private val methodChannelRegistry: MethodChannelRegistry) : PluginRegistry {
    private val plugins = mutableMapOf<String, FlutterBackend>()

    override val allBackends: Collection<FlutterBackend>
        get() = plugins.values

    override fun add(backend: FlutterBackend) {
        plugins += (backend.id to backend)
        methodChannelRegistry.addPlugin(backend)
    }

    override fun remove(backend: FlutterBackend) {
        check(!backend.isGlobal) { "Global backend can't be removed." }
        methodChannelRegistry.removePlugin(backend)
        plugins.remove(backend.id) ?: return
    }

    @Suppress("UNCHECKED_CAST")
    override operator fun <T> get(id: String): T = plugins.getValue(id) as T
}
