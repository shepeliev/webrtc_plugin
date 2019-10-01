import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/media/audio.dart';
import 'package:webrtc_plugin/src/media/video.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

import 'media.dart';

abstract class UserMedia {
  UserMedia._();

  static Future<UserMedia> initialize({Audio audio, Video video}) async {
    final userMedia = _UserMedia(globalChannel);
    await userMedia.initialize({
      'audio': audio?.toMap(),
      'video': video?.toMap(),
    });
    return userMedia;
  }

  Future<MediaStream> createLocalMediaStream();

  Future<void> dispose();
}

class _UserMedia implements UserMedia {
  final MethodChannel _channel;

  _UserMedia(this._channel);

  Future<void> initialize(Map<String, dynamic> constraintsMap) async {
    await _channel.invokeMethod('initializeUserMedia', constraintsMap);
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
