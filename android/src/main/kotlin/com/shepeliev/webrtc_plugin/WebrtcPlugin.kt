package com.shepeliev.webrtc_plugin

import android.content.Context
import com.shepeliev.webrtc_plugin.plugin.DefaultFlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.DefaultMethodChannelRegistry
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterBackend
import com.shepeliev.webrtc_plugin.webrtc.DefaultVideoCapturerFactory
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory

const val METHOD_CHANNEL_NAME = "flutter.shepeliev.com/webrtc"

object WebrtcPlugin {
    private lateinit var methodChannelRegistry: DefaultMethodChannelRegistry

    @JvmStatic
    fun registerWith(registrar: Registrar) {
        val context = registrar.context()
        initializePeerConnectionFactory(context)
        val eglBase = EglBase.create()
        val peerConnectionFactory = createPeerConnectionFactory(eglBase)

        methodChannelRegistry = DefaultMethodChannelRegistry(registrar)
        val backendRegistry = DefaultFlutterBackendRegistry(methodChannelRegistry)

        val globalPlugins = listOf(
            TextureRendererBackendFactory(registrar, eglBase, backendRegistry),
            RtcPeerConnectionFactoryBackend(registrar, peerConnectionFactory, backendRegistry),
            MediaBackend(
                peerConnectionFactory,
                DefaultVideoCapturerFactory(registrar.context(), eglBase),
                backendRegistry
            )
        )
        methodChannelRegistry.addGlobalBackends(globalPlugins)

        registrar.addViewDestroyListener {
            disposeTextureRendererBackends(backendRegistry)
            disposeRtcPeerConnectionBackends(backendRegistry)
            disposeMdeiaStreamBackends(backendRegistry)
            disposeGlobalBackends(globalPlugins)
            peerConnectionFactory.dispose()
            eglBase.release()
            true
        }
    }

    private fun initializePeerConnectionFactory(context: Context) {
        val options = PeerConnectionFactory
            .InitializationOptions.builder(context)
            .setEnableInternalTracer(BuildConfig.DEBUG)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun createPeerConnectionFactory(eglBase: EglBase) =
        PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()

    private fun disposeTextureRendererBackends(backendRegistry: FlutterBackendRegistry) {
        backendRegistry.all
            .filterIsInstance(TextureRendererBackend::class.java)
            .forEach { it.dispose() }
    }

    private fun disposeRtcPeerConnectionBackends(backendRegistry: FlutterBackendRegistry) {
        backendRegistry.all
            .filterIsInstance(RtcPeerConnectionBackend::class.java)
            .forEach { it.dispose() }
    }

    private fun disposeMdeiaStreamBackends(backendRegistry: FlutterBackendRegistry) {
        backendRegistry.all
            .filterIsInstance(MediaStreamBackend::class.java)
            .forEach { it.dispose() }
    }

    private fun disposeGlobalBackends(backends: List<GlobalFlutterBackend>) {
        backends.forEach { it.dispose() }
    }
}
