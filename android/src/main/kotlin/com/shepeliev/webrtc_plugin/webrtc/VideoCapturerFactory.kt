package com.shepeliev.webrtc_plugin.webrtc

import org.webrtc.CameraVideoCapturer
import org.webrtc.CapturerObserver

interface VideoCapturerFactory {
    fun createCameraVideoCapturer(
        videoConstraints: Map<String, Any>,
        capturerObserver: CapturerObserver
    ): CameraVideoCapturer?
}

