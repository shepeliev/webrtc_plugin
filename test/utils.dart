import 'dart:math';

const _alphabet =
    'abcdefghijklmnopqrstuvwxyz0123456789_ABCDEFGHIJKLMNOPQRSTUVWXYZ';

final _rnd = Random.secure();

String randomString({int length: 20}) => List(length)
    .map((dynamic el) => _rnd.nextInt(_alphabet.length))
    .map((int i) => _alphabet[i])
    .join();

int randomInt({int max: 10000000}) => _rnd.nextInt(max);
