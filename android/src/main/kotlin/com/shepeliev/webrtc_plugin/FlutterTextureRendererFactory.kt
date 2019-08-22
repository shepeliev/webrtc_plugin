package com.shepeliev.webrtc_plugin

import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterBackend
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar

internal class FlutterTextureRendererFactory(private val registrar: Registrar) :
    GlobalFlutterBackend {

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "createTextureRenderer" to ::createTextureRenderer
    )

    @Suppress("UNUSED_PARAMETER")
    private fun createTextureRenderer(methodCall: MethodCall): Map<String, Any> =
        FlutterTextureRenderer(registrar)
            .let { mapOf("id" to it.id, "textureId" to it.textureId) }
}
