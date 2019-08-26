package com.shepeliev.webrtc_plugin.plugin

import java.util.*

@Deprecated("Use newStringId()", replaceWith = ReplaceWith("newStringId()"))
internal fun newId(): String = newStringId()

internal fun newStringId(): String = randomString()
internal fun randomString(): String = UUID.randomUUID().toString()
