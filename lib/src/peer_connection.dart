import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/audio_source.dart';
import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/src/video_source.dart';

class PeerConnection {
  final String id;
  final MethodChannel _channel;

  PeerConnection(this.id) : _channel = MethodChannel('$channelName::$id');

  static Future<PeerConnection> create(PeerConnectionObserver observer,
      [IceServer iceServer]) async {
    return null;
  }

  Future<SessionDescription> createOffer(SdpConstraints constraints,
      [AudioSource audioSource, VideoSource videoSource]) async {
    return null;
  }

  Future<SessionDescription> createAnswer(
      SessionDescription offer, SdpConstraints constraints) async {
    return null;
  }

  Future<void> addIceCandidate(IceCandidate iceCandidate) async {
    return null;
  }

  Future<void> removeIceCandidates(List<IceCandidate> iceCandidates) async {
    return null;
  }
}

abstract class PeerConnectionObserver {
  Future<void> onIceConnectionChange(IceConnectionState state);

  Future<void> onIceCandidate(IceCandidate iceCandidate);

  Future<void> onIceCandidatesRemoved(List<IceCandidate> iceCandidates);
}

class IceServer {
  final List<String> urls;
  final String username;
  final String password;
  final TlsCertPolicy tlsCertPolicy;
  final String hostname;
  final List<String> tlsAlpnProtocols;
  final List<String> tlsEllipticCurves;

  IceServer(
      {this.urls,
      this.username = "",
      this.password = "",
      this.tlsCertPolicy = TlsCertPolicy.secure,
      this.hostname = "",
      this.tlsAlpnProtocols,
      this.tlsEllipticCurves})
      : assert(urls.isNotEmpty);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is IceServer &&
          runtimeType == other.runtimeType &&
          urls == other.urls &&
          username == other.username &&
          password == other.password &&
          tlsCertPolicy == other.tlsCertPolicy &&
          hostname == other.hostname &&
          tlsAlpnProtocols == other.tlsAlpnProtocols &&
          tlsEllipticCurves == other.tlsEllipticCurves;

  @override
  int get hashCode =>
      urls.hashCode ^
      username.hashCode ^
      password.hashCode ^
      tlsCertPolicy.hashCode ^
      hostname.hashCode ^
      tlsAlpnProtocols.hashCode ^
      tlsEllipticCurves.hashCode;

  @override
  String toString() {
    return 'IceServer{urls: $urls, username: $username, password: $password, tlsCertPolicy: $tlsCertPolicy, hostname: $hostname, tlsAlpnProtocols: $tlsAlpnProtocols, tlsEllipticCurves: $tlsEllipticCurves}';
  }
}

enum TlsCertPolicy { secure, insecure_no_check }

class SdpConstraints {
  final bool offerToReceiveAudio;
  final bool offerToReceiveVideo;
  final bool iceRestart;

  SdpConstraints(
      {this.offerToReceiveAudio,
      this.offerToReceiveVideo,
      this.iceRestart = false});

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is SdpConstraints &&
          runtimeType == other.runtimeType &&
          offerToReceiveAudio == other.offerToReceiveAudio &&
          offerToReceiveVideo == other.offerToReceiveVideo &&
          iceRestart == other.iceRestart;

  @override
  int get hashCode =>
      offerToReceiveAudio.hashCode ^
      offerToReceiveVideo.hashCode ^
      iceRestart.hashCode;

  @override
  String toString() {
    return 'SdpConstraints{offerToReceiveAudio: $offerToReceiveAudio, offerToReceiveVideo: $offerToReceiveVideo, iceRestart: $iceRestart}';
  }
}

class IceCandidate {
  final String sdpMid;
  final int sdpMLineIndex;
  final String sdp;
  final String serverUrl;

  IceCandidate(this.sdpMid, this.sdpMLineIndex, this.sdp,
      [this.serverUrl = ""]);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is IceCandidate &&
          runtimeType == other.runtimeType &&
          sdpMid == other.sdpMid &&
          sdpMLineIndex == other.sdpMLineIndex &&
          sdp == other.sdp &&
          serverUrl == other.serverUrl;

  @override
  int get hashCode =>
      sdpMid.hashCode ^
      sdpMLineIndex.hashCode ^
      sdp.hashCode ^
      serverUrl.hashCode;

  @override
  String toString() {
    return 'IceCandidate{sdpMid: $sdpMid, sdpMLineIndex: $sdpMLineIndex, sdp: $sdp, serverUrl: $serverUrl}';
  }
}

class SessionDescription {
  final SessionDescriptionType type;
  final String description;

  SessionDescription(this.type, this.description);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is SessionDescription &&
          runtimeType == other.runtimeType &&
          type == other.type &&
          description == other.description;

  @override
  int get hashCode => type.hashCode ^ description.hashCode;

  @override
  String toString() {
    return 'SessionDescription{type: $type, description: $description}';
  }
}

enum SessionDescriptionType { offer, pranswer, answer }

enum IceConnectionState {
  newConnection,
  checking,
  connected,
  failed,
  disconnected,
  closed
}
