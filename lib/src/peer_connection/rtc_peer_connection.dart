import 'dart:async';

import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/media_stream.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

import 'session_description.dart';
import 'ice_candidate.dart';
import 'ice_server.dart';
import 'session_description_constraints.dart';

class RTCPeerConnection {
  final String id;
  final MethodChannel _channel;
  final _iceCandidate = StreamController<IceCandidate>();
  final _removedIceCandidates = StreamController<List<IceCandidate>>();
  final _iceConnectionState = StreamController<IceConnectionState>();
  final _addMediaStream = StreamController<MediaStream>();
  final _removeMediaStream = StreamController<MediaStream>();

  Stream<IceCandidate> get iceCandidates => _iceCandidate.stream;
  Stream<List<IceCandidate>> get removedIceCandidates =>
      _removedIceCandidates.stream;
  Stream<IceConnectionState> get iceConnectionState =>
      _iceConnectionState.stream;
  Stream<MediaStream> get addMedaiaStream => _addMediaStream.stream;
  Stream<MediaStream> get removeMediaStream => _removeMediaStream.stream;

  RTCPeerConnection(this.id, [bool mockMethodCallHandler = false])
      : assert(id != null),
        _channel = MethodChannel('$channelName::$id') {
    if (mockMethodCallHandler) {
      _channel.setMockMethodCallHandler(_methodCallHandler);
    } else {
      _channel.setMethodCallHandler(_methodCallHandler);
    }
  }

  Future<void> _methodCallHandler(MethodCall methodCall) async {
    switch (methodCall.method) {
      case 'onIceCandidate':
        _onIceCandidate(methodCall.arguments);
        break;
      case 'onRemoveIceCandidates':
        _onRemoveIceCandidates(methodCall.arguments);
        break;
      case 'onIceConnectionStateChange':
        _onIceConnectionStateChange(methodCall.arguments);
        break;
      case 'onAddMediaStream':
        _onAddMediaStream(methodCall.arguments);
        break;
      case 'onRemoveMediaStream':
        _onRemoveMediaStream(methodCall.arguments);
        break;
      default:
        throw PlatformException(
          code: 'UNSUPPORTED_OPERATION',
          message: 'Method "${methodCall.method} is not implemented .',
        );
    }
    return Future.value(null);
  }

  void _onIceCandidate(dynamic arguments) {
    _iceCandidate.add(_mapToIceCandidate(arguments));
  }

  void _onRemoveIceCandidates(dynamic arguments) {
    assert(arguments is List<dynamic>);
    final iceCandidateMaps = arguments as List<dynamic>;
    final removedIceCandidates =
        iceCandidateMaps.map(_mapToIceCandidate).toList();
    _removedIceCandidates.add(removedIceCandidates);
  }

  void _onIceConnectionStateChange(dynamic arguments) =>
      _iceConnectionState.add(_mapToIceConnectionState(arguments));

  void _onAddMediaStream(dynamic arguments) =>
      _addMediaStream.add(MediaStream.fromMap(arguments));

  void _onRemoveMediaStream(dynamic arguments) =>
      _removeMediaStream.add(MediaStream.fromMap(arguments));

  IceCandidate _mapToIceCandidate(dynamic map) {
    assert(map is Map<dynamic, dynamic>);
    final String sdpMid = map['sdpMid'];
    final int sdpMLineIndex = map['sdpMLineIndex'];
    final String sdp = map['sdp'];
    final String serverUrl = map['serverUrl'];
    return IceCandidate(sdpMid, sdpMLineIndex, sdp, serverUrl);
  }

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

  Future<void> addMediaStream(MediaStream stream) async {
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

  Future<void> dispose() async {
    await tryInvokeMethod(_channel, 'dispose');
    await Future.wait([
      _addMediaStream.close(),
      _removeMediaStream.close(),
      _iceConnectionState.close(),
      _iceCandidate.close(),
      _removedIceCandidates.close(),
    ]);
  }
}

enum IceConnectionState {
  newConnection,
  checking,
  connected,
  failed,
  disconnected,
  closed
}
