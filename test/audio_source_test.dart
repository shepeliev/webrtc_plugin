import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/src/audio_source.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

import 'utils.dart';

void main() {
  final id = randomString();
  final channel = MethodChannel('$channelName::$id');

  String calledGlobalMethod;
  dynamic globalArguments;

  String calledMethod;
  dynamic arguments;

  setUp(() {
    globalChannel.setMockMethodCallHandler((MethodCall methodCall) async {
      calledGlobalMethod = methodCall.method;
      globalArguments = methodCall.arguments;

      switch (calledGlobalMethod) {
        case 'createAudioSource':
          return randomString();
      }

      return null;
    });

    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      calledMethod = methodCall.method;
      arguments = methodCall.arguments;
    });
  });

  tearDown(() {
    calledGlobalMethod = null;
    globalArguments = null;
    calledMethod = null;
    arguments = null;
    globalChannel.setMockMethodCallHandler(null);
    channel.setMockMethodCallHandler(null);
  });

  group('create', () {
    test('without any constraints', () async {
      final videoSource = await AudioSource.create();

      expect(calledGlobalMethod, 'createAudioSource');
      expect(globalArguments, null);
      expect(videoSource.id.length, greaterThan(0));
    });
  });

  test('dispose', () async {
    await AudioSource(id).dispose();

    expect(calledMethod, 'dispose');
    expect(arguments, null);
  });
}
