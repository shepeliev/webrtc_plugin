import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin_example/src/model/room_params.dart';

void main() {
  test('deserialize from JSON', () {
    const jsonString = '{"params": {"is_initiator": "true", '
        '"room_link": "https://appr.tc/r/159777972", '
        '"version_info": "{\\"gitHash\\": \\"c8e4f11ecccef40e92f577ede737e81d76b319ed\\", '
        '\\"branch\\": \\"master\\", \\"time\\": \\"Tue Feb 26 14:22:13 2019 +0100\\"}", '
        '"messages": ["msg1", "msg2"], "error_messages": ["error1", "error2"], "offer_options": "{}", '
        '"client_id": "62959705", "ice_server_transports": "", '
        '"bypass_join_confirmation": "false", '
        '"media_constraints": "{\\"audio\\": true, '
        '\\"video\\": {\\"optional\\": [{\\"minWidth\\": \\"1280\\"}, {\\"minHeight\\": \\"720\\"}], '
        '\\"mandatory\\": {}}}", "include_loopback_js": "", '
        '"is_loopback": "false", "wss_url": "wss://apprtc-ws.webrtc.org:443/ws", '
        '"pc_constraints": "{\\"optional\\": []}", '
        '"pc_config": "{\\"rtcpMuxPolicy\\": \\"require\\", '
        '\\"bundlePolicy\\": \\"max-bundle\\", \\"iceServers\\": []}", '
        '"wss_post_url": "https://apprtc-ws.webrtc.org:443", '
        '"ice_server_url": "https://networktraversal.googleapis.com/v1alpha/iceconfig", '
        '"warning_messages": [], "room_id": "159777972"}, "result": "SUCCESS"}';
    final json = jsonDecode(jsonString);

    final expected = RoomParams(
      true,
      '159777972',
      '62959705',
      'https://appr.tc/r/159777972',
      ['msg1', 'msg2'],
      ['error1', 'error2'],
      'wss://apprtc-ws.webrtc.org:443/ws',
      'https://networktraversal.googleapis.com/v1alpha/iceconfig',
    );
    expect(RoomParams.fromJson(json['params']), equals(expected));
  });
}
