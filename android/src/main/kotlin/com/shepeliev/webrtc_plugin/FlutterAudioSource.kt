package com.shepeliev.webrtc_plugin

import com.shepeliev.webrtc_plugin.plugin.*
import io.flutter.plugin.common.MethodCall
import org.webrtc.AudioSource

internal class FlutterAudioSource(
    val audioSource: AudioSource,
    private val backendRegistry: FlutterBackendRegistry
) : FlutterBackend {

    override val id: BackendId = newId()

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "dispose" to ::dispose
    )

    init {
        backendRegistry.add(this)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun dispose(methodCall: MethodCall): Nothing? {
        audioSource.dispose()
        backendRegistry.remove(this)
        return null
    }
}
