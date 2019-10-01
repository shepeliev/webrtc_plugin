package com.shepeliev.webrtc_plugin.webrtc

import org.webrtc.CameraVideoCapturer
import org.webrtc.CapturerObserver

interface VideoCapturerFactory {
    fun createCameraVideoCapturer(capturerObserver: CapturerObserver): CameraVideoCapturer?
}

