import 'package:equatable/equatable.dart';

class SdpConstraints extends Equatable {
  final bool offerToReceiveAudio;
  final bool offerToReceiveVideo;
  final bool iceRestart;

  @override
  List<Object> get props => [
        offerToReceiveAudio,
        offerToReceiveVideo,
        iceRestart,
      ];

  const SdpConstraints(
      {this.offerToReceiveAudio,
      this.offerToReceiveVideo,
      this.iceRestart = false});

  Map<String, dynamic> toMap() => {
        'offerToReceiveAudio': offerToReceiveAudio,
        'offerToReceiveVideo': offerToReceiveVideo,
        'iceRestart': iceRestart
      };

  @override
  String toString() {
    return 'SdpConstraints{offerToReceiveAudio: $offerToReceiveAudio, offerToReceiveVideo: $offerToReceiveVideo, iceRestart: $iceRestart}';
  }
}
