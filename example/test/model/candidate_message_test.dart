import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';
import 'package:webrtc_plugin_example/src/model/ice_candidate_message.dart';

void main() {
  test('deserialize from JSON', () {
    var jsonMessage = jsonDecode(
        '{"type":"candidate","label":0,"id":"0","candidate":"candidate:3511422883"}');
    expect(
      IceCandidateMessage.fromJson(jsonMessage).iceCandidate,
      IceCandidate('0', 0, 'candidate:3511422883'),
    );
  });

  test('serialize to JSON', () {
    final iceCandidate = IceCandidate('0', 0, 'candidate:3511422883');
    final message = IceCandidateMessage(iceCandidate);

    expect(message.toJson(), {
      'type': 'candidate',
      'label': 0,
      'id': '0',
      'candidate': 'candidate:3511422883'
    });
  });
}
