import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/src/utils/random.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';

void main() {
  String id;
  MethodChannel channel;
  MediaStream mediaStream;

  List<MethodCall> methodCalls = [];

  setUp(() {
    id = randomString();
    channel = MethodChannel('$channelName::$id');
    mediaStream = MediaStream(id);

    channel.setMockMethodCallHandler((methodCall) async{
      methodCalls.add(methodCall);
    });
  });

  tearDown(() {
    methodCalls.clear();
  });

  test('addRenderer', () async {
    final renderer = TextureRenderer(randomString(), randomInt());
    mediaStream.addRenderer(renderer);

    expect(methodCalls.toString(),
        '[MethodCall(addRenderer, {rendererId: ${renderer.id}})]');
  });

  test('removeRenderer', () async {
    final renderer = TextureRenderer(randomString(), randomInt());
    mediaStream.removeRenderer(renderer);

    expect(methodCalls.toString(),
        '[MethodCall(removeRenderer, {rendererId: ${renderer.id}})]');
  });

  test('dispose', () async {
    await mediaStream.dispose();

    expect(methodCalls.toString(), '[MethodCall(dispose, null)]');
  });
}
