package com.shepeliev.webrtc_plugin.plugin

interface FlutterBackendRegistry {
    val all: Collection<FlutterBackend>

    fun add(backend: FlutterBackend)

    fun remove(backend: FlutterBackend)

    operator fun <T> get(id: String): T
}

class DefaultFlutterBackendRegistry(
//    private val methodChannelRegistry: MethodChannelRegistry
) : FlutterBackendRegistry {
    var methodChannelRegistry: MethodChannelRegistry? = null
        set(value) {
            if (value != null) {
                plugins.values.forEach { value.addFlutterBackend(it) }
            }
            field = value
        }

    private val plugins = mutableMapOf<String, FlutterBackend>()

    override val all: Collection<FlutterBackend>
        get() = plugins.values

    override fun add(backend: FlutterBackend) {
        plugins += (backend.id to backend)
        methodChannelRegistry!!.addFlutterBackend(backend)
    }

    override fun remove(backend: FlutterBackend) {
        check(!backend.isGlobal) { "Global backend can't be removed." }
        methodChannelRegistry!!.removePlugin(backend)
        plugins.remove(backend.id) ?: return
    }

    @Suppress("UNCHECKED_CAST")
    override operator fun <T> get(id: String): T = plugins.getValue(id) as T
}
