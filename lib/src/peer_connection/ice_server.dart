import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';

class IceServer extends Equatable {
  final List<String> urls;
  final String username;
  final String password;
  final TlsCertPolicy tlsCertPolicy;
  final String hostname;
  final List<String> tlsAlpnProtocols;
  final List<String> tlsEllipticCurves;

  @override
  List<Object> get props => [
        urls,
        username,
        password,
        tlsCertPolicy,
        hostname,
        tlsAlpnProtocols,
        tlsEllipticCurves,
      ];

  IceServer({
    @required this.urls,
    this.username = "",
    this.password = "",
    this.tlsCertPolicy = TlsCertPolicy.secure,
    this.hostname = "",
    this.tlsAlpnProtocols,
    this.tlsEllipticCurves,
  }) : assert(urls.isNotEmpty);

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
