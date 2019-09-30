import 'package:equatable/equatable.dart';

class IceCandidate extends Equatable {
  final String sdpMid;
  final int sdpMLineIndex;
  final String sdp;
  final String serverUrl;

  @override
  List<Object> get props => [sdpMid, sdpMLineIndex, sdp, serverUrl];

  IceCandidate(
    this.sdpMid,
    this.sdpMLineIndex,
    this.sdp, [
    this.serverUrl = "",
  ]);

  factory IceCandidate.fromMap(Map<dynamic, dynamic> map) {
    final String sdpMid = map['sdpMid'];
    final int sdpMLineIndex = map['sdpMLineIndex'];
    final String sdp = map['sdp'];
    final String serverUrl = map['serverUrl'];
    return IceCandidate(sdpMid, sdpMLineIndex, sdp, serverUrl);
  }

  Map<String, dynamic> toMap() => {
        'sdpMid': sdpMid,
        'sdpMLineIndex': sdpMLineIndex,
        'sdp': sdp,
        'serverUrl': serverUrl
      };

  @override
  String toString() {
    return 'IceCandidate{sdpMid: $sdpMid, sdpMLineIndex: $sdpMLineIndex, sdp: $sdp, serverUrl: $serverUrl}';
  }
}
