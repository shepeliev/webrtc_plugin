package com.shepeliev.webrtc_plugin.plugin

import java.util.*

@Deprecated("Use newStringId()", replaceWith = ReplaceWith("newStringId()"))
fun newId(): String = newStringId()

fun newStringId(): String = randomString()
fun randomString(): String = UUID.randomUUID().toString()
