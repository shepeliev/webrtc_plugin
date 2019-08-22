package com.shepeliev.webrtc_plugin

import com.shepeliev.webrtc_plugin.plugin.FlutterPlugin
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import io.flutter.plugin.common.MethodCall
import org.webrtc.VideoSink
import org.webrtc.VideoTrack

internal class FlutterVideoTrack(private val videoTrack: VideoTrack) :
    FlutterPlugin {

    override val id: String = videoTrack.id()
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "addRenderer" to ::addRenderer,
        "removeRenderer" to ::removeRenderer,
        "dispose" to ::dispose
    )

    init {
        WebrtcPlugin.pluginRegistry.add(this)
    }

    fun removeSink(sink: VideoSink) {
        videoTrack.removeSink(sink)
    }

    private fun addRenderer(methodCall: MethodCall): Nothing? {
        val renderer = getRenderer(methodCall)
        videoTrack.addSink(renderer)
        return null
    }

    private fun removeRenderer(methodCall: MethodCall): Nothing? {
        val renderer = getRenderer(methodCall)
        videoTrack.removeSink(renderer)
        return null
    }

    private fun getRenderer(methodCall: MethodCall): FlutterTextureRenderer {
        val rendererId: String = methodCall.argument("rendererId")
            ?: error { "'rendererId' argument required" }
        return WebrtcPlugin.pluginRegistry[rendererId]
    }

    @Suppress("UNUSED_PARAMETER")
    private fun dispose(methodCall: MethodCall): Nothing? {
        videoTrack.dispose()
        WebrtcPlugin.pluginRegistry.remove(this)
        return null
    }
}