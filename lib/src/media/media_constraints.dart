import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';
import 'package:webrtc_plugin/src/media/audio.dart';
import 'package:webrtc_plugin/src/media/video.dart';

@immutable
class MediaConstraints extends Equatable {
  final Audio audio;
  final Video video;

  @override
  List<Object> get props => [audio, video];

  const MediaConstraints({this.audio, this.video});

  Map<String, dynamic> toMap() => {
        'audio': audio?.toMap(),
        'video': video?.toMap(),
      };
}
