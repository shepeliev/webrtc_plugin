package com.shepeliev.webrtc_plugin

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.shepeliev.webrtc_plugin.plugin.FlutterBackendRegistry
import com.shepeliev.webrtc_plugin.plugin.GlobalFlutterBackend
import com.shepeliev.webrtc_plugin.plugin.MethodHandler
import com.shepeliev.webrtc_plugin.plugin.newStringId
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.webrtc.*
import java.util.concurrent.CountDownLatch

private val TAG = RtcPeerConnectionFactoryBackend::class.java.simpleName

class RtcPeerConnectionFactoryBackend(
    private val registrar: Registrar,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val backendRegistry: FlutterBackendRegistry
) :
    GlobalFlutterBackend {

    override val methodHandlers: Map<String, MethodHandler<*>> = mapOf(
        "createPeerConnection" to ::createPeerConnection
    )

    private fun createPeerConnection(methodCall: MethodCall): Map<String, Any> {
        val id = newStringId()
        val eventChannelName = "$METHOD_CHANNEL_NAME::$id/events"
        val eventChannel = EventChannel(registrar.messenger(), eventChannelName)
        val peerConnection = peerConnectionFactory.createPeerConnection(
            getIceServers(methodCall),
            PeerConnectionObserver(id, eventChannel)
        ) ?: error("Could not create PeerConnection.")
        return RtcPeerConnectionBackend(id, peerConnection, eventChannel, backendRegistry).toMap()
    }

    private fun getIceServers(methodCall: MethodCall): List<PeerConnection.IceServer> {
        if (methodCall.arguments == null) return emptyList()
        require(methodCall.arguments is List<*>) { "The argument is not a List" }
        @Suppress("UNCHECKED_CAST")
        val iceServers = methodCall.arguments as List<Map<String, *>>
        return iceServers.map { it.toIceServer() }
    }

    private fun Map<String, *>.toIceServer(): PeerConnection.IceServer {
        @Suppress("UNCHECKED_CAST")
        val urls = getValue("urls") as List<String>
        val username = getValue("username") as String
        val password = getValue("password") as String
        val tlsCertPolicy = when (getValue("tlsCertPolicy")) {
            "secure" -> PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_SECURE
            "insecure_no_check" -> PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK
            else -> throw IllegalArgumentException("Illegal TlsCertPolicy")
        }
        val hostname = getValue("hostname") as String
        @Suppress("UNCHECKED_CAST")
        val tlsAlpnProtocols = get("tlsAlpnProtocols") as? List<String>
        @Suppress("UNCHECKED_CAST")
        val tlsEllipticCurves = get("tlsAlpnProtocols") as? List<String>
        return PeerConnection.IceServer.builder(urls)
            .setUsername(username)
            .setPassword(password)
            .setTlsCertPolicy(tlsCertPolicy)
            .setHostname(hostname)
            .setTlsAlpnProtocols(tlsAlpnProtocols)
            .setTlsEllipticCurves(tlsEllipticCurves)
            .createIceServer()
    }

    private inner class PeerConnectionObserver(id: String, eventChannel: EventChannel) :
        PeerConnection.Observer {

        private val uiThread = Handler(Looper.getMainLooper())
        private val tag = "${PeerConnectionObserver::class.java.simpleName}::$id"
        private var eventSink: EventChannel.EventSink? = null

        private val streamHandler = object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink) {
                this@PeerConnectionObserver.eventSink = eventSink
            }

            override fun onCancel(arguments: Any?) {
                eventSink = null
            }
        }

        init {
            val countDownLatch = CountDownLatch(1)
            uiThread.post {
                eventChannel.setStreamHandler(streamHandler)
                countDownLatch.countDown()
            }
            countDownLatch.await()
        }

        override fun onIceCandidate(iceCandidate: IceCandidate) {
            Log.d(tag, "onIceCandidate($iceCandidate)")
            uiThread.post {
                eventSink?.success(
                    mapOf(
                        "type" to "iceCandidate",
                        "iceCandidate" to iceCandidate.toMap()
                    )
                )
            }
        }

        override fun onDataChannel(dataChannel: DataChannel) {
            Log.d(TAG, "onDataChannel($dataChannel)")
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {
            Log.d(tag, "onIceConnectionReceivingChange($receiving)")
        }

        override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
            Log.d(tag, "onIceConnectionChange($newState)")
            uiThread.post {
                eventSink?.success(
                    mapOf(
                        "type" to "iceConnectionStateChange",
                        "state" to newState.toString()
                    )
                )
            }
        }

        override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {
            Log.d(tag, "onIceGatheringChange($newState)")
        }

        override fun onAddStream(stream: MediaStream) {
            Log.d(tag, "onAddStream($stream)")
            val mediaStreamBackend = MediaStreamBackend(stream, null, backendRegistry)
            uiThread.post {
                eventSink?.success(
                    mapOf(
                        "type" to "addMediaStream",
                        "mediaStream" to mediaStreamBackend.toMap()
                    )
                )
            }
        }

        override fun onSignalingChange(newState: PeerConnection.SignalingState) {
            Log.d(tag, "onSignalingChange($newState)")
        }

        override fun onIceCandidatesRemoved(iceCandidates: Array<out IceCandidate>) {
            Log.d(tag, "onIceCandidatesRemoved(${iceCandidates.contentToString()}")
            uiThread.post {
                eventSink?.success(mapOf(
                    "type" to "removeIceCandidates",
                    "iceCandidates" to iceCandidates.map { it.toMap() }
                ))
            }
        }

        override fun onRemoveStream(stream: MediaStream) {
            Log.d(tag, "onRemoveStream($stream)")
            val mediaStreamBackend = backendRegistry.all
                .filterIsInstance(MediaStreamBackend::class.java)
                .first { it.mediaStream.id == stream.id }
            uiThread.post {
                eventSink?.success(
                    mapOf(
                        "type" to "addMediaStream",
                        "mediaStream" to mediaStreamBackend.toMap()
                    )
                )
            }
        }

        override fun onRenegotiationNeeded() {
            Log.d(tag, "onRenegotiationNeeded")
        }

        override fun onAddTrack(rtpReceiver: RtpReceiver, streams: Array<out MediaStream>) {
            Log.d(tag, "onAddTrack($rtpReceiver, ${streams.contentToString()}")
        }

        private fun IceCandidate.toMap() = mapOf(
            "sdpMid" to sdpMid,
            "sdpMLineIndex" to sdpMLineIndex,
            "sdp" to sdp,
            "serverUrl" to serverUrl
        )
    }
}
