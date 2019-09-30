import 'package:equatable/equatable.dart';
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
        await globalChannel.invokeMapMethod('createTextureRenderer');
    assert(result.containsKey('id'));
    assert(result.containsKey('textureId'));
    return TextureRenderer(result['id'], result['textureId']);
  }

  Future<void> dispose() async => await _channel.invokeMethod('dispose');
}

class FrameGeometry extends Equatable {
  final int width;
  final int height;
  final int rotation;

  @override
  List<Object> get props => [width, height, rotation];

  FrameGeometry({
    @required this.width,
    @required this.height,
    @required this.rotation,
  })  : assert(width != null),
        assert(height != null),
        assert(rotation != null);

  @override
  String toString() {
    return 'FrameGeometry{width: $width, height: $height, orientation: $rotation}';
  }
}
