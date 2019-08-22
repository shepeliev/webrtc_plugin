package com.shepeliev.webrtc_plugin

import android.content.Context
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterBackend
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import com.shepeliev.webrtc_plugin.plugin.BackendId
import com.shepeliev.webrtc_plugin.webrtc.CameraCapturer
import io.flutter.plugin.common.MethodCall
import org.webrtc.PeerConnectionFactory

internal class FlutterVideoSourceFactory(
    private val context: Context,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val cameraCapturer: CameraCapturer,
    private val backendRegistry: FlutterBackendRegistry
) : GlobalFlutterBackend {

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "createVideoSource" to ::createFlutterVideoSource
    )

    private fun createFlutterVideoSource(methodCall: MethodCall): BackendId {
        val isScreencast = methodCall.argument<Boolean>("isScreencast") ?: false
        val videoSource = peerConnectionFactory.createVideoSource(isScreencast)
        return FlutterVideoSource(
            context,
            videoSource,
            cameraCapturer,
            backendRegistry
        ).id
    }
}
