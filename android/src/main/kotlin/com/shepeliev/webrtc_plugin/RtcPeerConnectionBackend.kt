package com.shepeliev.webrtc_plugin

import android.util.Log
import com.shepeliev.webrtc_plugin.plugin.*
import com.shepeliev.webrtc_plugin.webrtc.mediaConstraints
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import org.webrtc.*
import java.util.concurrent.CountDownLatch

private val TAG = RtcPeerConnectionBackend::class.java.simpleName

class RtcPeerConnectionBackend(
    override val id: BackendId,
    private val peerConnection: PeerConnection,
    private val eventChannel: EventChannel,
    private val backendRegistry: FlutterBackendRegistry
) : FlutterBackend {

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "addMediaStream" to ::addMediaStream,
        "createOffer" to ::createOffer,
        "createAnswer" to ::createAnswer,
        "setLocalDescription" to ::setLocalDescription,
        "setRemoteDescription" to ::setRemoteDescription,
        "addIceCandidate" to ::addIceCandidate,
        "removeIceCandidates" to ::removeIceCandidates,
        "dispose" to ::dispose
    )

    private var disposed = false

    init {
        backendRegistry.add(this)
    }

    private fun addMediaStream(methodCall: MethodCall): Boolean {
        check(!disposed) { "PeerConnection is disposed!" }
        val mediaStreamId = methodCall.argument<String>("id") ?: error("'id' is required.")
        val mediaStreamBackend = backendRegistry.get<MediaStreamBackend>(mediaStreamId)
        val mediaStream = mediaStreamBackend.mediaStream
        Log.d(TAG, "Adding media stream $mediaStream.")
        return peerConnection.addStream(mediaStream)
    }

    private fun createOffer(methodCall: MethodCall): Map<String, Any> {
        check(!disposed) { "PeerConnection is disposed!" }
        val constraints = getMediaConstraints(methodCall)
        Log.d(TAG, "Creating an offer. Constraints: $constraints.")
        val createSdbObserver = CreateSdbObserver()
        peerConnection.createOffer(createSdbObserver, constraints)
        return createSdbObserver.sessionDescription.toMap()
    }

    private fun createAnswer(methodCall: MethodCall): Map<String, Any> {
        check(!disposed) { "PeerConnection is disposed!" }
        val constraints = getMediaConstraints(methodCall)
        Log.d(TAG, "Creating an answer. Constrains: $constraints")
        val createSdbObserver = CreateSdbObserver()
        peerConnection.createAnswer(createSdbObserver, constraints)
        return createSdbObserver.sessionDescription.toMap()
    }

    private fun getMediaConstraints(methodCall: MethodCall): MediaConstraints {
        val offerToReceiveAudio = methodCall.argument<Boolean>("offerToReceiveAudio")
            ?: error("'offerToReceiveAudio' is required.")
        val offerToReceiveVideo = methodCall.argument<Boolean>("offerToReceiveVideo")
            ?: error("'offerToReceiveVideo' is required.")
        val iceRestart = methodCall.argument<Boolean>("iceRestart")
            ?: error("'iceRestart' is required.")
        return mediaConstraints {
            mandatory {
                OfferToReceiveAudio = offerToReceiveAudio
                OfferToReceiveVideo = offerToReceiveVideo
                IceRestart = iceRestart
            }
        }
    }

    private fun SessionDescription.toMap() = mapOf(
        "type" to type.toString(),
        "description" to description
    )

    private fun setLocalDescription(methodCall: MethodCall): Nothing? {
        check(!disposed) { "PeerConnection is disposed!" }
        val sdp = getSessionDescription(methodCall)
        Log.d(TAG, "Setting local description: $sdp.")
        val setSdpObserver = SetSdbObserver()
        peerConnection.setLocalDescription(setSdpObserver, sdp)
        setSdpObserver.await()
        return null
    }

    private fun setRemoteDescription(methodCall: MethodCall): Nothing? {
        check(!disposed) { "PeerConnection is disposed!" }
        val sdp = getSessionDescription(methodCall)
        Log.d(TAG, "Setting remote description: $sdp.")
        val setSdpObserver = SetSdbObserver()
        peerConnection.setRemoteDescription(setSdpObserver, sdp)
        setSdpObserver.await()
        return null
    }

    private fun getSessionDescription(methodCall: MethodCall): SessionDescription {
        val typeStr = methodCall.argument<String>("type")
            ?: error("'type' is required.")
        val description = methodCall.argument<String>("description")
            ?: error("'description' is required.")
        val type = when (typeStr) {
            "offer" -> SessionDescription.Type.OFFER
            "pranswer" -> SessionDescription.Type.PRANSWER
            "answer" -> SessionDescription.Type.ANSWER
            else -> throw IllegalArgumentException("Illegal 'type': $typeStr")
        }
        return SessionDescription(type, description)
    }

    private fun addIceCandidate(methodCall: MethodCall): Boolean {
        val iceCandidate = getIceCandidate(methodCall)
        Log.d(TAG, "Adding ICE candidate: $iceCandidate.")
        return peerConnection.addIceCandidate(iceCandidate)
    }

    private fun getIceCandidate(methodCall: MethodCall): IceCandidate {
        val sdpMid = methodCall.argument<String>("sdpMid")
            ?: error("'sdpMid' is required")
        val sdpMLineIndex = methodCall.argument<Int>("sdpMLineIndex")
            ?: error("'sdpMLineIndex' is required")
        val sdp = methodCall.argument<String>("sdp")
            ?: error("'sdp' is required")
//        val serverUrl = methodCall.argument<String>("serverUrl")
//            ?: error("'serverUrl' is required")
        return IceCandidate(sdpMid, sdpMLineIndex, sdp)

    }

    private fun removeIceCandidates(methodCall: MethodCall): Boolean {
        check(!disposed) { "PeerConnection is disposed!" }
        val iceCandidates = (methodCall.arguments as List<*>)
            .map { getIceCandidate(MethodCall(null, it)) }
        Log.d(TAG, "Removing ICE candidates: $iceCandidates")
        return peerConnection.removeIceCandidates(iceCandidates.toTypedArray())
    }

    @Suppress("UNUSED_PARAMETER")
    private fun dispose(methodCall: MethodCall): Nothing? {
        disposeInternal()
        return null
    }

    private fun disposeInternal() {
        if (disposed) return
        Log.d(TAG, "Disposing.")
        peerConnection.dispose()
        backendRegistry.remove(this)
        uiThread { eventChannel.setStreamHandler(null) }
        disposed = true
    }

    fun toMap(): Map<String, Any> = mapOf("id" to id)

    protected fun finalize() {
        if (disposed) return
        Log.w(TAG, "$this has not been disposed properly!")
        disposeInternal()
    }
}

private class SetSdbObserver : DefaultSdpObserver {
    private val countDownLatch = CountDownLatch(1)
    private var exception: WebRtcPluginException? = null


    fun await() {
        countDownLatch.await()
    }

    override fun onSetFailure(error: String?) {
        exception = WebRtcPluginException(error)
        countDownLatch.countDown()
    }

    override fun onSetSuccess() {
        countDownLatch.countDown()
    }
}

private class CreateSdbObserver : DefaultSdpObserver {
    private val countDownLatch = CountDownLatch(1)
    private var exception: WebRtcPluginException? = null
    private var _sessionDescription: SessionDescription? = null

    val sessionDescription: SessionDescription
        get() {
            countDownLatch.await()
            exception?.let { throw it }
            return _sessionDescription!!
        }

    override fun onCreateSuccess(sdp: SessionDescription) {
        _sessionDescription = sdp
        countDownLatch.countDown()
    }

    override fun onCreateFailure(error: String?) {
        exception = WebRtcPluginException(error)
        countDownLatch.countDown()
    }
}

private interface DefaultSdpObserver : SdpObserver {
    override fun onSetFailure(error: String?) {
        throw UnsupportedOperationException()
    }

    override fun onSetSuccess() {
        throw UnsupportedOperationException()
    }

    override fun onCreateSuccess(sdp: SessionDescription) {
        throw UnsupportedOperationException()
    }

    override fun onCreateFailure(error: String?) {
        throw UnsupportedOperationException()
    }
}
