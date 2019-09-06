import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin_example/src/model/register_command.dart';

void main() {
  test('serialize to JSON', () {
    final command = RegisterCommand(roomId: '784201063', clientId: '44375083');
    final expectedJson =
        '{"cmd":"register","roomid":"784201063","clientid":"44375083"}';

    expect(jsonEncode(command.toJson()), expectedJson);
  });
}
