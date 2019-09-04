package com.shepeliev.webrtc_plugin

import android.graphics.SurfaceTexture
import android.os.Build
import android.util.Log
import com.shepeliev.webrtc_plugin.plugin.*
import com.shepeliev.webrtc_plugin.webrtc.eglBase
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.TextureRegistry
import org.webrtc.EglBase
import org.webrtc.EglRenderer
import org.webrtc.GlRectDrawer
import org.webrtc.VideoFrame

class TextureRendererBackend(
    registrar: Registrar,
    private val backendRegistry: FlutterBackendRegistry
) : EglRenderer(""), FlutterBackend {

    private lateinit var textureEntry: TextureRegistry.SurfaceTextureEntry
    private lateinit var texture: SurfaceTexture

    override val id: String = newStringId()
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "dispose" to ::disposeHandler
    )

    val textureId: Long
        get() = textureEntry.id()

    private var disposed = false

    init {
        uiThread {
            val textures = registrar.textures()
            textureEntry = textures.createSurfaceTexture()
            texture = textureEntry.surfaceTexture()
            createEglSurface(texture)

            // don't init EglRenderer while testing in Robolectric environment
            if (Build.MANUFACTURER != "robolectric") {
                init(eglBase.eglBaseContext, EglBase.CONFIG_PLAIN, GlRectDrawer())
            }
        }.await()
        registrar.addViewDestroyListener {
            dispose()
            true
        }
        backendRegistry.add(this)
    }

    override fun onFrame(frame: VideoFrame) {
        texture.setDefaultBufferSize(frame.rotatedWidth, frame.rotatedHeight)
        super.onFrame(frame)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun disposeHandler(methodCall: MethodCall): Nothing? {
        dispose()
        return null
    }

    override fun dispose() {
        if (disposed) return
        Log.d(tag, "Disposing $this.")
        removeRendererFromMediaStreams()
        release()
        backendRegistry.remove(this)
        disposed = true
        uiThread {
            texture.release()
            textureEntry.release()
        }
    }

    private fun removeRendererFromMediaStreams() {
        backendRegistry.all
            .filterIsInstance(MediaStreamBackend::class.java)
            .forEach { it.removeTextureRenderer(this) }
    }

    protected fun finalize() {
        if (!disposed) {
            Log.w(tag, "$this has not been disposed properly.")
            dispose()
        }
    }
}
