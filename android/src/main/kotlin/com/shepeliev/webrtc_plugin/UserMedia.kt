package com.shepeliev.webrtc_plugin

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterBackend
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import com.shepeliev.webrtc_plugin.plugin.newStringId
import com.shepeliev.webrtc_plugin.webrtc.VideoCapturerFactory
import io.flutter.plugin.common.MethodCall
import org.webrtc.AudioSource
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoSource

class UserMedia(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val videoCapturerFactory: VideoCapturerFactory,
    private val backendRegistry: FlutterBackendRegistry,
    private val mediaStreamBackendFactory: MediaStreamBackendFactory = MediaStreamBackendFactory(
        peerConnectionFactory,
        videoCapturerFactory,
        backendRegistry
    )
) : GlobalFlutterBackend {

    override val methodHandlers: Map<String, MethodHandler<*>> =
        mapOf("getUserMedia" to ::getUserMedia)

    private fun getUserMedia(methodCall: MethodCall): Map<String, Any> {
        require(methodCall.hasArgument("audio") || methodCall.hasArgument("video")) {
            "At least audio or video media must be requested."
        }

        val videoConstraints = methodCall.argument<Map<String, Any>>("video")
        val videoSource = videoConstraints
            ?.let { peerConnectionFactory.createVideoSource(false) }
        val audioSource = createAudioSource(methodCall)
        val mediaStreamBackend = mediaStreamBackendFactory.createMediaStreamBackend(
            videoConstraints,
            videoSource,
            audioSource
        )
        val mediaStream = mediaStreamBackend.mediaStream
        return mapOf(
            "id" to mediaStreamBackend.id,
            "audioTracks" to mediaStream.audioTracks.map { mapOf("id" to it.id()) },
            "videoTracks" to mediaStream.videoTracks.map { mapOf("id" to it.id()) }
        )
    }

    @VisibleForTesting
    fun createAudioSource(methodCall: MethodCall): AudioSource? {
        val audioEnabled = methodCall.argument<Boolean>("audio") ?: return null
        return audioEnabled
            .takeIf { it }
            ?.let { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    }

    override fun dispose() {
        Log.d(tag, "Disposing $this.")
    }

    class MediaStreamBackendFactory(
        private val peerConnectionFactory: PeerConnectionFactory,
        private val videoCapturerFactory: VideoCapturerFactory,
        private val backendRegistry: FlutterBackendRegistry
    ) {
        fun createMediaStreamBackend(
            videoConstraints: Map<String, Any>?,
            videoSource: VideoSource?,
            audioSource: AudioSource?
        ): MediaStreamBackend {
            check(audioSource != null || videoSource != null)
            check(videoSource == null || videoConstraints != null)
            val audioTrack = audioSource?.let {
                peerConnectionFactory.createAudioTrack(newStringId(), it)
            }
            val videoTrack = videoSource?.let {
                peerConnectionFactory.createVideoTrack(newStringId(), it)
            }
            val videoCapturer = videoSource?.let {
                videoCapturerFactory.createCameraVideoCapturer(
                    videoConstraints!!,
                    it.capturerObserver
                )
            }
            val mediaStream = peerConnectionFactory
                .createLocalMediaStream("LocalMediaStream::${newStringId()}")
                .apply {
                    audioTrack?.let { addTrack(it) }
                    videoTrack?.let { addTrack(it) }
                }
            return MediaStreamBackend(mediaStream, videoCapturer, backendRegistry)
        }
    }
}
