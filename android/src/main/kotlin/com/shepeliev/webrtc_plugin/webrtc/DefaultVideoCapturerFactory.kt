package com.shepeliev.webrtc_plugin.webrtc

import android.content.Context
import android.util.Log
import com.shepeliev.webrtc_plugin.plugin.newStringId
import org.webrtc.CameraVideoCapturer
import org.webrtc.CapturerObserver
import org.webrtc.SurfaceTextureHelper

private val TAG = DefaultVideoCapturerFactory::class.java.simpleName

internal class DefaultVideoCapturerFactory(private val context: Context) :
    VideoCapturerFactory {
    private val surfaceTextureHelper =
        SurfaceTextureHelper.create(
            "CapturerThread::${newStringId()}",
            eglBase.eglBaseContext
        )

    override fun createCameraVideoCapturer(
        videoConstraints: Map<String, Any>,
        capturerObserver: CapturerObserver
    ): CameraVideoCapturer? {
        // TODO implement initializing camera capturing according to videoConstraints
        val cameraEnumerator = context.cameraEnumerator()
        return cameraEnumerator.createFrontCameraCapturer(CameraEventsHandler())?.apply {
            initialize(surfaceTextureHelper, context, capturerObserver)
            startCapture(1280, 720, 30)
        } ?: run {
            Log.e(
                TAG,
                "Could not create CameraVideoCapturer."
            )
            return null
        }
    }

    protected fun finalize() {
        Log.w(
            TAG,
            "Finalizing DefaultVideoCapturerFactory"
        )
        surfaceTextureHelper.dispose()
    }

    private class CameraEventsHandler : CameraVideoCapturer.CameraEventsHandler {
        override fun onCameraError(errorDescription: String?) {
            Log.e(
                TAG,
                "Camera error: $errorDescription"
            )
        }

        override fun onCameraOpening(cameraId: String) {
            Log.d(TAG, "Opening camera: $cameraId")
        }

        override fun onCameraDisconnected() {
            Log.d(TAG, "Camera disconnected")
        }

        override fun onCameraFreezed(errorDescription: String?) {
            Log.e(
                TAG,
                "Camera freezed: $errorDescription"
            )
        }

        override fun onFirstFrameAvailable() {
            Log.d(TAG, "First frame is available.")
        }

        override fun onCameraClosed() {
            Log.d(TAG, "Camera closed.")
        }
    }
}
