import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';
import 'package:webrtc_plugin_example/src/model/ice_config.dart';

void main() {
  test('deserialize from JSON', () {
    final jsonResponse = '''{
  "lifetimeDuration": "86400s",
  "iceServers": [
    {
      "urls": [
        "stun:64.233.165.127:19302",
        "stun:[2a00:1450:4010:c08::7f]:19302"
      ]
    },
    {
      "urls": [
        "turn:64.233.161.127:19305?transport=udp",
        "turn:[2a00:1450:4010:c01::7f]:19305?transport=udp",
        "turn:64.233.161.127:19305?transport=tcp",
        "turn:[2a00:1450:4010:c01::7f]:19305?transport=tcp"
      ],
      "username": "iceServerUsername",
      "credential": "iceServerPassword",
      "maxRateKbps": "8000"
    }
  ],
  "blockStatus": "NOT_BLOCKED",
  "iceTransportPolicy": "all"
}''';


    final expectedIceServers = [
      IceServer(urls: [
        'stun:64.233.165.127:19302',
        'stun:[2a00:1450:4010:c08::7f]:19302'
      ]),
      IceServer(urls: [
        'turn:64.233.161.127:19305?transport=udp',
        'turn:[2a00:1450:4010:c01::7f]:19305?transport=udp',
        'turn:64.233.161.127:19305?transport=tcp',
        'turn:[2a00:1450:4010:c01::7f]:19305?transport=tcp'
      ],
          username: 'iceServerUsername',
          password: 'iceServerPassword')
    ];
    expect(
        IceConfig
            .fromJson(jsonDecode(jsonResponse))
            .iceServers,
        equals(expectedIceServers)
    );
  });
}
