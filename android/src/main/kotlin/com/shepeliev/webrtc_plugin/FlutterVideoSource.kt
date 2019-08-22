package com.shepeliev.webrtc_plugin

import android.content.Context
import com.shepeliev.webrtc_plugin.plugin.*
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import com.shepeliev.webrtc_plugin.plugin.newId
import com.shepeliev.webrtc_plugin.webrtc.CameraCapturer
import io.flutter.plugin.common.MethodCall
import org.webrtc.VideoSource

private const val BACK_SIDE = "back"
private const val FRONT_SIDE = "front"
private val ALLOWED_CAMERA_SIDES = listOf(BACK_SIDE, FRONT_SIDE)

internal class FlutterVideoSource(
    private val context: Context,
    val videoSource: VideoSource,
    private val cameraCapturer: CameraCapturer,
    private val backendRegistry: FlutterBackendRegistry
) : FlutterBackend {

    override val id: PluginId = newId()

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "startCapture" to ::startCapture,
        "stopCapture" to ::stopCapture,
        "dispose" to ::dispose
    )

    init {
        backendRegistry.add(this)
    }

    private fun startCapture(methodCall: MethodCall): Nothing? {
        val width: Int = methodCall.argument("width") ?: error("'width' is required")
        val height: Int = methodCall.argument("height") ?: error("'height' is required")
        val fps: Int = methodCall.argument("fps") ?: error("'fps' is required")
        val side: String = methodCall.argument("side") ?: error("'side' is required")
        require(side in ALLOWED_CAMERA_SIDES) { "'side' must be one of $ALLOWED_CAMERA_SIDES" }
        val cameraSide = when (side) {
            BACK_SIDE -> CameraCapturer.CameraSide.BACK
            else -> CameraCapturer.CameraSide.FRONT
        }
        cameraCapturer.startCapture(context, videoSource, width, height, fps, cameraSide)
        return null
    }

    @Suppress("UNUSED_PARAMETER")
    private fun stopCapture(methodCall: MethodCall): Nothing? {
        cameraCapturer.stopCapture()
        return null
    }

    @Suppress("UNUSED_PARAMETER")
    private fun dispose(methodCall: MethodCall): Nothing? {
        cameraCapturer.stopCapture()
        videoSource.dispose()
        backendRegistry.remove(this)
        return null
    }
}
