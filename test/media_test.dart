import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';

void main() {
  List<MethodCall> methodCalls = [];
  Function mockMethodHandler;

  setUp(() {
    globalChannel.setMockMethodCallHandler((methodCall) async {
      methodCalls.add(methodCall);
      return mockMethodHandler != null
          ? await mockMethodHandler(methodCall)
          : null;
    });
  });

  tearDown(() {
    methodCalls.clear();
    mockMethodHandler = null;
  });

  group('getUserMedia()', () {
    test('with default constraints', () async {
      mockMethodHandler = (MethodCall methodCall) async {
        return {
          'id': 'mediaStream1',
          'videoTracks': [
            {'id': 'videoTrack1'}
          ],
          'audioTracks': [
            {'id': 'audioTrack1'}
          ],
        };
      };

      final mediaStream = await getUserMedia();

      expect(mediaStream.id, 'mediaStream1');
      expect(mediaStream.videoTracks.first.id, 'videoTrack1');
      expect(mediaStream.audioTracks.first.id, 'audioTrack1');

      expect(methodCalls.toString(), '[MethodCall(getUserMedia, {audio: true, video: {width: {min: 640, ideal: 1280, max: 1280}, height: {min: 480, ideal: 720, max: 720}, framerate: {min: 15, ideal: 30, max: 30}, facingMode: user}})]');
    });
  });
}
