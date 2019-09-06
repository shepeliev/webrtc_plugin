import 'package:flutter/foundation.dart';

class IceServer {
  final List<String> urls;
  final String username;
  final String password;
  final TlsCertPolicy tlsCertPolicy;
  final String hostname;
  final List<String> tlsAlpnProtocols;
  final List<String> tlsEllipticCurves;

  IceServer(
      {@required this.urls,
      this.username = "",
      this.password = "",
      this.tlsCertPolicy = TlsCertPolicy.secure,
      this.hostname = "",
      this.tlsAlpnProtocols,
      this.tlsEllipticCurves})
      : assert(urls.isNotEmpty);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is IceServer &&
          runtimeType == other.runtimeType &&
          listEquals(urls, other.urls) &&
          username == other.username &&
          password == other.password &&
          tlsCertPolicy == other.tlsCertPolicy &&
          hostname == other.hostname &&
          listEquals(tlsAlpnProtocols, other.tlsAlpnProtocols) &&
          listEquals(tlsEllipticCurves, other.tlsEllipticCurves);

  @override
  int get hashCode =>
      urls.hashCode ^
      username.hashCode ^
      password.hashCode ^
      tlsCertPolicy.hashCode ^
      hostname.hashCode ^
      tlsAlpnProtocols.hashCode ^
      tlsEllipticCurves.hashCode;

  Map<String, dynamic> toMap() {
    final tlsCertPolicyVal = tlsCertPolicy.toString().split('.').last;
    return {
      'urls': urls,
      'username': username,
      'password': password,
      'tlsCertPolicy': tlsCertPolicyVal,
      'hostname': hostname,
      'tlsAlpnProtocols': tlsAlpnProtocols,
      'tlsEllipticCurves': tlsEllipticCurves,
    };
  }

  @override
  String toString() {
    return 'IceServer{urls: $urls, username: $username, password: $password, tlsCertPolicy: $tlsCertPolicy, hostname: $hostname, tlsAlpnProtocols: $tlsAlpnProtocols, tlsEllipticCurves: $tlsEllipticCurves}';
  }
}

enum TlsCertPolicy { secure, insecure_no_check }
