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
  VideoSource _videoSource;
  VideoTrack _videoTrack;
  TextureRenderer _textureRenderer;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    VideoSource videoSource;
    VideoTrack videoTrack;
    TextureRenderer renderer;
    try {
      videoSource = await VideoSource.create();
      videoTrack = await VideoTrack.create(videoSource);
      renderer = await TextureRenderer.create();
      await videoTrack.addRenderer(renderer);
      await videoSource.startCapture(width: 1280, height: 720, fps: 30);
    } on PlatformException catch (e) {
      print(e);
    }

    if (!mounted) {
      await renderer.dispose();
      await videoTrack.dispose();
      await videoSource.dispose();
      return;
    }

    setState(() {
      _videoSource = videoSource;
      _videoTrack = videoTrack;
      _textureRenderer = renderer;
    });
  }

  Future<bool> _disposeVideo() async {
    await _textureRenderer.dispose();
    await _videoTrack.dispose();
    await _videoSource.dispose();
    return true;
  }

  @override
  Widget build(BuildContext context) {
    final child = _textureRenderer != null
        ? VideoTrackView(textureRenderer: _textureRenderer)
        : Text("Opening camera");

    return MaterialApp(
        home: WillPopScope(
      onWillPop: _disposeVideo,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: child,
        ),
      ),
    ));
  }
}
