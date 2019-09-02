package com.shepeliev.webrtc_plugin

import android.graphics.SurfaceTexture
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
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
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
class TextureRendererBackendFactoryTest {
    @get:Rule val mockitoRule = MockitoJUnit.rule()!!

    @Mock private lateinit var registrar: Registrar
    @Mock private lateinit var textureRegistry: TextureRegistry
    @Mock private lateinit var textureEntry: TextureRegistry.SurfaceTextureEntry
    @Mock private lateinit var texture: SurfaceTexture
    @Mock private lateinit var backendRegistry: FlutterBackendRegistry

    private val textureId = Random(System.currentTimeMillis()).nextLong()
    private lateinit var backendFactory: TextureRendererBackendFactory

    @Before
    fun setUp() {
        ShadowBuild.setManufacturer("robolectric")
        whenever(registrar.textures()) doReturn textureRegistry
        whenever(textureRegistry.createSurfaceTexture()) doReturn textureEntry
        whenever(textureEntry.id()) doReturn textureId
        whenever(textureEntry.surfaceTexture()) doReturn texture

        backendFactory = TextureRendererBackendFactory(registrar, backendRegistry)
    }

    @Test
    fun isGlobal() {
        assertThat(backendFactory.isGlobal).isTrue()
    }

    @Test
    fun createTextureRenderer() {
        val handler = backendFactory.methodHandlers.getValue("createTextureRenderer")

        @Suppress("UNCHECKED_CAST")
        val result = handler(MethodCall("createTextureRenderer", null)) as Map<String, Any?>

        argumentCaptor<TextureRendererBackend>().apply {
            verify(backendRegistry).add(capture())
            assertThat(firstValue.id).isEqualTo(result["id"])
            assertThat(firstValue.textureId).isEqualTo(result["textureId"])
            assertThat(result).isEqualTo(mapOf("id" to firstValue.id, "textureId" to textureId))
        }
    }
}
