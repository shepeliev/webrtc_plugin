package com.shepeliev.webrtc_plugin

import android.util.Log
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterBackend
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import com.shepeliev.webrtc_plugin.plugin.newStringId
import com.shepeliev.webrtc_plugin.webrtc.VideoCapturerFactory
import io.flutter.plugin.common.MethodCall
import org.webrtc.*

class UserMedia(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val videoCapturerFactory: VideoCapturerFactory,
    private val backendRegistry: FlutterBackendRegistry
) : GlobalFlutterBackend {

    private var isInitialized = false
    private var audioSource: AudioSource? = null
    private var videoSource: VideoSource? = null
    private var videoCapturer: CameraVideoCapturer? = null

    override val methodHandlers: Map<String, MethodHandler<*>> =
        mapOf(
            "initializeUserMedia" to ::initializeUserMedia,
            "createLocalMediaStream" to ::createLocalMediaStream,
            "disposeUserMedia" to ::disposeUserMedia
        )

    private fun initializeUserMedia(methodCall: MethodCall): Nothing? {
        check(!isInitialized) { "User media already initialized." }
        val audioConstraints = methodCall.argument<Map<String, Any>>("audio")
        val videoConstraints = methodCall.argument<Map<String, Any>>("video")
        createAudioSource(audioConstraints)
        createVideoSource(videoConstraints)
        createVideoCapturer()
        startCapture(videoConstraints)
        isInitialized = true
        return null
    }

    private fun createVideoSource(constraints: Map<String, Any>?) {
        videoSource = constraints?.let { peerConnectionFactory.createVideoSource(false) }
    }

    private fun createAudioSource(constraints: Map<String, Any>?) {
        audioSource =
            constraints?.let { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    }

    private fun startCapture(videoConstraints: Map<String, Any>?) {
        videoCapturer?.let {
            val params = extractCaptureParams(videoConstraints!!)
            it.startCapture(params.width, params.height, params.fps)
        }
    }

    private fun createVideoCapturer() {
        videoCapturer = videoSource?.let {
            videoCapturerFactory.createCameraVideoCapturer(it.capturerObserver)
        }
    }

    private fun extractCaptureParams(videoConstraints: Map<String, Any>): CaptureParams {
        // TODO: implement respecting min and max values
        val width = videoConstraints.getValue("minWidth") as Int
        val height = videoConstraints.getValue("minHeight") as Int
        val fps = videoConstraints.getValue("minFps") as Int
        return CaptureParams(width, height, fps)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun createLocalMediaStream(methodCall: MethodCall): Map<String, Any> {
        check(isInitialized) { "User media has not bee initialized yet!" }
        val mediaStream = peerConnectionFactory.createLocalMediaStream(
            "LocalMediaStream::${newStringId()}"
        ).apply {
            if (audioSource != null) {
                val track = peerConnectionFactory.createAudioTrack(
                    newStringId(),
                    audioSource
                )
                addTrack(track)
            }
            if (videoSource != null) {
                val track = peerConnectionFactory.createVideoTrack(
                    newStringId(),
                    videoSource
                )
                addTrack(track)
            }
        }
        val flutterMediaStream = MediaStreamBackend(mediaStream, backendRegistry)
        return flutterMediaStream.toMap()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun disposeUserMedia(methodCall: MethodCall): Nothing? {
        dispose()
        return null
    }

    override fun dispose() {
        Log.d(tag, "Disposing $this.")
        videoCapturer?.dispose()
        audioSource?.dispose()
        audioSource = null
        videoSource?.dispose()
        videoSource = null
        isInitialized = false
    }
}

private data class CaptureParams(val width: Int, val height: Int, val fps: Int)
