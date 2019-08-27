class SdpConstraints {
  final bool offerToReceiveAudio;
  final bool offerToReceiveVideo;
  final bool iceRestart;

  SdpConstraints(
      {this.offerToReceiveAudio,
      this.offerToReceiveVideo,
      this.iceRestart = false});

  Map<String, dynamic> toMap() => {
        'offerToReceiveAudio': offerToReceiveAudio,
        'offerToReceiveVideo': offerToReceiveVideo,
        'iceRestart': iceRestart
      };

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is SdpConstraints &&
          runtimeType == other.runtimeType &&
          offerToReceiveAudio == other.offerToReceiveAudio &&
          offerToReceiveVideo == other.offerToReceiveVideo &&
          iceRestart == other.iceRestart;

  @override
  int get hashCode =>
      offerToReceiveAudio.hashCode ^
      offerToReceiveVideo.hashCode ^
      iceRestart.hashCode;

  @override
  String toString() {
    return 'SdpConstraints{offerToReceiveAudio: $offerToReceiveAudio, offerToReceiveVideo: $offerToReceiveVideo, iceRestart: $iceRestart}';
  }
}
