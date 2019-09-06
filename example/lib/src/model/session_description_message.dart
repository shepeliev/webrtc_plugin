import 'package:webrtc_plugin/webrtc_plugin.dart';

class SessionDescriptionMessage {
  final SessionDescription sessionDescription;

  SessionDescriptionMessage(this.sessionDescription);

  SessionDescriptionMessage.fromJson(Map<String, dynamic> json)
      : this(SessionDescription(
          _getSessionDescriptionType(json['type']),
          json['sdp'],
        ));

  static SessionDescriptionType _getSessionDescriptionType(String type) {
    switch (type) {
      case 'offer':
        return SessionDescriptionType.offer;
      case 'answer':
        return SessionDescriptionType.answer;
      default:
        throw Exception('Illegal type: $type');
    }
  }

  Map<String, dynamic> toJson() => {
        'type': sessionDescription.type == SessionDescriptionType.offer
            ? 'offer'
            : 'answer',
        'sdp': sessionDescription.description
      };
}
