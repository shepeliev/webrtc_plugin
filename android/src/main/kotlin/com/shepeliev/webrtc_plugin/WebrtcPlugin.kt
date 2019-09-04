package com.shepeliev.webrtc_plugin

import com.shepeliev.webrtc_plugin.plugin.DefaultFlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.DefaultMethodChannelRegistry
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterBackend
import com.shepeliev.webrtc_plugin.webrtc.DefaultVideoCapturerFactory
import com.shepeliev.webrtc_plugin.webrtc.PCF
import io.flutter.plugin.common.PluginRegistry.Registrar

const val METHOD_CHANNEL_NAME = "flutter.shepeliev.com/webrtc"

object WebrtcPlugin {
    private lateinit var methodChannelRegistry: DefaultMethodChannelRegistry

    @JvmStatic
    fun registerWith(registrar: Registrar) {
        PCF.initialize(registrar.context())

        methodChannelRegistry = DefaultMethodChannelRegistry(registrar)
        val backendRegistry = DefaultFlutterBackendRegistry(methodChannelRegistry)

        val globalPlugins = listOf(
            TextureRendererBackendFactory(registrar, backendRegistry),
            RtcPeerConnectionFactoryBackend(registrar, PCF.instance, backendRegistry),
            MediaBackend(
                PCF.instance,
                DefaultVideoCapturerFactory(registrar.context()),
                backendRegistry
            )
        )
        methodChannelRegistry.addGlobalBackends(globalPlugins)

        registrar.addViewDestroyListener {
            disposeTextureRendererBackends(backendRegistry)
            disposeRtcPeerConnectionBackends(backendRegistry)
            disposeMdeiaStreamBackends(backendRegistry)
            disposeGlobalBackends(globalPlugins)
            true
        }
    }

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
