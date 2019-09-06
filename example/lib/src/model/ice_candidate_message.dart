import 'package:webrtc_plugin/webrtc_plugin.dart';

class IceCandidateMessage {
  final IceCandidate iceCandidate;

  IceCandidateMessage(this.iceCandidate);

  IceCandidateMessage.fromJson(Map<String, dynamic> json)
      : this(IceCandidate(json['id'], json['label'], json['candidate']));

  Map<String, dynamic> toJson() => {
    'type': 'candidate',
    'label': iceCandidate.sdpMLineIndex,
    'id': iceCandidate.sdpMid,
    'candidate': iceCandidate.sdp
  };
}
