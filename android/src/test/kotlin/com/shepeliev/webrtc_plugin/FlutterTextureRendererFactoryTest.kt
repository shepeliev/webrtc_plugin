package com.shepeliev.webrtc_plugin

import android.graphics.SurfaceTexture
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.shepeliev.webrtc_plugin.plugin.DefaultFlutterBackendRegistry
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
class FlutterTextureRendererFactoryTest {
    @get:Rule val mockitoRule = MockitoJUnit.rule()!!

    @Mock private lateinit var registrar: Registrar
    @Mock private lateinit var textureRegistry: TextureRegistry
    @Mock private lateinit var textureEntry: TextureRegistry.SurfaceTextureEntry
    @Mock private lateinit var texture: SurfaceTexture

    private val textureId = Random(System.currentTimeMillis()).nextLong()
    private lateinit var factory: FlutterTextureRendererFactory

    @Before
    fun setUp() {
        ShadowBuild.setManufacturer("robolectric")
        whenever(registrar.textures()) doReturn textureRegistry
        whenever(textureRegistry.createSurfaceTexture()) doReturn textureEntry
        whenever(textureEntry.id()) doReturn textureId
        whenever(textureEntry.surfaceTexture()) doReturn texture
        WebrtcPlugin.flutterBackendRegistry = DefaultFlutterBackendRegistry(mock())

        factory = FlutterTextureRendererFactory(registrar)
    }

    @Test
    fun isGlobal() {
        assertThat(factory.isGlobal).isTrue()
    }

    @Test
    fun createTextureRenderer() {
        val handler = factory.methodHandlers.getValue("createTextureRenderer")

        @Suppress("UNCHECKED_CAST")
        val result = handler(MethodCall("createTextureRenderer", null)) as Map<String, Any?>

        val plugin: FlutterTextureRenderer = WebrtcPlugin.flutterBackendRegistry[result["id"].toString()]
        assertThat(result).isEqualTo(mapOf("id" to plugin.id, "textureId" to textureId))
    }
}
