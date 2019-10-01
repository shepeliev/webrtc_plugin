import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';
import 'package:webrtc_plugin_example/src/model/ice_candidate_message.dart';
import 'package:webrtc_plugin_example/src/model/room_params.dart';
import 'package:webrtc_plugin_example/src/model/session_description_message.dart';
import 'package:webrtc_plugin_example/src/services/apprtc_api.dart' as api;
import 'package:webrtc_plugin_example/src/services/signaling.dart';

class RoomController {
  final String roomName;
  final _localMediaStreamController = StreamController<MediaStream>();
  final _remoteMediaStreamController = StreamController<MediaStream>();
  final _errorMessageStreamController = StreamController<String>();
  final _subscriptions = <StreamSubscription>[];

  RoomParams _roomParams;
  bool _isInitiator;
  UserMedia _userMedia;
  MediaStream _localMediaStream;
  Signaling _signaling;
  RtcPeerConnection _rtcPeerConnection;

  Stream<MediaStream> get localMediaStream =>
      _localMediaStreamController.stream;

  Stream<MediaStream> get remoteMediaStream =>
      _remoteMediaStreamController.stream;

  Stream<String> get errorMessage => _errorMessageStreamController.stream;

  RoomController(this.roomName);

  Future<void> joinRoom() async {
    _roomParams = await api.joinRoom(roomName);
    _isInitiator = _roomParams.isInitiator;
    await _initUserMedia();
    await _createRtcPeerConnection();
    await _initSignaling();
    await _setupRtcConnection();
  }

  Future _initUserMedia() async {
    _userMedia = await UserMedia.initialize(
      audio: Audio.enabled,
      video: Video.size(width: 640, height: 480),
    );
    _localMediaStream = await _userMedia.createLocalMediaStream();
    _localMediaStreamController.add(_localMediaStream);
  }

  Future _initSignaling() async {
    _signaling = await Signaling.initialize(_roomParams);
    _subscriptions.add(
      _signaling.signalingStream.listen((message) async {
        final error = message['error']?.toString() ?? '';
        if (error.length > 0) {
          _error('WSS error received: $error', uiMessage: error);
        } else {
          await _handleMessage(jsonDecode(message['msg']));
        }
      }),
    );
    _signaling.sendRegisterSignal(_roomParams);
  }

  Future _handleMessage(Map<String, dynamic> json) async {
    var type = json['type'];
    switch (type) {
      case 'offer':
        await _handleOffer(
            SessionDescriptionMessage.fromJson(json).sessionDescription);
        break;
      case 'answer':
        await _handleAnswer(
            SessionDescriptionMessage.fromJson(json).sessionDescription);
        break;
      case 'candidate':
        await _handleIceCandidate(
            IceCandidateMessage.fromJson(json).iceCandidate);
        break;
      case 'bye':
        _handleBye();
        break;
      default:
        _error('Unkown message type: $json');
    }
  }

  Future _handleOffer(SessionDescription sessionDescription) async {
    try {
      await _rtcPeerConnection.setRemoteDescription(sessionDescription);
      final answer = await _rtcPeerConnection
          .createAnswer(_getSdpConstraints(restart: false));
      await _rtcPeerConnection.setLocalDescription(answer);
      await _sendSessionDescription(answer);
    } on PlatformException catch (e) {
      _error('Failed to handle offer. Error: $e');
    }
  }

  Future _handleAnswer(SessionDescription sessionDescription) async {
    try {
      await _rtcPeerConnection.setRemoteDescription(sessionDescription);
    } on PlatformException catch (e) {
      _error('Failed to handle answer. Error: $e');
    }
  }

  Future _handleIceCandidate(IceCandidate iceCandidate) async {
    try {
      await _rtcPeerConnection.addIceCandidate(iceCandidate);
    } on PlatformException catch (e) {
      _error('Failed to hanlde ICE candidate. Error: $e');
    }
  }

  Future _handleBye() async {
    _remoteMediaStreamController.add(null);
    await _rtcPeerConnection.removeMediaStream(_localMediaStream);
    _cancelSubscriptions();
    _disposeSignaling();
    await _disposeRtcPeerConnection();
    _isInitiator = true;
    await _createRtcPeerConnection();
    await _initSignaling();
    await _setupRtcConnection();
  }

  Future _createRtcPeerConnection() async {
    final iceConfig = await api.fetchIceConfig(_roomParams.iceServerUrl);
    _rtcPeerConnection = await RtcPeerConnection.create(iceConfig.iceServers);
    _subscribeOnRtcPeerConnectionEvents();
    await _rtcPeerConnection.addMediaStream(_localMediaStream);
  }

  void _subscribeOnRtcPeerConnectionEvents() {
    if (_rtcPeerConnection == null) return;
    final subscriptions = [
      _rtcPeerConnection
          .on(RtcPeerConnectionEvent.iceCandidate)
          .map((d) => IceCandidate.fromMap(d))
          .listen((candidate) => _sendIceCandidate(candidate)),
      _rtcPeerConnection
          .on(RtcPeerConnectionEvent.addStream)
          .map((d) => MediaStream.fromMap(d))
          .listen((stream) => _remoteMediaStreamController.add(stream)),
      _rtcPeerConnection
          .on(RtcPeerConnectionEvent.removeStream)
          .listen((_) => _remoteMediaStreamController.add(null)),
      _rtcPeerConnection
          .on(RtcPeerConnectionEvent.iceConnectionChange)
          .map((d) => iceConnectionStateFromString(d))
          .listen((state) {
        if (state == IceConnectionState.disconnected) {
          _handleBye();
        }
      })
    ];
    _subscriptions.addAll(subscriptions);
  }

  Future _sendIceCandidate(IceCandidate iceCandidate) async {
    final message = IceCandidateMessage(iceCandidate);
    if (_isInitiator) {
      api.postMessage(_roomParams, message.toJson());
    } else {
      _signaling.sendMessage(message.toJson());
    }
  }

  Future _setupRtcConnection() async {
    if (_rtcPeerConnection == null) return;
    if (_isInitiator) {
      await _createOffer();
    } else {
      _roomParams.messages
          .forEach((msg) async => await _handleMessage(jsonDecode(msg)));
    }
  }

  Future _createOffer({bool restart = false}) async {
    try {
      final sessionDescription = await _rtcPeerConnection
          .createOffer(_getSdpConstraints(restart: restart));
      await _rtcPeerConnection.setLocalDescription(sessionDescription);
      _sendSessionDescription(sessionDescription);
    } on PlatformException catch (e) {
      _error('Failed to setup RTC connection. Error: $e',
          uiMessage: 'Failed to setup RTC connection');
    }
  }

  Future _sendSessionDescription(SessionDescription sessionDescription) async {
    final message = SessionDescriptionMessage(sessionDescription);
    if (_isInitiator) {
      api.postMessage(_roomParams, message.toJson());
    } else {
      _signaling.sendMessage(message.toJson());
    }
  }

  Future leaveRoom() async {
    debugPrint('Leaving room: "$roomName"');
    _signaling?.sendByeSignal();
    await api.leaveRoom(_roomParams);
    _roomParams = null;
    _cancelSubscriptions();
    _disposeSignaling();
    await _disposeRtcPeerConnection();
    await _disposeUserMedia();
  }

  void _disposeSignaling() {
    _signaling?.dispose();
    _signaling = null;
  }

  void _cancelSubscriptions() {
    _subscriptions.forEach((s) => s.cancel());
    _subscriptions.clear();
  }

  Future _disposeRtcPeerConnection() async {
    await _rtcPeerConnection?.dispose();
    _rtcPeerConnection = null;
  }

  Future _disposeUserMedia() async {
    await _localMediaStream?.dispose();
    await _userMedia?.dispose();
    _localMediaStream = null;
  }

  SdpConstraints _getSdpConstraints({@required bool restart}) => SdpConstraints(
        offerToReceiveAudio: true,
        offerToReceiveVideo: true,
        iceRestart: restart,
      );

  void _error(String logMessage, {String uiMessage}) {
    debugPrint(logMessage);
    if (uiMessage != null) {
      _errorMessageStreamController.add(uiMessage);
    }
  }
}
