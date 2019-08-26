import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:webrtc_plugin/src/texture_renderer.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';

class MediaStreamView extends StatefulWidget {
  final MediaStream source;

  MediaStreamView({Key key, @required this.source})
      : assert(source != null),
        super(key: key);

  @override
  State<StatefulWidget> createState() => _MediaStreamViewState(source);
}

class _MediaStreamViewState extends State<MediaStreamView> {
  final MediaStream _mediaStream;
  TextureRenderer _textureRenderer;

  _MediaStreamViewState(this._mediaStream);

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    TextureRenderer renderer;
    try {
      renderer = await TextureRenderer.create();
      await _mediaStream.addRenderer(renderer);
    } on PlatformException catch (e) {
      print(e);
    }

    if (!mounted) {
      await renderer.dispose();
      return;
    }

    setState(() {
      _textureRenderer = renderer;
    });
  }

  @override
  void dispose() {
    _textureRenderer.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return _textureRenderer == null ? _buildEmptyView() : _buildTextureView();
  }

  Widget _buildEmptyView() {
    return Center(
      child: Text('Wating for texture renderer.'),
    );
  }

  Widget _buildTextureView() {
    return Texture(textureId: _textureRenderer.textureId);
  }
}
