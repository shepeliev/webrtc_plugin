import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/src/utils/flexible_range.dart';

import 'media_stream.dart';

Future<MediaStream> getUserMedia(
    [MediaStreamConstraints constraints =
        const MediaStreamConstraints()]) async {
  final result = await globalChannel.invokeMapMethod(
    'getUserMedia',
    constraints.toMap(),
  );
  return MediaStream.fromMap(result);
}

class MediaStreamConstraints {
  final bool audio;
  final VideoConstraints video;

  const MediaStreamConstraints(
      {this.audio = true, this.video = const VideoConstraints()})
      : assert(video != null);

  Map<String, dynamic> toMap() {
    return {'audio': audio, 'video': video.toMap()};
  }
}

const int _minWidth = 640;
const int _minHeight = 480;
const int _maxWidth = 1280;
const int _maxHeight = 720;
const int _minFramerate = 15;
const int _maxFramerate = 30;

class VideoConstraints {
  final FlexibleRange width;
  final FlexibleRange height;
  final FlexibleRange framerate;
  final FacingMode facingMode;

  const VideoConstraints(
      {this.width = const Range(_minWidth, _maxWidth),
      this.height = const Range(_minHeight, _maxHeight),
      this.framerate = const Range(_minFramerate, _maxFramerate),
      this.facingMode = FacingMode.user});

  Map<String, dynamic> toMap() {
    final facingModeValue = facingMode.toString().split('.').last;
    return {
      'width': width.toMap(),
      'height': height.toMap(),
      'framerate': framerate.toMap(),
      'facingMode': facingModeValue
    };
  }
}

enum FacingMode { user, environment }
