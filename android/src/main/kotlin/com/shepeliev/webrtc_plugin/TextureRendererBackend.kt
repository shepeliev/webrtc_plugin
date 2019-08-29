package com.shepeliev.webrtc_plugin

import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.shepeliev.webrtc_plugin.plugin.FlutterBackend
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import com.shepeliev.webrtc_plugin.plugin.newStringId
import com.shepeliev.webrtc_plugin.webrtc.eglBase
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.TextureRegistry
import org.webrtc.EglBase
import org.webrtc.EglRenderer
import org.webrtc.GlRectDrawer
import org.webrtc.VideoFrame
import java.util.concurrent.CountDownLatch

private val TAG = FlutterTextureRenderer::class.java.simpleName

class FlutterTextureRenderer(
    registrar: Registrar,
    private val backendRegistry: FlutterBackendRegistry
) : EglRenderer(""), FlutterBackend {

    private val mainThread = Handler(Looper.getMainLooper())
    private lateinit var textureEntry: TextureRegistry.SurfaceTextureEntry
    private lateinit var texture: SurfaceTexture

    override val id: String = newStringId()
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "dispose" to ::dispose
    )

    val textureId: Long
        get() = textureEntry.id()

    private var disposed = false

    init {
        val countDownLatch = CountDownLatch(1)

        mainThread.post {
            val textures = registrar.textures()
            textureEntry = textures.createSurfaceTexture()
            texture = textureEntry.surfaceTexture()
            createEglSurface(texture)

            // don't init EglRenderer while testing in Robolectric environment
            if (Build.MANUFACTURER != "robolectric") {
                init(eglBase.eglBaseContext, EglBase.CONFIG_PLAIN, GlRectDrawer())
            }
            countDownLatch.countDown()
        }
        countDownLatch.await()
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
        val countDownLatch = CountDownLatch(1)
        mainThread.post {
            release()
            texture.release()
            textureEntry.release()
            countDownLatch.countDown()
        }
        countDownLatch.await()
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
