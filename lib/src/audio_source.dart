import 'dart:async';

import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

class AudioSource {
  final String id;
  final MethodChannel _channel;

  AudioSource(this.id) : _channel = MethodChannel('$channelName::$id');

  static Future<AudioSource> create([bool isScreencast = false]) async {
    final id = await tryInvokeMethod(globalChannel, 'createAudioSource');
    return AudioSource(id);
  }

  Future<void> dispose() async => await tryInvokeMethod(_channel, 'dispose');
}

enum CameraSide { front, back }
