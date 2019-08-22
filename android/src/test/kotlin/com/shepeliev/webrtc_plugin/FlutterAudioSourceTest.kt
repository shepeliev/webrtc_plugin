package com.shepeliev.webrtc_plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import io.flutter.plugin.common.MethodCall
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.webrtc.AudioSource

@RunWith(MockitoJUnitRunner::class)
class FlutterAudioSourceTest {
    @Mock private lateinit var audioSource: AudioSource
    @Mock private lateinit var backendRegistry: FlutterBackendRegistry

    private lateinit var flutterAudioSource: FlutterAudioSource

    @Before
    fun setUp() {
        flutterAudioSource = FlutterAudioSource(audioSource, backendRegistry)
    }

    @Test
    fun constructor() {
        val flutterAudioSource2 = FlutterAudioSource(audioSource, backendRegistry)

        assertThat(flutterAudioSource.id).isNotEqualTo(flutterAudioSource2.id)
        verify(backendRegistry).add(flutterAudioSource)
        verify(backendRegistry).add(flutterAudioSource2)
    }

    @Test
    fun dispose() {
        val handler = flutterAudioSource.methodHandlers.getValue("dispose")

        handler(MethodCall("dispose", null))

        verify(audioSource).dispose()
        verify(backendRegistry).remove(flutterAudioSource)
    }
}
