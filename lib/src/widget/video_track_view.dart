import 'package:flutter/cupertino.dart';
import 'package:webrtc_plugin/src/texture_renderer.dart';

class VideoTrackView extends StatefulWidget {
  final TextureRenderer textureRenderer;

  VideoTrackView({Key key, this.textureRenderer}) : super(key: key);

  @override
  State<StatefulWidget> createState() => _VideoTrackViewState(textureRenderer);
}

class _VideoTrackViewState extends State<VideoTrackView> {
  final TextureRenderer _renderer;

  _VideoTrackViewState(this._renderer);

  @override
  void dispose() {
    _renderer.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Texture(textureId: _renderer.textureId);
  }
}
