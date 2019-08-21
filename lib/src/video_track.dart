import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/texture_renderer.dart';
import 'package:webrtc_plugin/src/video_source.dart';

import 'method_channel.dart';

class VideoTrack {
  final String id;
  final MethodChannel _channel;

  VideoTrack(this.id) : _channel = MethodChannel('$channelName::$id');

  static Future<VideoTrack> create(VideoSource videoSource) async {
    String id = await tryInvokeMethod(
        globalChannel, 'createVideoTrack', {'videoSourceId': videoSource.id});
    return VideoTrack(id);
  }

  Future<void> addRenderer(TextureRenderer renderer) async =>
      await tryInvokeMethod(
          _channel, 'addRenderer', {'rendererId': renderer.id});

  Future<void> removeRenderer(TextureRenderer renderer) async =>
      await tryInvokeMethod(
          _channel, "removeRenderer", {'rendererId': renderer.id});

  Future<void> dispose() async => await tryInvokeMethod(_channel, 'dispose');
}
