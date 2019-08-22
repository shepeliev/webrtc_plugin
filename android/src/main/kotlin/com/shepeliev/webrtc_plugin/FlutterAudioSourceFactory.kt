package com.shepeliev.webrtc_plugin

import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterBackend
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import com.shepeliev.webrtc_plugin.plugin.PluginId
import io.flutter.plugin.common.MethodCall
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory

internal class FlutterAudioSourceFactory(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val backendRegistry: FlutterBackendRegistry
) : GlobalFlutterBackend {

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "createAudioSource" to ::createFlutterVideoSource
    )

    @Suppress("UNUSED_PARAMETER")
    private fun createFlutterVideoSource(methodCall: MethodCall): PluginId {
        val videoSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        return FlutterAudioSource(videoSource, backendRegistry).id
    }
}
