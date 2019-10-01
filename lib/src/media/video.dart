import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';

const MIN_WIDTH = 1;
const MAX_WIDTH = 3840;
const MIN_HEIGHT = 1;
const MAX_HEIGHT = 2160;
const MIN_FPS = 1;
const MAX_FPS = 60;
const IDEAL_WIDTH = 1920;
const IDEAL_HEIGHT = 1080;
const IDEAL_FPS = 30;

@immutable
class Video extends Equatable {
  final int minWidth;
  final int maxWidth;
  final int minHeight;
  final int maxHeight;
  final int minFps;
  final int maxFps;
  final FacingMode facingMode;

  static const Video enabled = Video();
  static const Video disabled = null;

  const Video({
    this.minWidth = MIN_WIDTH,
    this.maxWidth = IDEAL_WIDTH,
    this.minHeight = MIN_HEIGHT,
    this.maxHeight = IDEAL_HEIGHT,
    this.minFps = MIN_FPS,
    this.maxFps = IDEAL_FPS,
    this.facingMode = FacingMode.user,
  })  : assert(minWidth >= MIN_WIDTH),
        assert(minWidth >= MIN_WIDTH),
        assert(maxWidth <= MAX_WIDTH),
        assert(maxWidth <= MAX_WIDTH),
        assert(minWidth <= maxWidth),
        assert(minHeight >= MIN_HEIGHT),
        assert(minHeight <= MAX_HEIGHT),
        assert(maxHeight >= MIN_HEIGHT),
        assert(maxHeight <= MAX_HEIGHT),
        assert(minHeight <= maxHeight),
        assert(minFps >= MIN_FPS),
        assert(minFps <= MAX_FPS),
        assert(maxFps > MIN_FPS),
        assert(minFps <= MAX_FPS),
        assert(minFps <= maxFps);

  const Video.size({
    @required int width,
    @required int height,
    int fps,
    FacingMode facingMode,
  }) : this(
          minWidth: width,
          maxWidth: width,
          minHeight: height,
          maxHeight: height,
          minFps: fps,
          maxFps: fps,
          facingMode: facingMode,
        );

  const Video.max({
    @required int width,
    @required int height,
    int fps,
    FacingMode facingMode,
  }) : this(
          maxWidth: width,
          maxHeight: height,
          maxFps: fps,
          facingMode: facingMode,
        );

  Map<String, dynamic> toMap() => {
        'minWidth': minWidth,
        'maxWidth': maxWidth,
        'minHeight': minHeight,
        'maxHeight': maxHeight,
        'minFps': minFps,
        'maxFps': maxFps,
        'facingMode': facingMode.toString().split('.').last,
      };

  @override
  List<Object> get props => [
        minWidth,
        maxWidth,
        minHeight,
        maxHeight,
        minFps,
        maxFps,
        facingMode,
      ];
}

enum FacingMode { user, environment }
