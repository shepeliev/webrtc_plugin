import 'package:flutter/foundation.dart';

class RoomParams {
  final bool isInitiator;
  final String roomId;
  final String clientId;
  final String roomLink;
  final List<String> messages;
  final List<String> errorMessages;
  final String wssUrl;
  final String iceServerUrl;

  RoomParams(this.isInitiator, this.roomId, this.clientId, this.roomLink,
      this.messages, this.errorMessages, this.wssUrl, this.iceServerUrl);

  RoomParams.fromJson(Map<String, dynamic> json)
      : this(
          json['is_initiator'] == 'true',
          json['room_id'],
          json['client_id'],
          json['room_link'],
          json['messages'].map<String>((m) => m.toString())?.toList() ?? [],
          json['error_messages'].map<String>((m) => m.toString())?.toList() ?? [],
          json['wss_url'],
          json['ice_server_url'],
        );

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is RoomParams &&
          runtimeType == other.runtimeType &&
          isInitiator == other.isInitiator &&
          roomId == other.roomId &&
          clientId == other.clientId &&
          roomLink == other.roomLink &&
          listEquals(messages, other.messages) &&
          listEquals(errorMessages, other.errorMessages) &&
          wssUrl == other.wssUrl &&
          iceServerUrl == other.iceServerUrl;

  @override
  int get hashCode =>
      isInitiator.hashCode ^
      roomId.hashCode ^
      clientId.hashCode ^
      roomLink.hashCode ^
      messages.hashCode ^
      errorMessages.hashCode ^
      wssUrl.hashCode ^
      iceServerUrl.hashCode;

  @override
  String toString() {
    return 'RoomParams{isInitiator: $isInitiator, roomId: $roomId, clientId: $clientId, roomLink: $roomLink, messages: $messages, errorMessages: $errorMessages, wssUrl: $wssUrl, iceServerUrl: $iceServerUrl}';
  }
}
