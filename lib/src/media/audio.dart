import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';

@immutable
class Audio extends Equatable {
  static const Audio enabled = Audio();
  static const Audio disabled = null;

  @override
  List<Object> get props => [this];

  const Audio();

  Map<String, dynamic> toMap() => {};
}
