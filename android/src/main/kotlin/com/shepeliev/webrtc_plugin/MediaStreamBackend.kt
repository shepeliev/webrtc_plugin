package com.shepeliev.webrtc_plugin

import android.util.Log
import com.shepeliev.webrtc_plugin.plugin.*
import io.flutter.plugin.common.MethodCall
import org.webrtc.CameraVideoCapturer
import org.webrtc.MediaStream
import org.webrtc.VideoSink

private val TAG = MediaStreamBackend::class.java.simpleName

class MediaStreamBackend(
    val mediaStream: MediaStream,
    private val videoCapturer: CameraVideoCapturer?,
    private val backendRegistry: FlutterBackendRegistry
) : FlutterBackend {
    override val id: BackendId = newStringId()

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "dispose" to ::disposeInternal,
        "addRenderer" to ::addRenderer,
        "removeRenderer" to ::removeRenderer
    )

    private var disposed = false

    init {
        backendRegistry.add(this)
    }

    fun dispose() {
        if (disposed) return
        Log.d(TAG, "Disposing MediaStreamBackend.")
        removeRenderers()
        videoCapturer?.stopCapture()
        mediaStream.dispose()
        backendRegistry.remove(this)
        disposed = true
    }

    private fun removeRenderers() {
        val renderers = backendRegistry.all.filterIsInstance<VideoSink>()
        mediaStream.videoTracks.forEach { videoTrack ->
            renderers.forEach {
                Log.d(TAG, "Remove renderer $it from $videoTrack")
                videoTrack.removeSink(it)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun disposeInternal(methodCall: MethodCall): Nothing? {
        dispose()
        return null
    }

    private fun addRenderer(methodCall: MethodCall): Nothing? {
        check(!disposed) { "MediaStreamBackend has been disposed" }
        if (mediaStream.videoTracks.isEmpty()) {
            Log.e(TAG, "MediaStream{id: ${mediaStream.id}} doesn't contain video track.")
            return null
        }
        val renderer = getTextureRenderer(methodCall)
        val videoTrack = mediaStream.videoTracks.first()
        videoTrack.addSink(renderer)
        Log.d(TAG, "Added renderer $renderer to $videoTrack")
        return null
    }

    private fun removeRenderer(methodCall: MethodCall): Nothing? {
        check(!disposed) { "MediaStreamBackend has been disposed" }
        if (mediaStream.videoTracks.isEmpty()) {
            Log.e(TAG, "MediaStream{id: ${mediaStream.id}} doesn't contain video track.")
            return null
        }
        val renderer = getTextureRenderer(methodCall)
        val videoTrack = mediaStream.videoTracks.first()
        videoTrack.removeSink(renderer)
        Log.d(TAG, "Removed renderer $renderer to $videoTrack")
        return null
    }

    private fun getTextureRenderer(methodCall: MethodCall): TextureRendererBackend {
        require(methodCall.hasArgument("rendererId")) { "'rendererId' is required." }
        val rendererId = methodCall.argument<String>("rendererId")!!
        return backendRegistry[rendererId]
    }

    fun toMap(): Map<String, Any> {
        val audioTracks = mediaStream.audioTracks.map { mapOf("id" to it.id()) }
        val videoTracks = mediaStream.videoTracks.map { mapOf("id" to it.id()) }
        return mapOf("id" to id, "audioTracks" to audioTracks, "videoTracks" to videoTracks)
    }

    protected fun finalize() {
        if (!disposed) {
            Log.w(TAG, "MediaStreamBackend has not been disposed properly.")
            dispose()
        }
    }
}
