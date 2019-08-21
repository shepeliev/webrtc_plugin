import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/method_channel.dart';

class TextureRenderer {
  final String id;
  final int textureId;
  final MethodChannel _channel;

  TextureRenderer(this.id, this.textureId)
      : _channel = MethodChannel('$channelName::$id');

  static Future<TextureRenderer> create() async {
    final Map<String, Object> result =
        await tryInvokeMapMethod(globalChannel, 'createTextureRenderer');
    assert(result.containsKey('id'));
    assert(result.containsKey('textureId'));
    return TextureRenderer(result['id'], result['textureId']);
  }

  Future<void> dispose() async => await _channel.invokeMethod('dispose');
}
