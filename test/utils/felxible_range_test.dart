import 'package:flutter_test/flutter_test.dart';
import 'package:webrtc_plugin/src/utils/flexible_range.dart';

void main() {
  group('construction', () {
    test('range', () {
      expect(Range(3, 5).min, 3);
      expect(Range(3, 5).ideal, 5);
      expect(Range(3, 5).max, 5);
    });

    test('exactly', () {
      expect(Exactly(3).min, 3);
      expect(Exactly(3).ideal, 3);
      expect(Exactly(3).max, 3);
    });

    test('max less than min', () {
      expect(() => Range(5, 3), throwsAssertionError);
    });

    test('ideal is not in range', () {
      expect(() => Range(3, 5).ideally(2), throwsAssertionError);
      expect(() => Range(3, 5).ideally(6), throwsAssertionError);
    });

    test('ideal inrange', () {
      expect(Range(3, 5).ideally(3).min, 3);
      expect(Range(3, 5).ideally(3).ideal, 3);
      expect(Range(3, 5).ideally(3).max, 5);

      expect(Range(3, 5).ideally(4).min, 3);
      expect(Range(3, 5).ideally(4).ideal, 4);
      expect(Range(3, 5).ideally(4).max, 5);

      expect(Range(3, 5).ideally(5).min, 3);
      expect(Range(3, 5).ideally(5).ideal, 5);
      expect(Range(3, 5).ideally(5).max, 5);
    });
  });

  group('toString', () {
    test('fixed range', () {
      expect(Range(3, 5).toString(), '[3..5]');
      expect(Exactly(5).toString(), '[5..5]');
    });

    test('flexible range', () {
      expect(Range(3, 5).ideally(4).toString(), '[3..5]{4}');
    });
  });

  test('isExact', () {
    expect(Exactly(5).isExact, true);
    expect(Range(3, 5).isExact, false);
  });

  test('isRange', () {
    expect(Exactly(5).isRange, false);
    expect(Range(3, 5).isRange, true);
  });
}
