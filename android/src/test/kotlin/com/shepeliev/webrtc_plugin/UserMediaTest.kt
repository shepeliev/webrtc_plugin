package com.shepeliev.webrtc_plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.newStringId
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
    @Mock private lateinit var videoCapturer: CameraVideoCapturer
    @Mock private lateinit var backendRegistry: FlutterBackendRegistry
    @Mock private lateinit var audioSource: AudioSource
    @Mock private lateinit var videoSource: VideoSource
    @Mock private lateinit var capturerObserver: CapturerObserver
    @Mock private lateinit var videoTrack: VideoTrack
    @Mock private lateinit var audioTrack: AudioTrack

    private lateinit var userMedia: UserMedia

    @Before
    fun setUp() {
        whenever(peerConnectionFactory.createAudioSource(any())) doReturn audioSource
        whenever(peerConnectionFactory.createVideoSource(any())) doReturn videoSource

        whenever(videoSource.capturerObserver) doReturn capturerObserver

        whenever(videoCapturerFactory.createCameraVideoCapturer(any())) doReturn videoCapturer

        userMedia = UserMedia(
            peerConnectionFactory,
            videoCapturerFactory,
            backendRegistry
        )
    }

    @Test
    fun isGlobal() {
        assertThat(userMedia.isGlobal).isTrue()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun initializeUserMedia() {
        // arrange
        val handler = userMedia.methodHandlers.getValue("initializeUserMedia")
        val videoConstraints = mapOf(
            "minWidth" to 640,
            "maxWidth" to 1280,
            "minHeight" to 480,
            "maxHeight" to 720,
            "minFps" to 15,
            "maxFps" to 30,
            "facingMode" to "user"
        )
        val constraints = mapOf(
            "audio" to emptyMap(),
            "video" to videoConstraints
        )

        // act
        val result = handler(MethodCall("initializeUserMedia", constraints))

        // assert
        assertThat(result).isNull()
        argumentCaptor<MediaConstraints>().apply {
            verify(peerConnectionFactory).createAudioSource(capture())
            assertThat(firstValue.mandatory).isEmpty()
            assertThat(firstValue.optional).isEmpty()
        }
        verify(peerConnectionFactory).createVideoSource(false)
        verify(videoCapturerFactory).createCameraVideoCapturer(capturerObserver)
        verify(videoCapturer).startCapture(640, 480, 15)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun initializeUserMedia_without_audio() {
        // arrange
        val handler = userMedia.methodHandlers.getValue("initializeUserMedia")
        val videoConstraints = mapOf(
            "minWidth" to 640,
            "maxWidth" to 1280,
            "minHeight" to 480,
            "maxHeight" to 720,
            "minFps" to 15,
            "maxFps" to 30,
            "facingMode" to "user"
        )
        val constraints = mapOf(
            "audio" to null,
            "video" to videoConstraints
        )

        // act
        val result = handler(MethodCall("initializeUserMedia", constraints))

        // assert
        assertThat(result).isNull()
        verify(peerConnectionFactory, never()).createAudioSource(any())
        verify(peerConnectionFactory).createVideoSource(false)
        verify(videoCapturerFactory).createCameraVideoCapturer(capturerObserver)
        verify(videoCapturer).startCapture(640, 480, 15)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun initializeUserMedia_without_video() {
        // arrange
        val handler = userMedia.methodHandlers.getValue("initializeUserMedia")
        val constraints = mapOf(
            "audio" to emptyMap<String, Any>(),
            "video" to null
        )

        // act
        val result = handler(MethodCall("initializeUserMedia", constraints))

        // assert
        assertThat(result).isNull()
        argumentCaptor<MediaConstraints>().apply {
            verify(peerConnectionFactory).createAudioSource(capture())
            assertThat(firstValue.mandatory).isEmpty()
            assertThat(firstValue.optional).isEmpty()
        }
        verify(peerConnectionFactory, never()).createVideoSource(any())
        verify(videoCapturerFactory, never()).createCameraVideoCapturer(any())
        verify(videoCapturer, never()).startCapture(any(), any(), any())
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun initializeUserMedia_without_video_neither_audio() {
        // arrange
        val handler = userMedia.methodHandlers.getValue("initializeUserMedia")
        val constraints = mapOf("audio" to null, "video" to null)

        // act
        val result = handler(MethodCall("initializeUserMedia", constraints))

        // assert
        assertThat(result).isNull()
        verify(peerConnectionFactory, never()).createAudioSource(any())
        verify(peerConnectionFactory, never()).createVideoSource(any())
        verify(videoCapturerFactory, never()).createCameraVideoCapturer(any())
        verify(videoCapturer, never()).startCapture(any(), any(), any())
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun createLocalMediaStream() {
        // arrange
        val mediaStream = FakeMediaStream(newStringId())
        whenever(peerConnectionFactory.createLocalMediaStream(any())) doReturn mediaStream
        whenever(peerConnectionFactory.createAudioTrack(any(), any())) doReturn audioTrack
        whenever(peerConnectionFactory.createVideoTrack(any(), any())) doReturn videoTrack
        whenever(videoTrack.id()) doReturn "videoTrack::42"
        whenever(audioTrack.id()) doReturn "audioTrack::42"
        val initializeUserMediaHandler = userMedia.methodHandlers.getValue("initializeUserMedia")
        val videoConstraints = mapOf(
            "minWidth" to 640,
            "maxWidth" to 1280,
            "minHeight" to 480,
            "maxHeight" to 720,
            "minFps" to 15,
            "maxFps" to 30,
            "facingMode" to "user"
        )
        val constraints = mapOf(
            "audio" to emptyMap(),
            "video" to videoConstraints
        )
        initializeUserMediaHandler(MethodCall("initializeUserMedia", constraints))

        // act
        val createLocalMediaStreamHandler =
            userMedia.methodHandlers.getValue("createLocalMediaStream")
        val result = createLocalMediaStreamHandler(
            MethodCall("createLocalMediaStream", null)
        ) as Map<String, Any?>

        // assert
        assertThat(result["id"] as String).isNotEmpty()
        assertThat(result["audioTracks"]).isEqualTo(listOf(mapOf("id" to "audioTrack::42")))
        assertThat(result["videoTracks"]).isEqualTo(listOf(mapOf("id" to "videoTrack::42")))
        assertThat(mediaStream.audioTracks).containsExactly(audioTrack)
        assertThat(mediaStream.videoTracks).containsExactly(videoTrack)
        verify(peerConnectionFactory).createLocalMediaStream(any())
        verify(peerConnectionFactory).createAudioTrack(any(), eq(audioSource))
        verify(peerConnectionFactory).createVideoTrack(any(), eq(videoSource))
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun createLocalMediaStream_without_audio() {
        // arrange
        val mediaStream = FakeMediaStream(newStringId())
        whenever(peerConnectionFactory.createLocalMediaStream(any())) doReturn mediaStream
        whenever(peerConnectionFactory.createVideoTrack(any(), any())) doReturn videoTrack
        whenever(videoTrack.id()) doReturn "videoTrack::42"
        val initializeUserMediaHandler = userMedia.methodHandlers.getValue("initializeUserMedia")
        val videoConstraints = mapOf(
            "minWidth" to 640,
            "maxWidth" to 1280,
            "minHeight" to 480,
            "maxHeight" to 720,
            "minFps" to 15,
            "maxFps" to 30,
            "facingMode" to "user"
        )
        val constraints = mapOf(
            "audio" to null,
            "video" to videoConstraints
        )
        initializeUserMediaHandler(MethodCall("initializeUserMedia", constraints))

        // act
        val createLocalMediaStreamHandler =
            userMedia.methodHandlers.getValue("createLocalMediaStream")
        val result = createLocalMediaStreamHandler(
            MethodCall("createLocalMediaStream", null)
        ) as Map<String, Any?>

        // assert
        assertThat(result["id"] as String).isNotEmpty()
        assertThat(result["audioTracks"] as List<Any>).isEmpty()
        assertThat(result["videoTracks"]).isEqualTo(listOf(mapOf("id" to "videoTrack::42")))
        assertThat(mediaStream.audioTracks).isEmpty()
        assertThat(mediaStream.videoTracks).containsExactly(videoTrack)
        verify(peerConnectionFactory).createLocalMediaStream(any())
        verify(peerConnectionFactory, never()).createAudioTrack(any(), any())
        verify(peerConnectionFactory).createVideoTrack(any(), eq(videoSource))
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun createLocalMediaStream_without_video() {
        // arrange
        val mediaStream = FakeMediaStream(newStringId())
        whenever(peerConnectionFactory.createLocalMediaStream(any())) doReturn mediaStream
        whenever(peerConnectionFactory.createAudioTrack(any(), any())) doReturn audioTrack
        whenever(audioTrack.id()) doReturn "audioTrack::42"
        val initializeUserMediaHandler = userMedia.methodHandlers.getValue("initializeUserMedia")
        val constraints = mapOf(
            "audio" to emptyMap<String, Any>(),
            "video" to null
        )
        initializeUserMediaHandler(MethodCall("initializeUserMedia", constraints))

        // act
        val createLocalMediaStreamHandler =
            userMedia.methodHandlers.getValue("createLocalMediaStream")
        val result = createLocalMediaStreamHandler(
            MethodCall("createLocalMediaStream", null)
        ) as Map<String, Any?>

        // assert
        assertThat(result["id"] as String).isNotEmpty()
        assertThat(result["audioTracks"]).isEqualTo(listOf(mapOf("id" to "audioTrack::42")))
        assertThat(result["videoTracks"] as List<Any>).isEmpty()
        assertThat(mediaStream.audioTracks).containsExactly(audioTrack)
        assertThat(mediaStream.videoTracks).isEmpty()
        verify(peerConnectionFactory).createLocalMediaStream(any())
        verify(peerConnectionFactory).createAudioTrack(any(), eq(audioSource))
        verify(peerConnectionFactory, never()).createVideoTrack(any(), any())
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun createLocalMediaStream_without_video_neither_audio() {
        // arrange
        val mediaStream = FakeMediaStream(newStringId())
        whenever(peerConnectionFactory.createLocalMediaStream(any())) doReturn mediaStream
        val initializeUserMediaHandler = userMedia.methodHandlers.getValue("initializeUserMedia")
        val constraints = mapOf(
            "audio" to null,
            "video" to null
        )
        initializeUserMediaHandler(MethodCall("initializeUserMedia", constraints))

        // act
        val createLocalMediaStreamHandler =
            userMedia.methodHandlers.getValue("createLocalMediaStream")
        val result = createLocalMediaStreamHandler(
            MethodCall("createLocalMediaStream", null)
        ) as Map<String, Any?>

        // assert
        assertThat(result["id"] as String).isNotEmpty()
        assertThat(result["audioTracks"] as List<Any>).isEmpty()
        assertThat(result["videoTracks"] as List<Any>).isEmpty()
        assertThat(mediaStream.audioTracks).isEmpty()
        assertThat(mediaStream.videoTracks).isEmpty()
        verify(peerConnectionFactory).createLocalMediaStream(any())
        verify(peerConnectionFactory, never()).createAudioTrack(any(), any())
        verify(peerConnectionFactory, never()).createVideoTrack(any(), any())
    }

    @Test(expected = IllegalStateException::class)
    @Suppress("UNCHECKED_CAST")
    fun createLocalMediaStream_when_UserMedia_has_not_been_initialized() {
        // act
        val createLocalMediaStreamHandler =
            userMedia.methodHandlers.getValue("createLocalMediaStream")
        createLocalMediaStreamHandler(
            MethodCall("createLocalMediaStream", null)
        ) as Map<String, Any?>
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun disposeUserMedia() {
        // arrange
        val initializeUserMediaHandler = userMedia.methodHandlers.getValue("initializeUserMedia")
        val videoConstraints = mapOf(
            "minWidth" to 640,
            "maxWidth" to 1280,
            "minHeight" to 480,
            "maxHeight" to 720,
            "minFps" to 15,
            "maxFps" to 30,
            "facingMode" to "user"
        )
        val constraints = mapOf(
            "audio" to emptyMap(),
            "video" to videoConstraints
        )
        initializeUserMediaHandler(MethodCall("initializeUserMedia", constraints))

        // act
        val createLocalMediaStreamHandler =
            userMedia.methodHandlers.getValue("disposeUserMedia")
        val result = createLocalMediaStreamHandler(
            MethodCall("disposeUserMedia", null)
        )

        // assert
        assertThat(result).isNull()
        verify(videoCapturer).dispose()
        verify(audioSource).dispose()
        verify(videoSource).dispose()
    }
}

class FakeMediaStream(private  val id: String) : MediaStream(1) {
    override fun addTrack(track: AudioTrack): Boolean {
        audioTracks.add(track)
        return true
    }

    override fun addTrack(track: VideoTrack): Boolean {
        videoTracks.add(track)
        return true
    }

    override fun getId(): String = id
}
