package com.shepeliev.webrtc_plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.webrtc.VideoCapturerFactory
import io.flutter.plugin.common.MethodCall
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.webrtc.*

@RunWith(MockitoJUnitRunner::class)
class UserMediaTest {

    @Mock private lateinit var peerConnectionFactory: PeerConnectionFactory
    @Mock private lateinit var videoCapturerFactory: VideoCapturerFactory
    @Mock private lateinit var backendRegistry: FlutterBackendRegistry
    @Mock private lateinit var audioSource: AudioSource
    @Mock private lateinit var videoSource: VideoSource
    @Mock private lateinit var userMediaStreamFactory: UserMedia.MediaStreamBackendFactory
    @Mock private lateinit var videoTrack: VideoTrack
    @Mock private lateinit var audioTrack: AudioTrack

    private lateinit var userMedia: UserMedia

    @Before
    fun setUp() {
        whenever(videoTrack.id()) doReturn "videoTrack::42"
        whenever(audioTrack.id()) doReturn "audioTrack::42"
        userMedia = UserMedia(
            peerConnectionFactory,
            videoCapturerFactory,
            backendRegistry,
            userMediaStreamFactory
        )
    }

    @Test
    fun isGlobal() {
        assertThat(userMedia.isGlobal).isTrue()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun getUserMedia() {
        stubMediaStreamBackendFactory()
        val handler = userMedia.methodHandlers.getValue("getUserMedia")

        val videoConstraints = mapOf(
            "width" to mapOf("min" to 640, "ideal" to 1280, "max" to 1280),
            "height" to mapOf("min" to 480, "ideal" to 720, "max" to 720),
            "framerate" to mapOf("min" to 15, "ideal" to 30, "max" to 30),
            "facingMode" to "user"
        )
        val constraints = mapOf("audio" to true, "video" to videoConstraints)
        val resultMap = handler(MethodCall("getUserMedia", constraints)) as Map<String, Any>

        assertThat(resultMap).isEqualTo(
            mapOf(
                "id" to "42",
                "videoTracks" to listOf(mapOf("id" to "videoTrack::42")),
                "audioTracks" to listOf(mapOf("id" to "audioTrack::42"))
            )
        )
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun getUserMedia_without_audio() {
        stubMediaStreamBackendFactory()
        val handler = userMedia.methodHandlers.getValue("getUserMedia")

        val videoConstraints = mapOf(
            "width" to mapOf("min" to 640, "ideal" to 1280, "max" to 1280),
            "height" to mapOf("min" to 480, "ideal" to 720, "max" to 720),
            "framerate" to mapOf("min" to 15, "ideal" to 30, "max" to 30),
            "facingMode" to "user"
        )
        val constraints = mapOf("video" to videoConstraints)
        val resultMap = handler(MethodCall("getUserMedia", constraints)) as Map<String, Any>

        assertThat(resultMap).isEqualTo(
            mapOf(
                "id" to "42",
                "videoTracks" to listOf(mapOf("id" to "videoTrack::42")),
                "audioTracks" to emptyList<Map<String, Any>>()
            )
        )
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun getUserMedia_without_video() {
        stubMediaStreamBackendFactory()
        val handler = userMedia.methodHandlers.getValue("getUserMedia")

        val constraints = mapOf("audio" to true)
        val resultMap = handler(MethodCall("getUserMedia", constraints)) as Map<String, Any>

        assertThat(resultMap).isEqualTo(
            mapOf(
                "id" to "42",
                "videoTracks" to emptyList<Map<String, Any>>(),
                "audioTracks" to listOf(mapOf("id" to "audioTrack::42"))
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun getUserMedia_without_video_either_audio() {
        stubMediaStreamBackendFactory()
        val handler = userMedia.methodHandlers.getValue("getUserMedia")

        handler(MethodCall("getUserMedia", null))
    }

    private fun stubMediaStreamBackendFactory() {
        whenever(peerConnectionFactory.createVideoSource(any())) doReturn videoSource
        whenever(peerConnectionFactory.createAudioSource(any())) doReturn audioSource
        whenever(userMediaStreamFactory.createMediaStreamBackend(anyOrNull(), anyOrNull(), anyOrNull())) doAnswer {
            val videoTrackArg = it.arguments[1]?.let { this.videoTrack }
            val audioTrackArg = it.arguments[2]?.let { this.audioTrack }
            stubMediaStreamBackend(videoTrackArg, audioTrackArg)
        }
    }

    private fun stubMediaStreamBackend(
        videoTrack: VideoTrack?,
        audioTrack: AudioTrack?
    ): MediaStreamBackend = mock {
            on { id } doReturn "42"
            on { mediaStream } doReturn createFakeMediaStream(videoTrack, audioTrack)
        }

    private fun createFakeMediaStream(
        videoTrack: VideoTrack?,
        audioTrack: AudioTrack?
    ): MediaStream = MediaStream(1).apply {
        videoTrack?.let { videoTracks.add(it) }
        audioTrack?.let { audioTracks.add(it) }
    }


    @Test
    fun createAudioSource_audio_enabled() {
        whenever(peerConnectionFactory.createAudioSource(any())) doReturn audioSource
        val methodCall = MethodCall("getUserMedia", mapOf("audio" to true))

        assertThat(userMedia.createAudioSource(methodCall)).isEqualTo(audioSource)
    }

    @Test
    fun createAudioSource_audio_disabled() {
        val methodCall = MethodCall("getUserMedia", mapOf("audio" to false))

        assertThat(userMedia.createAudioSource(methodCall)).isNull()
    }

    @Test
    fun createAudioSource_audio_null() {
        val methodCall = MethodCall("getUserMedia", mapOf("audio" to null))

        assertThat(userMedia.createAudioSource(methodCall)).isNull()
    }

    @Test
    fun createAudioSource_audio_absent() {
        val methodCall = MethodCall("getUserMedia", null)

        assertThat(userMedia.createAudioSource(methodCall)).isNull()
    }
}
