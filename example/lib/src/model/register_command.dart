import 'package:flutter/cupertino.dart';

class RegisterCommand {
  final String roomId;
  final String clientId;

  RegisterCommand({@required this.roomId, @required this.clientId});

  Map<String, dynamic> toJson() => {
    'cmd': 'register',
    'roomid': roomId,
    'clientid': clientId
  };
}
