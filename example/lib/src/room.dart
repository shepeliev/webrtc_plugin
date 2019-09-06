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

  StreamSubscription _localMediaStreamSubscription;
  MediaStream _localMediaStream;

  _RoomState(String roomName) : _roomController = RoomController(roomName);

  @override
  void initState() {
    super.initState();
    _localMediaStreamSubscription =
        _roomController.localMediaStream.listen((mediaStream) => setState(() {
              _localMediaStream = mediaStream;
            }));

    _joinRoom();
  }

  Future _joinRoom() async {
    try {
      _roomController.joinRoom();
    } on Exception catch(e) {
      debugPrint("Failed to join into room: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: _buildLocalView(),
      ),
    );
  }

  _buildLocalView() {
    return _localMediaStream == null
        ? Text("Waiting for camera...")
        : MediaStreamView(source: _localMediaStream);
  }

  @override
  void dispose() {
    _localMediaStreamSubscription?.cancel();
    _roomController.leaveRoom();
    super.dispose();
  }
}
