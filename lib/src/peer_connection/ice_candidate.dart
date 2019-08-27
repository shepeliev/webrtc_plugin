class IceCandidate {
  final String sdpMid;
  final int sdpMLineIndex;
  final String sdp;
  final String serverUrl;

  IceCandidate(this.sdpMid, this.sdpMLineIndex, this.sdp,
      [this.serverUrl = ""]);

  Map<String, dynamic> toMap() => {
        'sdpMid': sdpMid,
        'sdpMLineIndex': sdpMLineIndex,
        'sdp': sdp,
        'serverUrl': serverUrl
      };

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is IceCandidate &&
          runtimeType == other.runtimeType &&
          sdpMid == other.sdpMid &&
          sdpMLineIndex == other.sdpMLineIndex &&
          sdp == other.sdp &&
          serverUrl == other.serverUrl;

  @override
  int get hashCode =>
      sdpMid.hashCode ^
      sdpMLineIndex.hashCode ^
      sdp.hashCode ^
      serverUrl.hashCode;

  @override
  String toString() {
    return 'IceCandidate{sdpMid: $sdpMid, sdpMLineIndex: $sdpMLineIndex, sdp: $sdp, serverUrl: $serverUrl}';
  }
}
