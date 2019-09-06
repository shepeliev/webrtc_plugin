import 'dart:async';

import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/media_stream.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

import 'ice_candidate.dart';
import 'ice_server.dart';
import 'session_description.dart';
import 'session_description_constraints.dart';

class RtcPeerConnection {
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

  Stream<RemoteMediaStream> get remoteMediaStream =>
      _eventStream.where((event) {
        return ['addMediaStream', 'removeMediaStream'].contains(event['type']);
      }).map((event) {
        final stream = MediaStream.fromMap(event['mediaStream']);
        return event['type'] == 'addMediaStream'
            ? RemoteMediaStream(adding: stream)
            : RemoteMediaStream(removing: stream);
      });

  RtcPeerConnection(this.id)
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
      case 'COMPLETED':
        return IceConnectionState.completed;
      default:
        throw PlatformException(
          code: 'ILLEGAL_ARGUMENT',
          message: 'Illegal ICE connection state $arguments',
        );
    }
  }

  static Future<RtcPeerConnection> create([List<IceServer> iceServers]) async {
    final iceServersMap = iceServers?.map((it) => it.toMap())?.toList();
    final resultMap = await tryInvokeMapMethod(
        globalChannel, "createPeerConnection", iceServersMap);
    return RtcPeerConnection(resultMap['id']);
  }

  Future<bool> addStream(MediaStream stream) async =>
      await tryInvokeMethod(_channel, 'addMediaStream', stream.toMap());

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

  Future<void> setLocalDescription(SessionDescription sdp) async =>
      await tryInvokeMethod(_channel, 'setLocalDescription', sdp.toMap());

  Future<void> setRemoteDescription(SessionDescription sdp) async =>
      await tryInvokeMethod(_channel, 'setRemoteDescription', sdp.toMap());

  Future<bool> addIceCandidate(IceCandidate iceCandidate) async =>
      await tryInvokeMethod(_channel, 'addIceCandidate', iceCandidate.toMap());

  Future<bool> removeIceCandidates(List<IceCandidate> iceCandidates) async =>
      await tryInvokeMethod(
        _channel,
        'removeIceCandidates',
        iceCandidates.map((item) => item.toMap()).toList(),
      );

  Future<void> dispose() async => await tryInvokeMethod(_channel, 'dispose');
}

enum IceConnectionState {
  newConnection,
  checking,
  connected,
  failed,
  disconnected,
  closed,
  completed
}

class RemoteMediaStream {
  final MediaStream adding;
  final MediaStream removing;

  RemoteMediaStream({this.adding, this.removing});
}
