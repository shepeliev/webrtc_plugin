import 'package:equatable/equatable.dart';
import 'package:flutter/services.dart';

class SessionDescription extends Equatable {
  final SessionDescriptionType type;
  final String description;

  @override
  List<Object> get props => [type, description];

  const SessionDescription(this.type, this.description)
      : assert(type != null),
        assert(description != null);

  factory SessionDescription.fromMap(Map<dynamic, dynamic> map) {
    SessionDescriptionType type;
    switch ((map['type'] as String).toUpperCase()) {
      case 'ANSWER':
        type = SessionDescriptionType.answer;
        break;
      case 'PRANSWER':
        type = SessionDescriptionType.pranswer;
        break;
      case 'OFFER':
        type = SessionDescriptionType.offer;
        break;
      default:
        throw PlatformException(
          code: 'ILLEGAL_ALRGUMENT',
          message: 'Illegal session description type: ${map['type']}',
        );
    }
    return SessionDescription(type, map['description']);
  }

  Map<String, dynamic> toMap() =>
      {'type': type.toString().split('.').last, 'description': description};

  @override
  String toString() {
    return 'SessionDescription{type: $type, description: $description}';
  }
}

enum SessionDescriptionType { offer, pranswer, answer }
