package com.shepeliev.webrtc_plugin

import com.shepeliev.webrtc_plugin.plugin.*
import io.flutter.plugin.common.MethodCall
import org.webrtc.PeerConnectionFactory

internal class FlutterVideoTrackFactory(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val backendRegistry: FlutterBackendRegistry
) : GlobalFlutterBackend {

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "createVideoTrack" to ::createVideoTrack
    )

    private fun createVideoTrack(methodCall: MethodCall): PluginId {
        val videoSource = getFlutterVideoSource(methodCall).videoSource
        val videoTrack = peerConnectionFactory.createVideoTrack(newId(), videoSource)
        return FlutterVideoTrack(videoTrack, backendRegistry).id
    }

    private fun getFlutterVideoSource(methodCall: MethodCall): FlutterVideoSource {
        val id: String = methodCall.argument("videoSourceId")
            ?: error("'videoSourceId' is required")
        return backendRegistry[id]
    }
}
