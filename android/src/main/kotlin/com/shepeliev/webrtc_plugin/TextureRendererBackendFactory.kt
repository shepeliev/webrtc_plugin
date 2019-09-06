package com.shepeliev.webrtc_plugin

import android.util.Log
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterBackend
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.webrtc.EglBase

class TextureRendererBackendFactory(
    private val registrar: Registrar,
    private val eglBase: EglBase,
    private val backendRegistry: FlutterBackendRegistry
) : GlobalFlutterBackend {

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "createTextureRenderer" to ::createTextureRenderer
    )

    @Suppress("UNUSED_PARAMETER")
    private fun createTextureRenderer(methodCall: MethodCall): Map<String, Any> =
        TextureRendererBackend(registrar, eglBase, backendRegistry)
            .let { mapOf("id" to it.id, "textureId" to it.textureId) }

    override fun dispose() {
        Log.d(tag, "Disposing $this")
    }
}
