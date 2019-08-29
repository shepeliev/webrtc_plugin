import 'dart:async';

import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/media_stream.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

import 'ice_candidate.dart';
import 'ice_server.dart';
import 'session_description.dart';
import 'session_description_constraints.dart';

class RTCPeerConnection {
  final String id;
  final MethodChannel _channel;
  final Stream<dynamic> _eventStream;

  Stream<IceCandidate> get iceCandidates => _eventStream
      .where((event) => event['type'] == 'iceCandidate')
      .map((event) => IceCandidate.fromMap(event['iceCandidate']));

  Stream<List<IceCandidate>> get removedIceCandidates => _eventStream
      .where((event) => event['type'] == 'removeIceCandidates')
      .map((event) => event['iceCandidates'] as List)
      .map((candidates) =>
          candidates.map((item) => IceCandidate.fromMap(item)).toList());

  Stream<IceConnectionState> get iceConnectionState => _eventStream
      .where((event) => event['type'] == 'iceConnectionStateChange')
      .map((event) => _mapToIceConnectionState(event['state']));

  Stream<MediaStream> get addMediaStream => _eventStream
      .where((event) => event['type'] == 'addMediaStream')
      .map((event) => MediaStream.fromMap(event['mediaStream']));

  Stream<MediaStream> get removeMediaStream => _eventStream
      .where((event) => event['type'] == 'removeMediaStream')
      .map((event) => MediaStream.fromMap(event['mediaStream']));

  RTCPeerConnection(this.id)
      : assert(id != null),
        _channel = MethodChannel('$channelName::$id'),
        _eventStream =
            EventChannel('$channelName::$id/events').receiveBroadcastStream();

  IceConnectionState _mapToIceConnectionState(dynamic arguments) {
    switch (arguments) {
      case 'NEW':
        return IceConnectionState.newConnection;
      case 'CHECKING':
        return IceConnectionState.checking;
      case 'CONNECTED':
        return IceConnectionState.connected;
      case 'FAILED':
        return IceConnectionState.failed;
      case 'DISCONNECTED':
        return IceConnectionState.disconnected;
      case 'CLOSED':
        return IceConnectionState.closed;
      default:
        throw PlatformException(
          code: 'ILLEGAL_ARGUMENT',
          message: 'Illegal ICE connection state $arguments',
        );
    }
  }

  static Future<RTCPeerConnection> create([IceServer iceServer]) async {
    final iceServerMap = iceServer != null ? iceServer.toMap() : null;
    final resultMap = await tryInvokeMapMethod(
        globalChannel, "createPeerConnection", iceServerMap);
    return RTCPeerConnection(resultMap['id']);
  }

  Future<void> addStream(MediaStream stream) async {
    await tryInvokeMethod(_channel, 'addMediaStream', stream.toMap());
  }

  Future<SessionDescription> createOffer(SdpConstraints constraints) async {
    final resultMap =
        await tryInvokeMapMethod(_channel, 'createOffer', constraints.toMap());
    return SessionDescription.fromMap(resultMap);
  }

  Future<SessionDescription> createAnswer(SdpConstraints constraints) async {
    final resultMap =
        await tryInvokeMapMethod(_channel, 'createAnswer', constraints.toMap());
    return SessionDescription.fromMap(resultMap);
  }

  Future<void> setLocalDescription(SessionDescription sdp) async {
    await tryInvokeMethod(_channel, 'setLocalDescription', sdp.toMap());
  }

  Future<void> setRemoteDescription(SessionDescription sdp) async {
    await tryInvokeMethod(_channel, 'setRemoteDescription', sdp.toMap());
  }

  Future<void> addIceCandidate(IceCandidate iceCandidate) async {
    await tryInvokeMethod(_channel, 'addIceCandidate', iceCandidate.toMap());
  }

  Future<void> removeIceCandidates(List<IceCandidate> iceCandidates) async {
    await tryInvokeMethod(
      _channel,
      'removeIceCandidates',
      iceCandidates.map((item) => item.toMap()).toList(),
    );
  }

  Future<void> dispose() async => await tryInvokeMethod(_channel, 'dispose');
}

enum IceConnectionState {
  newConnection,
  checking,
  connected,
  failed,
  disconnected,
  closed
}