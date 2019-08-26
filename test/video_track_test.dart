import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';

import 'utils.dart';

void main() {
  String id;
  MethodChannel channel;
  List<MethodCall> methodCalls = [];

  setUp(() {
    id = randomString();
    channel = MethodChannel('$channelName::$id');
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      methodCalls.add(methodCall);
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
    methodCalls.clear();
  });

  test('create from Map', () {
    final map = {'id': 'track1'};

    expect(VideoTrack.fromMap(map).toString(), 'VideoTrack{id: track1}');
  });
}
