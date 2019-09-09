import 'dart:async';

import 'package:flutter/material.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';
import 'package:webrtc_plugin_example/src/room_controller.dart';

class Room extends StatefulWidget {
  final String roomName;

  Room(this.roomName);

  @override
  State<StatefulWidget> createState() => _RoomState(roomName);
}

class _RoomState extends State<Room> {
  final RoomController _roomController;
  final _subscriptions = <StreamSubscription>[];

  MediaStream _localMediaStream;
  MediaStream _remoteMediaStream;

  _RoomState(String roomName) : _roomController = RoomController(roomName);

  @override
  void initState() {
    super.initState();
    _subscriptions.addAll([
      _roomController.localMediaStream.listen((stream) => setState(() {
            _localMediaStream = stream;
          })),
      _roomController.remoteMediaStream.listen((stream) => setState(() {
            _remoteMediaStream = stream;
          }))
    ]);

    _joinRoom();
  }

  Future _joinRoom() async {
    try {
      _roomController.joinRoom();
    } on Exception catch (e) {
      debugPrint("Failed to join into room: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: _remoteMediaStream == null
            ? _buildLocalView()
            : _buildRemoteAndLocalView(),
      ),
    );
  }

  Widget _buildRemoteAndLocalView() {
    return Stack(
      children: <Widget>[
        Container(color: Colors.black),
        Center(child: _buildRemoteView()),
        Positioned(
          right: 16.0,
          top: 48.0,
          width: 100,
          height: 100,
          child: _buildLocalView(),
        ),
      ],
    );
  }

  Widget _buildLocalView() {
    return _localMediaStream == null
        ? Text("Waiting for camera...")
        : FittedBox(
            fit: BoxFit.fitWidth,
            child: MediaStreamView(source: _localMediaStream));
  }

  Widget _buildRemoteView() => _remoteMediaStream != null
      ? FittedBox(
          fit: BoxFit.fitWidth,
          child: MediaStreamView(source: _remoteMediaStream),
        )
      : Container(
          color: Colors.black,
          child: Center(
            child: Text(
              "Waiting for remote peer..",
              style: TextStyle(color: Colors.white),
            ),
          ),
        );

  @override
  void dispose() {
    _subscriptions.forEach((s) => s.cancel());
    _roomController.leaveRoom();
    super.dispose();
  }
}
