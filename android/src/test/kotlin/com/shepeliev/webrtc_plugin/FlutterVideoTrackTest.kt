package com.shepeliev.webrtc_plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.shepeliev.webrtc_plugin.plugin.DefaultFlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.newId
import io.flutter.plugin.common.MethodCall
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowBuild
import org.webrtc.VideoSink
import org.webrtc.VideoTrack

@RunWith(RobolectricTestRunner::class)
class FlutterVideoTrackTest {
    @get:Rule val mockitoRule = MockitoJUnit.rule()!!

    @Mock private lateinit var videoTrack: VideoTrack

    private lateinit var flutterVideoTrack: FlutterVideoTrack

    @Before
    fun setUp() {
        ShadowBuild.setManufacturer("robolectric")
        WebrtcPlugin.flutterBackendRegistry = DefaultFlutterBackendRegistry(mock())
        whenever(videoTrack.id()) doReturn newId()
        flutterVideoTrack = FlutterVideoTrack(videoTrack)
    }

    @Test
    fun constructor() {
        assertThat(WebrtcPlugin.flutterBackendRegistry.allBackends).contains(flutterVideoTrack)
    }

    @Test
    fun removeSink() {
        val sink = mock<VideoSink>()

        flutterVideoTrack.removeSink(sink)

        verify(videoTrack).removeSink(sink)
    }

    @Test
    fun addRenderer() {
        val rendererId = newId()
        val renderer = mock<FlutterTextureRenderer> {
            on { id } doReturn rendererId
        }
        WebrtcPlugin.flutterBackendRegistry.add(renderer)

        val handler = flutterVideoTrack.methodHandlers.getValue("addRenderer")
        handler(MethodCall("addRenderer", mapOf("rendererId" to rendererId)))

        verify(videoTrack).addSink(renderer)
    }

    @Test
    fun removeRenderer() {
        val rendererId = newId()
        val renderer = mock<FlutterTextureRenderer> {
            on { id } doReturn rendererId
        }
        WebrtcPlugin.flutterBackendRegistry.add(renderer)

        val handler = flutterVideoTrack.methodHandlers.getValue("removeRenderer")
        handler(MethodCall("removeRenderer", mapOf("rendererId" to rendererId)))

        verify(videoTrack).removeSink(renderer)
    }

    @Test
    fun dispose() {
        val handler = flutterVideoTrack.methodHandlers.getValue("dispose")
        handler(MethodCall("dispose", null))

        verify(videoTrack).dispose()
        assertThat(WebrtcPlugin.flutterBackendRegistry.allBackends).isEmpty()
    }
}
