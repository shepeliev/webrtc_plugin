import 'dart:async';

import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

class VideoSource {
  final String id;
  final MethodChannel _channel;

  VideoSource(this.id) : _channel = MethodChannel('$channelName::$id');

  static Future<VideoSource> create([bool isScreencast = false]) async {
    final id = await tryInvokeMethod(
        globalChannel, 'createVideoSource', {'isScreencast': isScreencast});
    return VideoSource(id);
  }

  Future<void> startCapture(
      {int width,
      int height,
      int fps,
      CameraSide side = CameraSide.back}) async {
    final sideValue = side.toString().split('.').last;
    final args = {
      'width': width,
      'height': height,
      'fps': fps,
      'side': sideValue
    };
    tryInvokeMethod(_channel, 'startCapture', args);
  }

  Future<void> stopCapture() async =>
      await tryInvokeMethod(_channel, 'stopCapture');

  Future<CameraSide> switchCamera() async {
    final isFront = await tryInvokeMethod(globalChannel, 'switchCamera');
    return isFront ? CameraSide.front : CameraSide.back;
  }

  Future<void> dispose() async => await tryInvokeMethod(_channel, 'dispose');
}

enum CameraSide { front, back }
