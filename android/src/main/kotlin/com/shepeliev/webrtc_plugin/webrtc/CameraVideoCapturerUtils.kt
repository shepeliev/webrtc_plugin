package com.shepeliev.webrtc_plugin.webrtc

import android.content.Context
import android.util.Log
import org.webrtc.*

fun Context.cameraEnumerator(): CameraEnumerator = when {
    Camera2Enumerator.isSupported(this) -> Camera2Enumerator(this)
    else -> Camera1Enumerator()
}

fun CameraEnumerator.createFrontCameraCapturer(
    eventsHandler: CameraVideoCapturer.CameraEventsHandler? = null
): CameraVideoCapturer? {
    return firstFrontCameraName()?.let { createCapturer(it, eventsHandler) }
}

fun CameraEnumerator.createBackCameraCapturer(
    eventsHandler: CameraVideoCapturer.CameraEventsHandler? = null
): CameraVideoCapturer? {
    return firstBackCameraName()?.let { createCapturer(it, eventsHandler) }
}

private fun CameraEnumerator.firstFrontCameraName(): String? =
    deviceNames.firstOrNull { isFrontFacing(it) }

private fun CameraEnumerator.firstBackCameraName(): String? =
    deviceNames.firstOrNull { isBackFacing(it) }
