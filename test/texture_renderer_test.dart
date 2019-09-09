import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/src/utils/random.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';

void main() {
  MethodChannel channel;
  String calledMethod;
  dynamic arguments;

  setUp(() {
    globalChannel.setMockMethodCallHandler((MethodCall methodCall) async {
      switch (methodCall.method) {
        case 'createTextureRenderer':
          channel = MethodChannel('$channelName::rendererId');
          channel
              .setMockMethodCallHandler((MethodCall rendererMethodCall) async {
            calledMethod = rendererMethodCall.method;
            arguments = rendererMethodCall.arguments;
          });
          return {'id': 'rendererId', 'textureId': 123};
          break;
      }

      throw "Unkown method";
    });
  });

  tearDown(() {
    channel = null;
    calledMethod = null;
    arguments = null;
    channel?.setMockMethodCallHandler(null);
    globalChannel.setMockMethodCallHandler(null);
  });

  test('createTextureRenderer', () async {
    final renderer = await TextureRenderer.create();
    expect(renderer.id, 'rendererId');
    expect(renderer.textureId, 123);
  });

  test('dispose', () async {
    final renderer = await TextureRenderer.create();
    await renderer.dispose();

    expect(calledMethod, 'dispose');
    expect(arguments, null);
  });

  test('frameGeometryStream', () async {
    final id = randomString();
    final renderer = TextureRenderer(id, 0);
    final geometries = <FrameGeometry>[];
    renderer.frameGeometry.listen((geometry) => geometries.add(geometry));

    await _postEvent('$channelName::$id/events',
        {'width': 1280, 'height': 720, 'rotation': 0});
    await _postEvent('$channelName::$id/events',
        {'width': 720, 'height': 1280, 'rotation': 90});

    expect(geometries, [
      FrameGeometry(width: 1280, height: 720, rotation: 0),
      FrameGeometry(width: 720, height: 1280, rotation: 90),
    ]);
  });
}

Future<void> _postEvent(channel, data) async {
  // see https://github.com/flutter/flutter/issues/26528#issuecomment-512896634
  await defaultBinaryMessenger.handlePlatformMessage(
    channel,
    StandardMethodCodec().encodeSuccessEnvelope(data),
        (ByteData data) {},
  );
}
