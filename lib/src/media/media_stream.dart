import 'package:equatable/equatable.dart';
import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';

class MediaStream  {
  final String id;
  final List<VideoTrack> videoTracks;
  final List<AudioTrack> audioTracks;
  final MethodChannel _channel;

  MediaStream(
    this.id, [
    this.videoTracks = const [],
    this.audioTracks = const [],
  ]) : _channel = MethodChannel('$channelName::$id');

  factory MediaStream.fromMap(Map<dynamic, dynamic> map) {
    assert(map != null);

    final videoTracks = (map['videoTracks'] as List<dynamic>)
        .map((track) => track as Map<dynamic, dynamic>)
        .map((track) => VideoTrack.fromMap(track))
        .toList();

    final audioTracks = (map['audioTracks'] as List<dynamic>)
        .map((track) => track as Map<dynamic, dynamic>)
        .map((track) => AudioTrack.fromMap(track))
        .toList();

    return MediaStream(map['id'], videoTracks, audioTracks);
  }

  Future<void> addRenderer(TextureRenderer renderer) async =>
      await _channel.invokeMethod('addRenderer', {'rendererId': renderer.id});

  Future<void> removeRenderer(TextureRenderer renderer) async => await _channel
      .invokeMethod("removeRenderer", {'rendererId': renderer.id});

  Future<void> dispose() async => await _channel.invokeMethod('dispose');

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'videoTracks': videoTracks.map((track) => track.toMap()).toList(),
      'audioTracks': videoTracks.map((track) => track.toMap()).toList()
    };
  }

  @override
  String toString() {
    return 'MediaStream{id: $id, videoTracks: $videoTracks, audioTracks: $audioTracks}';
  }
}

class AudioTrack extends Equatable {
  final String id;

  @override
  List<Object> get props => [id];

  AudioTrack(this.id);

  factory AudioTrack.fromMap(Map<dynamic, dynamic> map) {
    assert(map != null);
    return AudioTrack(map['id']);
  }

  Map<String, dynamic> toMap() => {'id': id};

  @override
  String toString() {
    return 'AudioTrack{id: $id}';
  }
}

class VideoTrack extends Equatable {
  final String id;

  @override
  List<Object> get props => [id];

  VideoTrack(this.id);

  factory VideoTrack.fromMap(Map<dynamic, dynamic> map) {
    assert(map != null);
    return VideoTrack(map['id']);
  }

  Map<String, dynamic> toMap() => {'id': id};

  @override
  String toString() {
    return 'VideoTrack{id: $id}';
  }
}
