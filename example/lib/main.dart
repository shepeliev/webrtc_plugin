import 'package:flutter/material.dart';
import 'package:webrtc_plugin_example/src/join_room.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: JoinRoom(),
    );
  }
}
