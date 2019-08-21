package com.shepeliev.webrtc_plugin

import android.content.Context
import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterPlugin
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import com.shepeliev.webrtc_plugin.plugin.PluginId
import com.shepeliev.webrtc_plugin.webrtc.CameraCapturer
import io.flutter.plugin.common.MethodCall
import org.webrtc.PeerConnectionFactory

internal class FlutterVideoSourceFactory(
    private val context: Context,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val cameraCapturer: CameraCapturer
) : GlobalFlutterPlugin {

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "createVideoSource" to ::createFlutterVideoSource
    )

    private fun createFlutterVideoSource(methodCall: MethodCall): PluginId {
        val isScreencast = methodCall.argument<Boolean>("isScreencast") ?: false
        val videoSource = peerConnectionFactory.createVideoSource(isScreencast)
        return FlutterVideoSource(context, videoSource, cameraCapturer).id
    }
}
