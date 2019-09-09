import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

class TextureRenderer {
  final String id;
  final int textureId;
  final MethodChannel _channel;
  final Stream<dynamic> _eventStream;

  Stream<FrameGeometry> get frameGeometry =>
      _eventStream.map((data) => FrameGeometry(
          width: data['width'],
          height: data['height'],
          rotation: data['rotation']));

  TextureRenderer(this.id, this.textureId)
      : _channel = MethodChannel('$channelName::$id'),
        _eventStream =
            EventChannel('$channelName::$id/events').receiveBroadcastStream();

  static Future<TextureRenderer> create() async {
    final Map<String, Object> result =
        await tryInvokeMapMethod(globalChannel, 'createTextureRenderer');
    assert(result.containsKey('id'));
    assert(result.containsKey('textureId'));
    return TextureRenderer(result['id'], result['textureId']);
  }

  Future<void> dispose() async => await _channel.invokeMethod('dispose');
}

class FrameGeometry {
  final int width;
  final int height;
  final int rotation;

  FrameGeometry(
      {@required this.width, @required this.height, @required this.rotation})
      : assert(width != null),
        assert(height != null),
        assert(rotation != null);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
          other is FrameGeometry &&
              runtimeType == other.runtimeType &&
              width == other.width &&
              height == other.height &&
              rotation == other.rotation;

  @override
  int get hashCode =>
      width.hashCode ^
      height.hashCode ^
      rotation.hashCode;

  @override
  String toString() {
    return 'FrameGeometry{width: $width, height: $height, orientation: $rotation}';
  }
}
