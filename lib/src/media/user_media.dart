import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/media/audio.dart';
import 'package:webrtc_plugin/src/media/video.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

import 'media.dart';

abstract class UserMedia {
  UserMedia._();

  factory UserMedia() => _UserMedia(globalChannel);

  static Future<UserMedia> create({Audio audio, Video video}) async {
    final userMedia = _UserMedia(globalChannel);
    await userMedia.initialize(audio: audio, video: video);
    return userMedia;
  }

  Future<void> initialize({Audio audio, Video video});

  Future<MediaStream> createLocalMediaStream();

  Future<void> dispose();
}

class _UserMedia implements UserMedia {
  final MethodChannel _channel;

  _UserMedia(this._channel);

  @override
  Future<void> initialize({Audio audio, Video video}) async {
    await _channel.invokeMethod('initializeUserMedia', {
      'audio': audio?.toMap(),
      'video': video?.toMap(),
    });
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
