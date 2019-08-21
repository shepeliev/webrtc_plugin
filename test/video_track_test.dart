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
        case 'createVideoTrack':
          return randomString();
      }

      throw 'UnsuppoertedMethod';
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

  test('create', () async {
    String videoSourceId = randomString();
    final videoSource = VideoSource(videoSourceId);
    final videoTrack = await VideoTrack.create(videoSource);

    expect(calledGlobalMethod, 'createVideoTrack');
    expect(globalArguments, {'videoSourceId': videoSourceId});
    expect(videoTrack.id.length, greaterThan(0));
  });

  test('addRenderer', () async {
    final renderer = TextureRenderer(randomString(), randomInt());
    VideoTrack(id).addRenderer(renderer);

    expect(calledMethod, 'addRenderer');
    expect(arguments, {'rendererId': renderer.id});
  });

  test('removeRenderer', () async {
    final renderer = TextureRenderer(randomString(), randomInt());
    VideoTrack(id).removeRenderer(renderer);

    expect(calledMethod, 'removeRenderer');
    expect(arguments, {'rendererId': renderer.id});
  });

  test('dispose', () async {
    VideoTrack(id).dispose();

    expect(calledMethod, 'dispose');
    expect(arguments, null);
  });
}
