import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/src/media/audio.dart';
import 'package:webrtc_plugin/src/media/media_constraints.dart';
import 'package:webrtc_plugin/src/media/video.dart';

void main() {
  test('toMap should return correct map', () {
    // act
    final map = MediaConstraints(
        audio: Audio.enabled,
        video: Video(
          minWidth: 640,
          maxWidth: 1280,
          minHeight: 480,
          maxHeight: 720,
          minFps: 15,
          maxFps: 30,
          facingMode: FacingMode.environment,
        )).toMap();

    // assert
    expect(map, {
      'audio': {},
      'video': {
        'minWidth': 640,
        'maxWidth': 1280,
        'minHeight': 480,
        'maxHeight': 720,
        'minFps': 15,
        'maxFps': 30,
        'facingMode': 'environment'
      },
    });
  });
}
