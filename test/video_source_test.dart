import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/src/video_source.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';

import 'utils.dart';

void main() {
  final id = randomString();
  final instanceChannel = MethodChannel('$channelName::$id');

  String calledGlobalMethod;
  dynamic globalArguments;

  String calledMethod;
  dynamic arguments;

  setUp(() {
    globalChannel.setMockMethodCallHandler((MethodCall methodCall) async {
      calledGlobalMethod = methodCall.method;
      globalArguments = methodCall.arguments;

      switch (calledGlobalMethod) {
        case 'createVideoSource':
          return randomString();
        case 'switchCamera':
          return true;
      }

      return null;
    });

    instanceChannel.setMockMethodCallHandler((MethodCall methodCall) async {
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
    instanceChannel.setMockMethodCallHandler(null);
  });

  group('create', () {
    test('isScreencast = false', () async {
      final videoSource = await VideoSource.create();

      expect(calledGlobalMethod, 'createVideoSource');
      expect(globalArguments, {'isScreencast': false});
      expect(videoSource.id.length, greaterThan(0));
    });

    test('isScreencast = true', () async {
      final videoSource = await VideoSource.create(true);

      expect(calledGlobalMethod, 'createVideoSource');
      expect(globalArguments, {'isScreencast': true});
      expect(videoSource.id.length, greaterThan(0));
    });
  });

  test('startCapture', () async {
    await VideoSource(id)
        .startCapture(width: 1280, height: 720, fps: 30, side: CameraSide.back);

    expect(calledMethod, 'startCapture');
    expect(arguments, {'width': 1280, 'height': 720, 'fps': 30, 'side': 'back'});
  });

  test('stopCapture', () async {
    await VideoSource(id).stopCapture();

    expect(calledMethod, 'stopCapture');
    expect(arguments, null);
  });

  test('switchCamera', () async {
    final newSide = await VideoSource(id).switchCamera();

    expect(newSide, CameraSide.front);
    expect(calledGlobalMethod, 'switchCamera');
    expect(globalArguments, null);
  });

  test('dispose', () async {
    await VideoSource(id).dispose();

    expect(calledMethod, 'dispose');
    expect(arguments, {'id': id});
  });
}
