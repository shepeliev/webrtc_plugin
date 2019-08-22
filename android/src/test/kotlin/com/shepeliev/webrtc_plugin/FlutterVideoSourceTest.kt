package com.shepeliev.webrtc_plugin

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.shepeliev.webrtc_plugin.plugin.DefaultFlutterBackendRegistry
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
import org.webrtc.VideoSource

@RunWith(RobolectricTestRunner::class)
class FlutterVideoSourceTest {
    @get:Rule val mockitoRule = MockitoJUnit.rule()!!

    @Mock private lateinit var videoSource: VideoSource
    @Mock private lateinit var cameraCapturer: CameraCapturer
    @Mock private lateinit var backendRegistry: FlutterBackendRegistry

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var flutterVideoSource: FlutterVideoSource

    @Before
    fun setUp() {
        flutterVideoSource = FlutterVideoSource(app, videoSource, cameraCapturer, backendRegistry)
    }

    @Test
    fun constructor() {
        val flutterVideoSource2 = FlutterVideoSource(app, videoSource, cameraCapturer, backendRegistry)

        assertThat(flutterVideoSource.id).isNotEqualTo(flutterVideoSource2.id)
        verify(backendRegistry).add(flutterVideoSource)
        verify(backendRegistry).add(flutterVideoSource2)
    }

    @Test
    fun startCapture_back_camera() {
        val handler = flutterVideoSource.methodHandlers.getValue("startCapture")

        val args = mapOf(
            "width" to 1280,
            "height" to 720,
            "fps" to 30,
            "side" to "back"
        )
        handler(MethodCall("startCapture", args))

        verify(cameraCapturer).startCapture(
            app,
            videoSource,
            1280,
            720,
            30,
            CameraCapturer.CameraSide.BACK
        )
    }

    @Test
    fun startCapture_front_camera() {
        val handler = flutterVideoSource.methodHandlers.getValue("startCapture")

        val args = mapOf(
            "width" to 1280,
            "height" to 720,
            "fps" to 30,
            "side" to "front"
        )
        handler(MethodCall("startCapture", args))

        verify(cameraCapturer).startCapture(
            app,
            videoSource,
            1280,
            720,
            30,
            CameraCapturer.CameraSide.FRONT
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun startCapture_incorrect_camera_side() {
        val handler = flutterVideoSource.methodHandlers.getValue("startCapture")

        val args = mapOf(
            "width" to 1280,
            "height" to 720,
            "fps" to 30,
            "side" to "rear"
        )
        handler(MethodCall("startCapture", args))
    }

    @Test
    fun stopCapture() {
        val handler = flutterVideoSource.methodHandlers.getValue("stopCapture")

        handler(MethodCall("stopCapture", null))

        verify(cameraCapturer).stopCapture()
    }

    @Test
    fun dispose() {
        val handler = flutterVideoSource.methodHandlers.getValue("dispose")

        handler(MethodCall("dispose", null))

        verify(cameraCapturer).stopCapture()
        verify(videoSource).dispose()
        verify(backendRegistry).remove(flutterVideoSource)
    }
}
