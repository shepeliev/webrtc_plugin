package com.shepeliev.webrtc_plugin

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import io.flutter.plugin.common.MethodCall
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner
import org.webrtc.AudioSource
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory

@RunWith(MockitoJUnitRunner::class)
class FlutterAudioSourceFactoryTest {
    @Mock private lateinit var peerConnectionFactory: PeerConnectionFactory
    @Mock private lateinit var audioSource: AudioSource
    @Mock private lateinit var backendRegistry: FlutterBackendRegistry

    private lateinit var factory: FlutterAudioSourceFactory

    @Before
    fun setUp() {
        whenever(peerConnectionFactory.createAudioSource(any())) doReturn audioSource
        factory = FlutterAudioSourceFactory(peerConnectionFactory, backendRegistry)
    }

    @Test
    fun isGlobal() {
        assertThat(factory.isGlobal).isTrue()
    }

    @Test
    fun createAudioSource() {
        val handler = factory.methodHandlers.getValue("createAudioSource")

        val id = handler(MethodCall("createVideoSource", null)) as String

        verify(peerConnectionFactory).createAudioSource(any())
        argumentCaptor<FlutterAudioSource>().apply {
            verify(backendRegistry).add(capture())
            assertThat(firstValue.id).isEqualTo(id)
            assertThat(firstValue.audioSource).isEqualTo(audioSource)
        }
    }
}
