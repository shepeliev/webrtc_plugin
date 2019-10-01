import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/webrtc_plugin.dart';
import 'package:webrtc_plugin/src/method_channel.dart';
import 'package:webrtc_plugin/src/peer_connection/peer_connection.dart';
import 'package:webrtc_plugin/src/utils/random.dart';

final id = randomString();

void main() {
  final channel = MethodChannel('$channelName::$id');
  final List<MethodCall> globalMethodCalls = [];
  final List<MethodCall> methodCalls = [];

  Function mockGlobalMethodHandler = (methodCall) async {};
  Function mockMethodHandler = (methodCall) async {};

  // stub EventChannel method handler
  MethodChannel('$channelName::$id/events')
      .setMockMethodCallHandler((methodCall) async {});

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

      final peerConnection = await RtcPeerConnection.create();

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
      final peerConnection = await RtcPeerConnection.create([iceServer]);

      expect(globalMethodCalls.toString(),
          '[MethodCall(createPeerConnection, [{urls: [url1, url2], username: user, password: super_password, tlsCertPolicy: secure, hostname: host1, tlsAlpnProtocols: null, tlsEllipticCurves: null}])]');
      expect(peerConnection, isNotNull);
      expect(peerConnection.id, id);
    });
  });

  test('addMediaStream', () async {
    mockMethodHandler = (methodCall) async => true;
    final peerConnection = RtcPeerConnection(id);
    final mediaStreamId = randomString();
    final mediaStream = MediaStream(mediaStreamId);

    final result = await peerConnection.addMediaStream(mediaStream);

    expect(result, isTrue);
    expect(methodCalls.toString(),
        '[MethodCall(addMediaStream, {id: $mediaStreamId, videoTracks: [], audioTracks: []})]');
  });

  test('removeMediaStream', () async {
    mockMethodHandler = (methodCall) async => true;
    final peerConnection = RtcPeerConnection(id);
    final mediaStreamId = randomString();
    final mediaStream = MediaStream(mediaStreamId);

    await peerConnection.removeMediaStream(mediaStream);

    expect(methodCalls.toString(),
        '[MethodCall(removeMediaStream, {id: $mediaStreamId, videoTracks: [], audioTracks: []})]');
  });

  test('createOffer', () async {
    mockMethodHandler =
        (methodCall) async => {'type': 'OFFER', 'description': 'description'};
    final peerConnection = RtcPeerConnection(id);
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
    final peerConnection = RtcPeerConnection(id);
    final constraints =
        SdpConstraints(offerToReceiveVideo: true, offerToReceiveAudio: true);

    final sdp = await peerConnection.createAnswer(constraints);

    expect(methodCalls.toString(),
        '[MethodCall(createAnswer, {offerToReceiveAudio: true, offerToReceiveVideo: true, iceRestart: false})]');
    expect(
        sdp, SessionDescription(SessionDescriptionType.answer, "description"));
  });

  test('setLocalDescription', () async {
    final peerConnection = RtcPeerConnection(id);
    final sdp = SessionDescription(SessionDescriptionType.offer, 'description');

    await peerConnection.setLocalDescription(sdp);

    expect(methodCalls.toString(),
        '[MethodCall(setLocalDescription, {type: offer, description: description})]');
  });

  test('setRemoteDescription', () async {
    final peerConnection = RtcPeerConnection(id);
    final sdp =
        SessionDescription(SessionDescriptionType.answer, 'description');

    await peerConnection.setRemoteDescription(sdp);

    expect(methodCalls.toString(),
        '[MethodCall(setRemoteDescription, {type: answer, description: description})]');
  });

  test('addIceCandidate', () async {
    mockMethodHandler = (methodCall) async => true;
    final peerConnection = RtcPeerConnection(id);
    final iceCandidate = IceCandidate(
      randomString(),
      randomInt(max: 1000),
      randomString(),
      randomString(),
    );

    final result = await peerConnection.addIceCandidate(iceCandidate);

    expect(result, isTrue);
    expect(methodCalls.toString(),
        '[MethodCall(addIceCandidate, {sdpMid: ${iceCandidate.sdpMid}, sdpMLineIndex: ${iceCandidate.sdpMLineIndex}, sdp: ${iceCandidate.sdp}, serverUrl: ${iceCandidate.serverUrl}})]');
  });

  test('removeIceCandidates', () async {
    mockMethodHandler = (methodCall) async => true;
    final peerConnection = RtcPeerConnection(id);
    final iceCandidate = IceCandidate(
      randomString(),
      randomInt(max: 1000),
      randomString(),
      randomString(),
    );

    final result = await peerConnection.removeIceCandidates([iceCandidate]);

    expect(result, isTrue);
    expect(methodCalls.toString(),
        '[MethodCall(removeIceCandidates, [{sdpMid: ${iceCandidate.sdpMid}, sdpMLineIndex: ${iceCandidate.sdpMLineIndex}, sdp: ${iceCandidate.sdp}, serverUrl: ${iceCandidate.serverUrl}}])]');
  });

  group('onEvent', () {
    test('iceConnectionState', () async {
      final peerConnection = RtcPeerConnection(id);
      final states = <IceConnectionState>[];
      final subscription = peerConnection
          .on(RtcPeerConnectionEvent.iceConnectionChange)
          .map((d) => iceConnectionStateFromString(d))
          .listen((state) => states.add(state));

      await _postEvent({'type': 'iceConnectionChange', 'data': 'NEW'});
      await _postEvent({'type': 'iceConnectionChange', 'data': 'CHECKING'});
      await _postEvent({'type': 'iceConnectionChange', 'data': 'CONNECTED'});
      await _postEvent({'type': 'iceConnectionChange', 'data': 'FAILED'});
      await _postEvent({'type': 'iceConnectionChange', 'data': 'DISCONNECTED'});
      await _postEvent({'type': 'iceConnectionChange', 'data': 'CLOSED'});
      await _postEvent({'type': 'iceConnectionChange', 'data': 'COMPLETED'});

      expect(states, [
        IceConnectionState.starting,
        IceConnectionState.checking,
        IceConnectionState.connected,
        IceConnectionState.failed,
        IceConnectionState.disconnected,
        IceConnectionState.closed,
        IceConnectionState.completed,
      ]);
      subscription.cancel();
    });

    test('iceGatheringState', () async {
      final peerConnection = RtcPeerConnection(id);
      final states = <IceGatheringState>[];
      final subscription = peerConnection
          .on(RtcPeerConnectionEvent.iceGatheringChange)
          .map((d) => iceGatheringStateFromString(d))
          .listen((state) => states.add(state));

      await _postEvent({'type': 'iceGatheringChange', 'data': 'NEW'});
      await _postEvent({'type': 'iceGatheringChange', 'data': 'GATHERING'});
      await _postEvent({'type': 'iceGatheringChange', 'data': 'COMPLETE'});

      expect(states, [
        IceGatheringState.starting,
        IceGatheringState.gathering,
        IceGatheringState.complete,
      ]);
      subscription.cancel();
    });

    test('signalingChange', () async {
      final peerConnection = RtcPeerConnection(id);
      final states = <SignalingState>[];
      final subscription = peerConnection
          .on(RtcPeerConnectionEvent.signalingChange)
          .map((d) => signalingStateFromString(d))
          .listen((state) => states.add(state));

      await _postEvent({'type': 'signalingChange', 'data': 'STABLE'});
      await _postEvent({'type': 'signalingChange', 'data': 'HAVE_LOCAL_OFFER'});
      await _postEvent(
          {'type': 'signalingChange', 'data': 'HAVE_LOCAL_PRANSWER'});
      await _postEvent(
          {'type': 'signalingChange', 'data': 'HAVE_REMOTE_OFFER'});
      await _postEvent(
          {'type': 'signalingChange', 'data': 'HAVE_REMOTE_PRANSWER'});
      await _postEvent({'type': 'signalingChange', 'data': 'CLOSED'});

      expect(states, [
        SignalingState.stable,
        SignalingState.haveLocalOffer,
        SignalingState.haveLocalPranswer,
        SignalingState.haveRemoteOffer,
        SignalingState.haveRemotePranswer,
        SignalingState.closed,
      ]);
      subscription.cancel();
    });

    test('iceCandidate', () async {
      final peerConnection = RtcPeerConnection(id);
      final candidates = <IceCandidate>[];
      final subscription = peerConnection
          .on(RtcPeerConnectionEvent.iceCandidate)
          .map((data) => IceCandidate.fromMap(data))
          .listen((candidate) => candidates.add(candidate));

      final iceCandidate = IceCandidate(
        randomString(),
        randomInt(max: 1000),
        randomString(),
        randomString(),
      );
      await _postEvent({
        'type': 'iceCandidate',
        'data': iceCandidate.toMap(),
      });

      expect(candidates, [iceCandidate]);
      subscription.cancel();
    });

    test('removedIceCandidates', () async {
      final peerConnection = RtcPeerConnection(id);
      final removedCandidates = <List<IceCandidate>>[];
      final subscription = peerConnection
          .on(RtcPeerConnectionEvent.iceCandidatesRemoved)
          .map((data) =>
              data.map<IceCandidate>((c) => IceCandidate.fromMap(c)).toList())
          .listen((candidates) => removedCandidates.add(candidates));

      final iceCandidate = IceCandidate(
          randomString(), randomInt(max: 1000), randomString(), randomString());
      await _postEvent({
        'type': 'iceCandidatesRemoved',
        'data': [iceCandidate.toMap()],
      });

      expect(removedCandidates, [
        [iceCandidate]
      ]);
      subscription.cancel();
    });

    test('addStream', () async {
      final peerConnection = RtcPeerConnection(id);
      final remoteStreams = <MediaStream>[];
      final subscription = peerConnection
          .on(RtcPeerConnectionEvent.addStream)
          .map((data) => MediaStream.fromMap(data))
          .listen((stream) => remoteStreams.add(stream));

      final mediaStreamId = randomString();
      final mediaStream = MediaStream(mediaStreamId);
      await _postEvent({
        'type': 'addStream',
        'data': mediaStream.toMap(),
      });

      expect(remoteStreams[0], mediaStream);
      subscription.cancel();
    });

    test('removeStream', () async {
      final peerConnection = RtcPeerConnection(id);
      final remoteStreams = <MediaStream>[];
      final subscription = peerConnection
          .on(RtcPeerConnectionEvent.removeStream)
          .map(((data) => MediaStream.fromMap(data)))
          .listen((stream) => remoteStreams.add(stream));

      final mediaStreamId = randomString();
      final mediaStream = MediaStream(mediaStreamId);
      await _postEvent({
        'type': 'removeStream',
        'data': mediaStream.toMap(),
      });

      expect(remoteStreams[0], mediaStream);
      subscription.cancel();
    });
  });

  test('dispose', () async {
    final peerConnection = RtcPeerConnection(id);

    await peerConnection.dispose();

    expect(methodCalls.toString(), '[MethodCall(dispose, null)]');
  });
}

Future<void> _postEvent(data) async {
  // see https://github.com/flutter/flutter/issues/26528#issuecomment-512896634
  await defaultBinaryMessenger.handlePlatformMessage(
    '$channelName::$id/events',
    StandardMethodCodec().encodeSuccessEnvelope(data),
    (ByteData data) {},
  );
}
