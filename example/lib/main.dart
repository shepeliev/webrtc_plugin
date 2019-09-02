import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

const sdpConstraints = SdpConstraints(
  offerToReceiveAudio: false,
  offerToReceiveVideo: true,
  iceRestart: false,
);

class _MyAppState extends State<MyApp> {
  CallState _callState = CallState.hangup;
  MediaStream _localMediaStream;
  MediaStream _remoteMediaStream;
  RTCPeerConnection _localPeerConnection;
  RTCPeerConnection _remotePeerConnection;
  SessionDescription _offer;
  List<StreamSubscription> _remoteSubscriptions;
  final List<StreamSubscription> _localSubscriptions = [];
  final List<IceCandidate> _localIceCandidates = [];

  @override
  void initState() {
    super.initState();
    initWebRtcState();
  }

  Future<void> initWebRtcState() async {
    MediaStream localMediaStream;
    RTCPeerConnection localPeerConnection;

    try {
      localMediaStream = await getUserMedia();
      localPeerConnection = await RTCPeerConnection.create();
      await localPeerConnection.addStream(localMediaStream);
    } on PlatformException catch (e) {
      print(e);
      return;
    }

    if (!mounted) {
      await localPeerConnection.dispose();
      await localMediaStream.dispose();
      return;
    }

    _localSubscriptions.add(localPeerConnection.iceCandidates
        .listen((iceCandidate) => _localIceCandidates.add(iceCandidate)));

    _localSubscriptions
        .add(localPeerConnection.iceConnectionState.listen((state) {
      if (state == IceConnectionState.disconnected) {
        _localIceCandidates.clear();
        _createOffer();
      }
    }));

    setState(() {
      _localMediaStream = localMediaStream;
      _localPeerConnection = localPeerConnection;
      _createOffer();
    });
  }

  Future<void> _createOffer() async {
    _offer = await _localPeerConnection.createOffer(sdpConstraints);
    await _localPeerConnection.setLocalDescription(_offer);
  }

  Future<bool> _disposeApp() async {
    _localSubscriptions.forEach((subscription) => subscription.cancel());
    await _hangup();
    await _localPeerConnection.dispose();
    await _localMediaStream.dispose();
    setState(() {
      _localMediaStream = null;
    });
    return true;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: _buildHome());
  }

  Widget _buildHome() {
    return WillPopScope(
      onWillPop: _disposeApp,
      child: Scaffold(
          appBar: AppBar(title: const Text('WebRTC plugin example')),
          body: Center(child: _buildBody()),
          floatingActionButton: _buildCallButton()),
    );
  }

  Widget _buildBody() {
    return Column(
      children: <Widget>[
        Expanded(child: _buildLocalView()),
        Expanded(child: _buildRemoteView()),
      ],
    );
  }

  Widget _buildLocalView() {
    return Center(
      child: _localMediaStream == null
          ? Text("Local view")
          : MediaStreamView(
              source: _localMediaStream,
            ),
    );
  }

  Widget _buildRemoteView() {
    return Center(
      child: _remoteMediaStream == null
          ? Text("Remote view")
          : MediaStreamView(
              source: _remoteMediaStream,
            ),
    );
  }

  Widget _buildCallButton() {
    Icon icon;
    Function action;
    switch (_callState) {
      case CallState.hangup:
        icon = Icon(Icons.call);
        action = _call;
        break;
      case CallState.online:
        icon = Icon(Icons.call_end);
        action = _hangup;
        break;
      default:
        return null;
    }
    final color = _callState == CallState.online ? Colors.red : null;
    return FloatingActionButton(
      child: icon,
      onPressed: action,
      backgroundColor: color,
    );
  }

  Future<void> _call() async {
    setState(() {
      _callState = CallState.undefined;
    });

    try {
      _remotePeerConnection = await RTCPeerConnection.create();
      _remoteSubscriptions = <StreamSubscription>[];
      _remoteSubscriptions.add(_remotePeerConnection.iceCandidates.listen(
          (iceCandidate) =>
              _localPeerConnection.addIceCandidate(iceCandidate)));
      _remoteSubscriptions
          .add(_remotePeerConnection.addMediaStream.listen((mediaStream) {
        setState(() {
          _remoteMediaStream = mediaStream;
        });
      }));
      _remotePeerConnection.iceConnectionState.listen((state) {
        if (state != IceConnectionState.connected) return;
        setState(() {
          _callState = CallState.online;
        });
      });

      await _remotePeerConnection.setRemoteDescription(_offer);
      final answer = await _remotePeerConnection.createAnswer(sdpConstraints);
      await _remotePeerConnection.setLocalDescription(answer);
      await _localPeerConnection.setRemoteDescription(answer);
      _localIceCandidates.forEach((candidate) async =>
          await _remotePeerConnection.addIceCandidate(candidate));
    } on PlatformException catch (e) {
      print(e);
    }
  }

  Future<void> _hangup() async {
    if (_callState != CallState.online) return;

    setState(() {
      _callState = CallState.undefined;
    });

    _remoteSubscriptions.forEach((subscription) => subscription.cancel());
    await _remotePeerConnection.dispose();

    setState(() {
      _remoteMediaStream = null;
      _callState = CallState.hangup;
    });
  }
}

enum CallState { hangup, undefined, online }
