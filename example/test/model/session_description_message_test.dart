import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';
import 'package:webrtc_plugin_example/src/model/session_description_message.dart';

void main() {
  test('deserialize from JSON', () {
    var offer = jsonDecode('{"type":"offer","sdp":"description"}');
    expect(
      SessionDescriptionMessage.fromJson(offer).sessionDescription,
      SessionDescription(SessionDescriptionType.offer, "description"),
    );

    var answer = jsonDecode('{"type":"answer","sdp":"description"}');
    expect(
      SessionDescriptionMessage.fromJson(answer).sessionDescription,
      SessionDescription(SessionDescriptionType.answer, "description"),
    );
  });

  test('serialize to JSON', () {
    final sessionDescription = SessionDescription(
      SessionDescriptionType.offer,
      "description",
    );
    final message = SessionDescriptionMessage(sessionDescription);

    expect(message.toJson(), {'type': 'offer', 'sdp': 'description'});
  });
}
