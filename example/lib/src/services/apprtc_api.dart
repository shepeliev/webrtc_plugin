import 'dart:async';
import 'dart:convert';

import 'package:flutter/cupertino.dart';
import 'package:http/http.dart' as http;
import 'package:webrtc_plugin_example/src/model/ice_config.dart';
import 'package:webrtc_plugin_example/src/model/room_params.dart';

const _baseUrl = 'https://appr.tc';

Future<RoomParams> joinRoom(String roomName) async {
  final response = await http.post('$_baseUrl/join/$roomName');
  if (response.statusCode == 200) {
    final json = jsonDecode(response.body);
    debugPrint('GCE->C: $json');
    if (json['result'] != 'SUCCESS') {
      throw Exception('Faild to foin the room. Response: $json');
    }
    return RoomParams.fromJson(json['params']);
  }

  throw Exception('Failed to join the room. '
      'Status code: ${response.statusCode}, error: ${response.body}');
}

Future<IceConfig> fetchIceConfig(String iceConfigUrl) async {
  assert(iceConfigUrl != null);

  final response =
      await http.post(iceConfigUrl, headers: {'Referer': _baseUrl});

  if (response.statusCode != 200) {
    throw Exception('Failed to fetch ICE config'
        'Status code: ${response.statusCode}, error: ${response.body}');
  }

  return IceConfig.fromJson(jsonDecode(response.body));
}

Future postMessage(RoomParams roomParams, Map<String, dynamic> json) async {
  assert(roomParams != null);
  assert(json != null);

  var encodedJson = jsonEncode(json);
  debugPrint('C->GCE: $encodedJson');
  final url = '$_baseUrl/message/${roomParams.roomId}/${roomParams.clientId}';
  final response = await http.post(url, body: encodedJson);

  if (response.statusCode != 200) {
    throw Exception('Failed to post message to GCE.'
        'Status code: ${response.statusCode}, error: ${response.body}');
  }

  final responseJson = jsonDecode(response.body);
  if (responseJson['result'] != 'SUCCESS') {
    throw Exception(
        'Failed to post message to GCE. GCE response: $responseJson');
  }
}

Future leaveRoom(RoomParams roomParams) async {
  assert(roomParams != null);

  final url = '$_baseUrl/leave/${roomParams.roomId}/${roomParams.clientId}';
  final response = await http.post(url);

  if (response.statusCode != 200) {
    throw Exception('Failed to leave the room.'
        'Status code: ${response.statusCode}, error: ${response.body}');
  }
}
