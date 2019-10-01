import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/media/audio.dart';
import 'package:webrtc_plugin/src/media/media_constraints.dart';
import 'package:webrtc_plugin/src/media/video.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

import 'media.dart';

abstract class UserMedia {
  UserMedia._();

  static Future<UserMedia> initialize([
    MediaConstraints constraints = const MediaConstraints(
      audio: Audio.enabled,
      video: Video.enabled,
    ),
  ]) async {
    final userMedia = _UserMedia(globalChannel);
    await userMedia.initialize(constraints);
    return userMedia;
  }

  Future<MediaStream> createLocalMediaStream();

  Future<void> dispose();
}

const _DEFAULT_MEDIA_CONSTRAINTS = MediaConstraints(
  audio: Audio.enabled,
  video: Video.enabled,
);

class _UserMedia implements UserMedia {
  final MethodChannel _channel;

  _UserMedia(this._channel);

  Future<void> initialize([
    MediaConstraints constraints = _DEFAULT_MEDIA_CONSTRAINTS,
  ]) async {
    await _channel.invokeMethod('initializeUserMedia', constraints.toMap());
  }

  @override
  Future<MediaStream> createLocalMediaStream() async {
    final map = await _channel.invokeMapMethod('createLocalMediaStream');
    return MediaStream.fromMap(map);
  }

  @override
  Future<void> dispose() async {
    await _channel.invokeMethod('disposeUserMedia');
  }
}
