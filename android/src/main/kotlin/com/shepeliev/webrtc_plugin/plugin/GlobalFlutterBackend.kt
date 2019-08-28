package com.shepeliev.webrtc_plugin.plugin

import com.shepeliev.webrtc_plugin.METHOD_CHANNEL_NAME

interface GlobalFlutterBackend : FlutterBackend {
    override val id: BackendId
        get() = javaClass.simpleName

    override val isGlobal: Boolean
        get() = true

    override val channelName: String
        get() = METHOD_CHANNEL_NAME
}
