package com.shepeliev.webrtc_plugin.webrtc

import android.content.Context
import org.webrtc.VideoSource

internal interface CameraCapturer {
    fun startCapture(
        context: Context,
        videoSource: VideoSource,
        width: Int,
        height: Int,
        fps: Int,
        side: CameraSide
    )

    fun stopCapture()

    enum class CameraSide { BACK, FRONT }
}
