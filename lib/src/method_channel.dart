import 'package:flutter/services.dart';

const channelName = 'flutter.shepeliev.com/webrtc';
const globalChannel = MethodChannel(channelName);

Future<T> tryInvokeMethod<T>(MethodChannel channel, String method,
    [dynamic args]) async {
  try {
    return channel.invokeMethod(method, args);
  } on PlatformException catch (e) {
    throw 'Invoking method "$method" on channel "${channel.name}" failed: ${e.message}';
  }
}

Future<Map<K, V>> tryInvokeMapMethod<K, V>(MethodChannel channel, String method,
    [dynamic args]) async {
  try {
    return channel.invokeMapMethod(method, args);
  } on PlatformException catch (e) {
    throw 'Invoking method "$method" on channel "${channel.name}" failed: ${e.message}';
  }
}
