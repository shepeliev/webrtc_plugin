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

  RtcPeerConnection(this.id)
      : assert(id != null),
        _channel = MethodChannel('$channelName::$id'),
        _eventStream =
            EventChannel('$channelName::$id/events').receiveBroadcastStream();

  static Future<RtcPeerConnection> create([List<IceServer> iceServers]) async {
    final iceServersMap = iceServers?.map((it) => it.toMap())?.toList();
    final resultMap = await tryInvokeMapMethod(
        globalChannel, "createPeerConnection", iceServersMap);
    return RtcPeerConnection(resultMap['id']);
  }

  Future<bool> addMediaStream(MediaStream stream) async =>
      await tryInvokeMethod(_channel, 'addMediaStream', stream.toMap());

  Future<void> removeMediaStream(MediaStream stream) async =>
      await tryInvokeMethod(_channel, 'removeMediaStream', stream.toMap());

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

  Stream<dynamic> on(RtcPeerConnectionEvent event) => _eventStream
      .where((e) => e['type'] == event.toString().split('.').last)
      .map((e) => e['data']);

  Future<void> dispose() async => await tryInvokeMethod(_channel, 'dispose');
}

enum RtcPeerConnectionEvent {
  signalingChange,
  iceConnectionChange,
  iceGatheringChange,
  iceCandidate,
  iceCandidatesRemoved,
  addStream,
  removeStream,
  dataChannel
}

enum IceConnectionState {
  starting,
  checking,
  connected,
  failed,
  disconnected,
  closed,
  completed
}

IceConnectionState iceConnectionStateFromString(String state) {
  assert(state != null);
  switch (state.toUpperCase()) {
    case 'NEW':
      return IceConnectionState.starting;
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
      throw ArgumentError('Illegal ICE connection state $state');
  }
}

enum IceGatheringState { starting, gathering, complete }

IceGatheringState iceGatheringStateFromString(String state) {
  assert(state != null);
  switch (state.toUpperCase()) {
    case 'NEW':
      return IceGatheringState.starting;
    case 'GATHERING':
      return IceGatheringState.gathering;
    case 'COMPLETE':
      return IceGatheringState.complete;
    default:
      throw ArgumentError('Illegal ICE gathering state $state');
  }
}

enum SignalingState {
  stable,
  haveLocalOffer,
  haveLocalPranswer,
  haveRemoteOffer,
  haveRemotePranswer,
  closed
}

SignalingState signalingStateFromString(String state) {
  assert(state != null);
  switch(state.toUpperCase()) {
    case 'STABLE':
      return SignalingState.stable;
    case 'HAVE_LOCAL_OFFER':
      return SignalingState.haveLocalOffer;
    case 'HAVE_LOCAL_PRANSWER':
      return SignalingState.haveLocalPranswer;
    case 'HAVE_REMOTE_OFFER':
      return SignalingState.haveRemoteOffer;
    case 'HAVE_REMOTE_PRANSWER':
      return SignalingState.haveRemotePranswer;
    case 'CLOSED':
      return SignalingState.closed;
    default:
      throw ArgumentError('Illegal ICE gathering state $state');
  }
}
