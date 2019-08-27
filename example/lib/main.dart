import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  MediaStream _localMediaStream;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    MediaStream localMediaStream;
    try {
      localMediaStream = await getUserMedia();
    } on PlatformException catch (e) {
      print(e);
    }

    if (!mounted) {
      await localMediaStream.dispose();
      return;
    }

    setState(() {
      _localMediaStream = localMediaStream;
    });
  }

  Future<bool> _disposeUserMedia() async {
    await _localMediaStream.dispose();
    setState(() {
      _localMediaStream = null;
    });
    return true;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: _buildHome());
  }

  Widget _buildHome() {
    return WillPopScope(
      onWillPop: _disposeUserMedia,
      child: Scaffold(
        appBar: AppBar(title: const Text('WebRTC example')),
        body: Center(child: _buildBody()),
      ),
    );
  }

  Widget _buildBody() {
    return Center(
        child: _localMediaStream != null
            ? MediaStreamView(source: _localMediaStream)
            : Text("Opening camera"));
  }
}
