package com.shepeliev.webrtc_plugin

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.webrtc.CameraCapturer
import io.flutter.plugin.common.MethodCall
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.robolectric.RobolectricTestRunner
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoSource

@RunWith(RobolectricTestRunner::class)
class FlutterVideoSourceFactoryTest {
    @get:Rule
    val mockitoRule = MockitoJUnit.rule()!!

    @Mock private lateinit var peerConnectionFactory: PeerConnectionFactory
    @Mock private lateinit var videoSource: VideoSource
    @Mock private lateinit var cameraCapturer: CameraCapturer
    @Mock private lateinit var backendRegistry: FlutterBackendRegistry

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var factory: FlutterVideoSourceFactory

    @Before
    fun setUp() {
        whenever(peerConnectionFactory.createVideoSource(any())) doReturn videoSource
        factory = FlutterVideoSourceFactory(
            app,
            peerConnectionFactory,
            cameraCapturer,
            backendRegistry
        )
    }

    @Test
    fun isGlobal() {
        assertThat(factory.isGlobal).isTrue()
    }

    @Test
    fun createVideoSource_isScreencast_false() {
        val handler = factory.methodHandlers.getValue("createVideoSource")

        val args = mapOf("isScreencast" to false)
        val id = handler(MethodCall("createVideoSource", args)) as String

        verify(peerConnectionFactory).createVideoSource(false)
        argumentCaptor<FlutterVideoSource>().apply {
            verify(backendRegistry).add(capture())
            assertThat(firstValue.id).isEqualTo(id)
            assertThat(firstValue.videoSource).isEqualTo(videoSource)
        }
    }

    @Test
    fun createVideoSource_isScreencast_absent() {
        val handler = factory.methodHandlers.getValue("createVideoSource")

        val id = handler(MethodCall("createVideoSource", null)) as String

        verify(peerConnectionFactory).createVideoSource(false)
        argumentCaptor<FlutterVideoSource>().apply {
            verify(backendRegistry).add(capture())
            assertThat(firstValue.id).isEqualTo(id)
            assertThat(firstValue.videoSource).isEqualTo(videoSource)
        }
    }

    @Test
    fun createVideoSource_isScreencast_true() {
        val handler = factory.methodHandlers.getValue("createVideoSource")

        val args = mapOf("isScreencast" to true)
        val id = handler(MethodCall("createVideoSource", args)) as String

        verify(peerConnectionFactory).createVideoSource(true)
        argumentCaptor<FlutterVideoSource>().apply {
            verify(backendRegistry).add(capture())
            assertThat(firstValue.id).isEqualTo(id)
            assertThat(firstValue.videoSource).isEqualTo(videoSource)
        }
    }
}
