import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/src/media_stream.dart';
import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/src/peer_connection/peer_connection.dart';
import 'package:webrtc_plugin/src/utils/random.dart';

void main() {
  final id = randomString();
  final channel = MethodChannel('$channelName::$id');
  final List<MethodCall> globalMethodCalls = [];
  final List<MethodCall> methodCalls = [];

  Function mockGlobalMethodHandler = (methodCall) async {};
  Function mockMethodHandler = (methodCall) async {};

  setUp(() {
    globalChannel.setMockMethodCallHandler((MethodCall methodCall) async {
      globalMethodCalls.add(methodCall);
      return await mockGlobalMethodHandler(methodCall);
    });

    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      methodCalls.add(methodCall);
      return await mockMethodHandler(methodCall);
    });
  });

  tearDown(() {
    globalMethodCalls.clear();
    methodCalls.clear();
    mockGlobalMethodHandler = (methodCall) async {};
    mockMethodHandler = (methodCall) async {};
    globalChannel.setMockMethodCallHandler(null);
    channel.setMockMethodCallHandler(null);
  });

  group('create', () {
    test('without IceServer', () async {
      mockGlobalMethodHandler = (methodCall) async {
        return {'id': id};
      };

      final peerConnection = await RTCPeerConnection.create();

      expect(globalMethodCalls.toString(),
          '[MethodCall(createPeerConnection, null)]');
      expect(peerConnection, isNotNull);
      expect(peerConnection.id, id);
    });

    test('with IceServer', () async {
      mockGlobalMethodHandler = (methodCall) async {
        return {'id': id};
      };

      final iceServer = IceServer(
          urls: ['url1', 'url2'],
          username: "user",
          password: "super_password",
          tlsCertPolicy: TlsCertPolicy.secure,
          hostname: "host1");
      final peerConnection = await RTCPeerConnection.create(iceServer);

      expect(globalMethodCalls.toString(),
          '[MethodCall(createPeerConnection, {urls: [url1, url2], username: user, password: super_password, tlsCertPolicy: secure, hostname: host1, tlsAlpnProtocols: null, tlsEllipticCurves: null})]');
      expect(peerConnection, isNotNull);
      expect(peerConnection.id, id);
    });
  });

  test('addMediaStream', () async {
    final peerConnection = RTCPeerConnection(id);
    final mediaStreamId = randomString();
    final mediaStream = MediaStream(mediaStreamId);

    await peerConnection.addMediaStream(mediaStream);

    expect(methodCalls.toString(),
        '[MethodCall(addMediaStream, {id: $mediaStreamId, videoTracks: [], audioTracks: []})]');
  });

  test('createOffer', () async {
    mockMethodHandler =
        (methodCall) async => {'type': 'OFFER', 'description': 'description'};
    final peerConnection = RTCPeerConnection(id);
    final constraints =
        SdpConstraints(offerToReceiveVideo: true, offerToReceiveAudio: true);

    final sdp = await peerConnection.createOffer(constraints);

    expect(methodCalls.toString(),
        '[MethodCall(createOffer, {offerToReceiveAudio: true, offerToReceiveVideo: true, iceRestart: false})]');
    expect(
        sdp, SessionDescription(SessionDescriptionType.offer, "description"));
  });

  test('createAnswer', () async {
    mockMethodHandler =
        (methodCall) async => {'type': 'ANSWER', 'description': 'description'};
    final peerConnection = RTCPeerConnection(id);
    final constraints =
        SdpConstraints(offerToReceiveVideo: true, offerToReceiveAudio: true);

    final sdp = await peerConnection.createAnswer(constraints);

    expect(methodCalls.toString(),
        '[MethodCall(createAnswer, {offerToReceiveAudio: true, offerToReceiveVideo: true, iceRestart: false})]');
    expect(
        sdp, SessionDescription(SessionDescriptionType.answer, "description"));
  });

  test('setLocalDescription', () async {
    final peerConnection = RTCPeerConnection(id);
    final sdp = SessionDescription(SessionDescriptionType.offer, 'description');

    await peerConnection.setLocalDescription(sdp);

    expect(methodCalls.toString(),
        '[MethodCall(setLocalDescription, {type: offer, description: description})]');
  });

  test('setRemoteDescription', () async {
    final peerConnection = RTCPeerConnection(id);
    final sdp =
        SessionDescription(SessionDescriptionType.answer, 'description');

    await peerConnection.setRemoteDescription(sdp);

    expect(methodCalls.toString(),
        '[MethodCall(setRemoteDescription, {type: answer, description: description})]');
  });

  test('addIceCandidate', () async {
    final peerConnection = RTCPeerConnection(id);
    final iceCandidate = IceCandidate(
      randomString(),
      randomInt(max: 1000),
      randomString(),
      randomString(),
    );

    await peerConnection.addIceCandidate(iceCandidate);

    expect(methodCalls.toString(),
        '[MethodCall(addIceCandidate, {sdpMid: ${iceCandidate.sdpMid}, sdpMLineIndex: ${iceCandidate.sdpMLineIndex}, sdp: ${iceCandidate.sdp}, serverUrl: ${iceCandidate.serverUrl}})]');
  });

  test('removeIceCandidates', () async {
    final peerConnection = RTCPeerConnection(id);
    final iceCandidate = IceCandidate(
      randomString(),
      randomInt(max: 1000),
      randomString(),
      randomString(),
    );

    await peerConnection.removeIceCandidates([iceCandidate]);

    expect(methodCalls.toString(),
        '[MethodCall(removeIceCandidates, [{sdpMid: ${iceCandidate.sdpMid}, sdpMLineIndex: ${iceCandidate.sdpMLineIndex}, sdp: ${iceCandidate.sdp}, serverUrl: ${iceCandidate.serverUrl}}])]');
  });

  test('iceConnectionState', () async {
    final peerConnection = RTCPeerConnection(id, true);
    final states = <IceConnectionState>[];
    final subscription =
        peerConnection.iceConnectionState.listen((state) => states.add(state));

    await channel.invokeMethod('onIceConnectionStateChange', 'NEW');
    await channel.invokeMethod('onIceConnectionStateChange', 'CHECKING');
    await channel.invokeMethod('onIceConnectionStateChange', 'CONNECTED');
    await channel.invokeMethod('onIceConnectionStateChange', 'FAILED');
    await channel.invokeMethod('onIceConnectionStateChange', 'DISCONNECTED');
    await channel.invokeMethod('onIceConnectionStateChange', 'CLOSED');

    expect(states, [
      IceConnectionState.newConnection,
      IceConnectionState.checking,
      IceConnectionState.connected,
      IceConnectionState.failed,
      IceConnectionState.disconnected,
      IceConnectionState.closed,
    ]);
    subscription.cancel();
  });

  test('iceCandidates', () async {
    final peerConnection = RTCPeerConnection(id, true);
    final candidates = <IceCandidate>[];
    final subscription = peerConnection.iceCandidates
        .listen((candidate) => candidates.add(candidate));

    final iceCandidate = IceCandidate(
        randomString(), randomInt(max: 1000), randomString(), randomString());
    await channel.invokeMethod('onIceCandidate', iceCandidate.toMap());

    expect(candidates, [iceCandidate]);
    subscription.cancel();
  });

  test('removedIceCandidates', () async {
    final peerConnection = RTCPeerConnection(id, true);
    final removedCandidates = <List<IceCandidate>>[];
    final subscription = peerConnection.removedIceCandidates
        .listen((candidates) => removedCandidates.add(candidates));

    final iceCandidate = IceCandidate(
        randomString(), randomInt(max: 1000), randomString(), randomString());
    await channel.invokeMethod('onRemoveIceCandidates', [iceCandidate.toMap()]);

    expect(removedCandidates, [
      [iceCandidate]
    ]);
    subscription.cancel();
  });

  test('addMediaStream', () async {
    final peerConnection = RTCPeerConnection(id, true);
    final mediaStreams = <MediaStream>[];
    final subscription = peerConnection.addMedaiaStream
        .listen((stream) => mediaStreams.add(stream));

    final mediaStreamId = randomString();
    final mediaStream = MediaStream(mediaStreamId);
    await channel.invokeMethod('onAddMediaStream', mediaStream.toMap());

    expect(mediaStreams, equals([mediaStream]));
    subscription.cancel();
  });

  test('removeMediaStream', () async {
    final peerConnection = RTCPeerConnection(id, true);
    final mediaStreams = <MediaStream>[];
    final subscription = peerConnection.removeMediaStream
        .listen((stream) => mediaStreams.add(stream));

    final mediaStreamId = randomString();
    final mediaStream = MediaStream(mediaStreamId);
    await channel.invokeMethod('onRemoveMediaStream', mediaStream.toMap());

    expect(mediaStreams, equals([mediaStream]));
    subscription.cancel();
  });
}
