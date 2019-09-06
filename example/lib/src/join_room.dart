import 'dart:math';

import 'package:flutter/material.dart';
import 'package:webrtc_plugin_example/src/room.dart';

class JoinRoom extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("AppRTC demo"),
      ),
      body: _JoinRoomForm(),
    );
  }
}

class _JoinRoomForm extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _JoinRoomFormState();
}

class _JoinRoomFormState extends State<_JoinRoomForm> {
  static final _random = Random.secure();
  final _formKey = GlobalKey<FormState>();
  final _roomNameController = TextEditingController(text: _getRandomName());

  static String _getRandomName() {
    const alphabet = "0123456789";
    return List.generate(9, (i) => alphabet[_random.nextInt(alphabet.length)])
        .join();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(16.0),
      child: Form(
        key: _formKey,
        child: Column(
          children: <Widget>[
            _buildRoomNameField(context),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                _buildJoinButton(context),
                _buildRandomButton()
              ],
            )
          ],
        ),
      ),
    );
  }

  Widget _buildRoomNameField(BuildContext context) {
    return TextFormField(
      controller: _roomNameController,
      validator: (value) {
        final isValid = value.contains(RegExp(r'^[\w-]{5,}$'));
        return isValid
            ? null
            : 'Room name must be 5 or more characters and include only\n'
            'letters, numbers, underscore and hyphen.';
      },
      onSaved: (value) {
        Navigator.push(
            context, MaterialPageRoute(builder: (_) => Room(value)));
      },
    );
  }

  Widget _buildJoinButton(BuildContext context) {
    return Container(
      padding: EdgeInsets.fromLTRB(4.0, 0, 4.0, 0),
      child: RaisedButton(
        onPressed: () {
          final formState = _formKey.currentState;
          if (formState.validate()) {
            formState.save();
          }
        },
        child: Text("Join"),
      ),
    );
  }

  Widget _buildRandomButton() {
    return Container(
      padding: EdgeInsets.fromLTRB(4.0, 0, 4.0, 0),
      child: RaisedButton(
        onPressed: () {
          setState(() {
            _roomNameController.text = _getRandomName();
          });
        },
        child: Text("Random"),
      ),
    );
  }
}
