import 'package:webrtc_plugin/webrtc_plugin.dart';

class IceConfig {
  final List<IceServer> iceServers;

  IceConfig._(this.iceServers);

  factory IceConfig.fromJson(Map<String, dynamic> json) {
    final iceServers = (json['iceServers'] as List).map((iceServerJson) {
      assert(iceServerJson is Map<String, dynamic>);
      return IceServer(
          urls: iceServerJson['urls'].map<String>((url) => url.toString()).toList(),
          username: iceServerJson['username'] ?? '',
          password: iceServerJson['credential'] ?? '');
    }).toList();

    return IceConfig._(iceServers);
  }
}
