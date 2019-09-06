import 'dart:convert';
import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:webrtc_plugin_example/src/model/register_command.dart';
import 'package:webrtc_plugin_example/src/model/room_params.dart';

class Signaling {
  WebSocket _webSocket;

  Stream<Map<String, dynamic>> get signalingStream => _webSocket.map((message) {
        debugPrint('WSS->C: $message');
        return jsonDecode(message);
      });

  Signaling._(this._webSocket);

  static Future<Signaling> initialize(RoomParams roomParams) async {
    assert(roomParams != null);
    return Signaling._(await WebSocket.connect(
      roomParams.wssUrl,
      headers: {'Origin': 'https://appr.tc'},
    ));
  }

  void _sendString(String message) {
    _webSocket.add(message);
    debugPrint('C->WSS: $message');
  }

  void sendRegisterSignal(RoomParams roomParams) {
    final registerCommand = RegisterCommand(
        roomId: roomParams.roomId, clientId: roomParams.clientId);
    _sendString(jsonEncode(registerCommand.toJson()));
  }

  void sendMessage(Map<String, dynamic> json) {
    final message = {'cmd': 'send', 'msg': jsonEncode(json)};
    _sendString(jsonEncode(message));
  }

  void sendByeSignal() {
    sendMessage({'type': 'bye'});
  }

  void dispose() {
    _webSocket?.close();
  }
}
