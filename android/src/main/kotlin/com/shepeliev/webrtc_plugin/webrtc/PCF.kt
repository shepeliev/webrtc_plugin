package com.shepeliev.webrtc_plugin.webrtc

import android.content.Context
import com.shepeliev.webrtc_plugin.BuildConfig
import com.shepeliev.webrtc_plugin.plugin.newId
import org.webrtc.*

internal object PCF {
    private var _peerConnectionFactory: PeerConnectionFactory? = null
    val instance: PeerConnectionFactory
        get() = _peerConnectionFactory ?: error("PeerConnectionFactory is not initialized yet.")

    fun initialize(context: Context) {
        if (alreadyInitialized()) return
        initializePeerConnectionFactory(context)
        _peerConnectionFactory =
            createPeerConnectionFactory(context)
    }

    private fun alreadyInitialized(): Boolean = _peerConnectionFactory != null

    private fun initializePeerConnectionFactory(context: Context) {
        val options = PeerConnectionFactory
            .InitializationOptions.builder(context)
            .setEnableInternalTracer(BuildConfig.DEBUG)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun createPeerConnectionFactory(context: Context): PeerConnectionFactory =
        PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()

    fun dispose() {
        _peerConnectionFactory?.dispose()
        _peerConnectionFactory = null
    }
}
