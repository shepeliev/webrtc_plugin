package com.shepeliev.webrtc_plugin.webrtc

import android.content.Context
import android.util.Log
import org.webrtc.*

private val TAG = DefaultCameraCapturer::class.java.simpleName

internal object DefaultCameraCapturer : CameraCapturer {
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var cameraCapturer: CameraVideoCapturer? = null
    private var currentVideoSource: VideoSource? = null

    override fun startCapture(
        context: Context,
        videoSource: VideoSource,
        width: Int,
        height: Int,
        fps: Int,
        side: CameraCapturer.CameraSide
    ) {
        if (currentVideoSource == videoSource) {
            Log.w(TAG, "Camera capturing already started.")
            return
        }
        stopCapture()
        currentVideoSource = videoSource
        cameraCapturer = cameraVideoCapturer(context.cameraEnumerator(), side)
        startCaptureInternal(context, width, height, fps)
    }

    private fun cameraVideoCapturer(
        cameraEnumerator: CameraEnumerator,
        side: CameraCapturer.CameraSide
    ): CameraVideoCapturer =
        when (side) {
            CameraCapturer.CameraSide.BACK -> {
                cameraEnumerator.createBackCameraCapturer(CameraEventsHandler())
            }
            CameraCapturer.CameraSide.FRONT -> {
                cameraEnumerator.createFrontCameraCapturer(CameraEventsHandler())
            }
        } ?: error("Creating camera capturer failed.")

    private fun startCaptureInternal(context: Context, width: Int, height: Int, fps: Int) {
        surfaceTextureHelper = SurfaceTextureHelper.create("CapturerHelper", eglBase.eglBaseContext)
        cameraCapturer!!.initialize(
            surfaceTextureHelper,
            context,
            currentVideoSource!!.capturerObserver
        )
        cameraCapturer!!.startCapture(width, height, fps)
    }

    override fun stopCapture() {
        if (cameraCapturer == null) return
        Log.d(TAG, "Stop capturing camera.")
        cameraCapturer?.stopCapture()
        cameraCapturer = null
        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null
        currentVideoSource = null
    }

    private class CameraEventsHandler : CameraVideoCapturer.CameraEventsHandler {
        override fun onCameraError(errorDescription: String?) {
            Log.e(TAG, "Camera error: $errorDescription")
            stopCapture()
        }

        override fun onCameraOpening(cameraId: String) {
            Log.d(TAG, "Opening camera: $cameraId")
        }

        override fun onCameraDisconnected() {
            Log.d(TAG, "Camera disconnected")
            stopCapture()
        }

        override fun onCameraFreezed(errorDescription: String?) {
            Log.e(TAG, "Camera freezed: $errorDescription")
            stopCapture()
        }

        override fun onFirstFrameAvailable() {
            Log.d(TAG, "First frame is available.")
        }

        override fun onCameraClosed() {
            Log.d(TAG, "Camera closed.")
            stopCapture()
        }
    }
}

private fun Context.cameraEnumerator(): CameraEnumerator = when {
    Camera2Enumerator.isSupported(this) -> Camera2Enumerator(this)
    else -> Camera1Enumerator()
}

private fun CameraEnumerator.createFrontCameraCapturer(
    eventsHandler: CameraVideoCapturer.CameraEventsHandler? = null
): CameraVideoCapturer? {
    return firstFrontCameraName()?.let { createCapturer(it, eventsHandler) }
}

private fun CameraEnumerator.createBackCameraCapturer(
    eventsHandler: CameraVideoCapturer.CameraEventsHandler? = null
): CameraVideoCapturer? {
    return firstBackCameraName()?.let { createCapturer(it, eventsHandler) }
}

private fun CameraEnumerator.firstFrontCameraName(): String? =
    deviceNames.firstOrNull { isFrontFacing(it) }

private fun CameraEnumerator.firstBackCameraName(): String? =
    deviceNames.firstOrNull { isBackFacing(it) }
