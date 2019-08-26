package com.shepeliev.webrtc_plugin

import android.graphics.SurfaceTexture
import android.os.Build
import android.util.Log
import com.shepeliev.webrtc_plugin.plugin.FlutterBackend
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import com.shepeliev.webrtc_plugin.plugin.newId
import com.shepeliev.webrtc_plugin.webrtc.eglBase
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.TextureRegistry
import org.webrtc.EglBase
import org.webrtc.EglRenderer
import org.webrtc.GlRectDrawer
import org.webrtc.VideoFrame

private val TAG = FlutterTextureRenderer::class.java.simpleName

internal class FlutterTextureRenderer(
    registrar: Registrar,
    private val backendRegistry: FlutterBackendRegistry
) : EglRenderer(""), FlutterBackend {
    private val textureEntry: TextureRegistry.SurfaceTextureEntry
    private val texture: SurfaceTexture

    override val id: String = newId()
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "dispose" to ::dispose
    )

    val textureId: Long
        get() = textureEntry.id()

    private var disposed = false

    init {
        val textures = registrar.textures()
        textureEntry = textures.createSurfaceTexture()
        texture = textureEntry.surfaceTexture()
        createEglSurface(texture)

        // don't init EglRenderer while testing in Robolectric environment
        if (Build.MANUFACTURER != "robolectric") {
            init(eglBase.eglBaseContext, EglBase.CONFIG_PLAIN, GlRectDrawer())
        }

        backendRegistry.add(this)
    }

    override fun onFrame(frame: VideoFrame) {
        texture.setDefaultBufferSize(frame.rotatedWidth, frame.rotatedHeight)
        super.onFrame(frame)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun dispose(methodCall: MethodCall): Nothing? {
        disposeInternal()
        return null
    }

    private fun disposeInternal() {
        if (disposed) return
        Log.d(TAG, "Disposing $this.")
        release()
        texture.release()
        textureEntry.release()
        backendRegistry.remove(this)
        disposed = true
    }

    protected fun finalize() {
        if (!disposed) {
            Log.w(TAG, "$this has not been disposed properly.")
            disposeInternal()
        }
    }
}
