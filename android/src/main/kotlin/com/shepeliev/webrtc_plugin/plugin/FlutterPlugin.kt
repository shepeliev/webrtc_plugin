package com.shepeliev.webrtc_plugin.plugin

import com.shepeliev.webrtc_plugin.METHOD_CHANNEL_NAME

typealias PluginId = String

internal interface FlutterPlugin {
    val id: PluginId

    val isGlobal: Boolean
        get() = false

    val channelName: String
        get() = "$METHOD_CHANNEL_NAME::$id"

    val methodHandlers: Map<String, MethodHandler<*>>
}
