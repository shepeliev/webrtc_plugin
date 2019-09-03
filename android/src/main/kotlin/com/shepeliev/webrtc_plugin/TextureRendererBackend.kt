package com.shepeliev.webrtc_plugin

import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.UiThread
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

private val TAG = TextureRendererBackend::class.java.simpleName

class TextureRendererBackend(
    registrar: Registrar,
    private val backendRegistry: FlutterBackendRegistry
) : EglRenderer(""), FlutterBackend {

    private val mainThread = Handler(Looper.getMainLooper())
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
            registrar.addViewDestroyListener {
                disposeInternal()
                true
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
    private fun disposeHandler(methodCall: MethodCall): Nothing? {
        disposeInternal()
        return null
    }

    private fun disposeInternal() {
        if (Thread.currentThread() == mainThread.looper.thread) {
            dispose()
        } else {
            val countDownLatch = CountDownLatch(1)
            mainThread.post {
                dispose()
                countDownLatch.countDown()
            }
            countDownLatch.await()
        }
        backendRegistry.remove(this)
        disposed = true
    }

    @UiThread
    private fun dispose() {
        if (disposed) return
        Log.d(TAG, "Disposing $this.")
        removeRendererFromMediaStreams()
        release()
        texture.release()
        textureEntry.release()
    }

    private fun removeRendererFromMediaStreams() {
        backendRegistry.all
            .filterIsInstance(MediaStreamBackend::class.java)
            .forEach { it.removeTextureRenderer(this) }
    }

    protected fun finalize() {
        if (!disposed) {
            Log.w(TAG, "$this has not been disposed properly.")
            disposeInternal()
        }
    }
}
