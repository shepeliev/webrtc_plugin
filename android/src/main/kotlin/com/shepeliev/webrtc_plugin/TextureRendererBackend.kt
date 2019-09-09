package com.shepeliev.webrtc_plugin

import android.graphics.SurfaceTexture
import android.os.Build
import android.util.Log
import com.shepeliev.webrtc_plugin.plugin.*
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.TextureRegistry
import org.webrtc.EglBase
import org.webrtc.EglRenderer
import org.webrtc.GlRectDrawer
import org.webrtc.VideoFrame

class TextureRendererBackend(
    registrar: Registrar,
    private val eglBase: EglBase,
    private val backendRegistry: FlutterBackendRegistry
) : EglRenderer(""), FlutterBackend {

    override val id: String = newStringId()
    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "dispose" to ::disposeHandler
    )

    val textureId: Long
        get() = textureEntry.id()

    private lateinit var textureEntry: TextureRegistry.SurfaceTextureEntry
    private lateinit var texture: SurfaceTexture
    private val eventChannel = EventChannel(
        registrar.messenger(),
        "$METHOD_CHANNEL_NAME::$id/events"
    )
    private var eventSink: EventChannel.EventSink? = null
    private var frameWidth = 0
    private var frameHeight = 0
    private var frameRotation = 0
    private var disposed = false

    init {
        backendRegistry.add(this)
        setEventChannelHandler()
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
    }

    private fun setEventChannelHandler() {
        eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(params: Any?, eventSink: EventChannel.EventSink) {
                this@TextureRendererBackend.eventSink = eventSink
            }

            override fun onCancel(params: Any?) {
                eventSink = null
            }
        })
    }

    override fun onFrame(frame: VideoFrame) {
        updateFrameGeometry(frame)
        super.onFrame(frame)
    }

    private fun updateFrameGeometry(frame: VideoFrame) {
        val currentWidth = frame.rotatedWidth
        val currentHeight = frame.rotatedHeight
        val currentRotation = frame.rotation

        if (currentWidth != frameWidth ||
                currentHeight != frameHeight ||
                currentRotation != frameRotation) {

            frameWidth = currentWidth
            frameHeight = currentHeight
            frameRotation = currentRotation
            texture.setDefaultBufferSize(frame.rotatedWidth, frame.rotatedHeight)
            uiThread {
                val geometry = mapOf(
                    "width" to currentWidth,
                    "height" to currentHeight,
                    "rotation" to currentRotation
                )
                eventSink?.success(geometry)
            }
        }
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
