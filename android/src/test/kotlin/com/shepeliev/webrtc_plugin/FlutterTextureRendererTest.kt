package com.shepeliev.webrtc_plugin

import android.graphics.SurfaceTexture
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.TextureRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowBuild
import org.webrtc.VideoFrame
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
class FlutterTextureRendererTest {
    @get:Rule
    val mockitoRule = MockitoJUnit.rule()!!

    @Mock private lateinit var registrar: Registrar
    @Mock private lateinit var textureRegistry: TextureRegistry
    @Mock private lateinit var textureEntry: TextureRegistry.SurfaceTextureEntry
    @Mock private lateinit var texture: SurfaceTexture
    @Mock private lateinit var backendRegistry: FlutterBackendRegistry

    private val textureId = Random(1).nextLong()
    private lateinit var renderer: FlutterTextureRenderer

    @Before
    fun setUp() {
        ShadowBuild.setManufacturer("robolectric")
        whenever(registrar.textures()) doReturn textureRegistry
        whenever(textureRegistry.createSurfaceTexture()) doReturn textureEntry
        whenever(textureEntry.surfaceTexture()) doReturn texture

        renderer = FlutterTextureRenderer(registrar, backendRegistry)
    }

    @Test
    fun constructor() {
        verify(backendRegistry).add(renderer)
    }

    @Test
    fun getId() {
        assertThat(renderer.id).isNotEmpty()
    }

    @Test
    fun getTextureId() {
        whenever(textureEntry.id()) doReturn textureId
        assertThat(renderer.textureId).isEqualTo(textureId)
    }

    @Test
    fun onFrame() {
        val buffer = mock<VideoFrame.I420Buffer> {
            on { width } doReturn 1280
            on { height } doReturn 720
        }
        val frame = VideoFrame(buffer, 0, 0)

        renderer.onFrame(frame)

        verify(texture).setDefaultBufferSize(1280, 720)
    }

    @Test
    fun dispose() {
        val flutterVideoTrack = mock<FlutterVideoTrack>()
        whenever(backendRegistry.all) doReturn listOf(flutterVideoTrack)

        val handler = renderer.methodHandlers.getValue("dispose")

        handler(MethodCall("dispose", null))

        verify(texture).release()
        verify(textureEntry).release()
        verify(flutterVideoTrack).removeSink(renderer)
        verify(backendRegistry).remove(renderer)
    }
}
