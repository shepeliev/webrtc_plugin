package com.shepeliev.webrtc_plugin

import com.shepeliev.webrtc_plugin.plugin.DefaultFlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.DefaultMethodChannelRegistry
import com.shepeliev.webrtc_plugin.webrtc.DefaultCameraCapturer
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
            FlutterVideoSourceFactory(
                registrar.context(),
                PCF.instance,
                DefaultCameraCapturer,
                backendRegistry
            ),
            FlutterVideoTrackFactory(PCF.instance, backendRegistry),
            FlutterTextureRendererFactory(registrar, backendRegistry)
        )
        methodChannelRegistry.addGlobalBackends(globalPlugins)
    }
}
