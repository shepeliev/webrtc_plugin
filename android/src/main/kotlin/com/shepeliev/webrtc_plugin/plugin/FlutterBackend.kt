package com.shepeliev.webrtc_plugin.plugin

import com.shepeliev.webrtc_plugin.METHOD_CHANNEL_NAME

typealias BackendId = String

interface FlutterBackend {
    val tag: String
        get() = javaClass.simpleName

    val id: BackendId

    val isGlobal: Boolean
        get() = false

    val channelName: String
        get() = "$METHOD_CHANNEL_NAME::$id"

    val methodHandlers: Map<String, MethodHandler<*>>

    fun dispose() {
        throw UnsupportedOperationException()
    }
}
