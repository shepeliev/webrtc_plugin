package com.shepeliev.webrtc_plugin.webrtc

import org.webrtc.MediaConstraints

fun mediaConstraints(build: MediaConstraintsBuilder.() -> Unit): MediaConstraints {
    val builder = MediaConstraintsBuilder()
    build(builder)
    return builder.mediaConstraints
}

class MediaConstraintsBuilder {

    val mediaConstraints: MediaConstraints = MediaConstraints()

    fun mandatory(build: Constraints.() -> Unit) {
        val constraints = Constraints()
        build(constraints)
        mediaConstraints.mandatory.addAll(constraints.keyValuePairs())
    }

    fun optional(build: Constraints.() -> Unit) {
        val constraints = Constraints()
        build(constraints)
        mediaConstraints.optional.addAll(constraints.keyValuePairs())
    }
}

@Suppress("PropertyName")
class Constraints {

    private val mediaConstraintsMap = mutableMapOf<String, Any>()

    var minAspectRatio: Int by mediaConstraintsMap
    var maxAspectRat: Int   by mediaConstraintsMap
    var maxWidth: Int   by mediaConstraintsMap
    var minWidth: Int   by mediaConstraintsMap
    var maxHeight: Int   by mediaConstraintsMap
    var minHeight: Int   by mediaConstraintsMap
    var maxFrameRate: Int   by mediaConstraintsMap
    var minFrameRate: Int   by mediaConstraintsMap

    // Audio constraints
    var echoCancellation: Boolean by mediaConstraintsMap

    // Constraint keys for CreateOffer / CreateAnswer defined in W3C specification.
    var OfferToReceiveAudio: Boolean by mediaConstraintsMap
    var OfferToReceiveVideo: Boolean by mediaConstraintsMap
    var VoiceActivityDetection: Boolean by mediaConstraintsMap
    var IceRestart: Boolean by mediaConstraintsMap

    // Below constraints should be used during PeerConnection construction.
    var DtlsSrtpKeyAgreement: Boolean by mediaConstraintsMap
    var RtpDataChannels: Boolean by mediaConstraintsMap

    fun addConstraint(constraint: String, value: Int) {
        mediaConstraintsMap[constraint] = value
    }

    fun addConstraint(constraint: String, value: Boolean) {
        mediaConstraintsMap[constraint] = value
    }

    fun keyValuePairs(): Collection<MediaConstraints.KeyValuePair> =
        mediaConstraintsMap.entries.map { MediaConstraints.KeyValuePair(it.key, it.value.toString()) }
}
