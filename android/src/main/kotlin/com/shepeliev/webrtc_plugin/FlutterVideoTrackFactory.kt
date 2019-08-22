package com.shepeliev.webrtc_plugin

import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterBackend
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import com.shepeliev.webrtc_plugin.plugin.PluginId
import com.shepeliev.webrtc_plugin.plugin.newId
import io.flutter.plugin.common.MethodCall
import org.webrtc.PeerConnectionFactory

internal class FlutterVideoTrackFactory(private val peerConnectionFactory: PeerConnectionFactory) :
    GlobalFlutterBackend {

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "createVideoTrack" to ::createVideoTrack
    )

    private fun createVideoTrack(methodCall: MethodCall): PluginId {
        val videoSource = getFlutterVideoSource(methodCall).videoSource
        val videoTrack = peerConnectionFactory.createVideoTrack(newId(), videoSource)
        return FlutterVideoTrack(videoTrack).id
    }

    private fun getFlutterVideoSource(methodCall: MethodCall): FlutterVideoSource {
        val id = methodCall.argument<String>("videoSourceId")
            ?: error("'videoSourceId' is required")
        return WebrtcPlugin.flutterBackendRegistry[id]
    }
}
