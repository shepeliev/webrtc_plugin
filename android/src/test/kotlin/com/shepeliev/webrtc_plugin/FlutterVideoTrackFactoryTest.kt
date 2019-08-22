package com.shepeliev.webrtc_plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.newId
import io.flutter.plugin.common.MethodCall
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

@RunWith(MockitoJUnitRunner::class)
class FlutterVideoTrackFactoryTest {
    @Mock private lateinit var peerConnectionFactory: PeerConnectionFactory
    @Mock private lateinit var flutterVideoSource: FlutterVideoSource
    @Mock private lateinit var videoSource: VideoSource
    @Mock private lateinit var videoTrack: VideoTrack
    @Mock private lateinit var backendRegistry: FlutterBackendRegistry

    private val videoSourceId = newId()
    private lateinit var factory: FlutterVideoTrackFactory

    @Before
    fun setUp() {
        whenever(backendRegistry.get<FlutterVideoSource>(videoSourceId)) doReturn flutterVideoSource
        whenever(peerConnectionFactory.createVideoTrack(any(), any())) doReturn videoTrack
        whenever(flutterVideoSource.videoSource) doReturn videoSource

        factory = FlutterVideoTrackFactory(peerConnectionFactory, backendRegistry)
    }

    @Test
    fun isGlobal() {
        assertThat(factory.isGlobal).isTrue()
    }

    @Test
    fun createVideoTrack() {
        val videoTrackId = newId()
        whenever(videoTrack.id()) doReturn videoTrackId
        val handler = factory.methodHandlers.getValue("createVideoTrack")

        val args = mapOf("videoSourceId" to videoSourceId)
        val id = handler(MethodCall("createVideoTrack", args)) as String

        verify(peerConnectionFactory).createVideoTrack(any(), eq(videoSource))
        assertThat(id).isEqualTo(videoTrackId)
        argumentCaptor<FlutterVideoTrack>().apply {
            verify(backendRegistry).add(capture())
            assertThat(firstValue.id).isEqualTo(id)
        }
    }
}
