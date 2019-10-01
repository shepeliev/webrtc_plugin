import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webrtc_plugin/src/media/media_constraints.dart';
import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';

import '../utils.dart';

void main() {
  MockMethodHandler methodHandler;
  UserMedia userMedia;

  setUp(() {
    methodHandler = MockMethodHandler();
    globalChannel.setMockMethodCallHandler(methodHandler);
  });

  test('initialize should call channel method with correct params', () async {
    // arrange
    final defaultConstraints = MediaConstraints();

    // act
    await UserMedia.initialize(defaultConstraints);

    // assert
    final call =
        verify(methodHandler.call(captureAny)).captured.single as MethodCall;
    expect(call.method, 'initializeUserMedia');
    expect(call.arguments, defaultConstraints.toMap());
  });

  test(
    'createLocalMediaStream should call channel method and return MediaStream',
    () async {
      // arrange
      final mediaStream = MediaStream(randomString());
      when(methodHandler.call(any))
          .thenAnswer((_) async => mediaStream.toMap());
      final userMedia = await UserMedia.initialize();

      // assert
      expect(await userMedia.createLocalMediaStream(), mediaStream);
      final call =
          verify(methodHandler.call(captureAny)).captured.last as MethodCall;
      expect(call.method, 'createLocalMediaStream');
    },
  );

  test(
    'dispose should call channel method',
    () async {
      // arrange
      final userMedia = await UserMedia.initialize();

      // act
      await userMedia.dispose();

      // assert
      final call =
          verify(methodHandler.call(captureAny)).captured.last as MethodCall;
      expect(call.method, 'disposeUserMedia');
    },
  );
}

class MockMethodHandler extends Mock implements MethodHandler {}

abstract class MethodHandler {
  Future<dynamic> call(MethodCall call);
}
