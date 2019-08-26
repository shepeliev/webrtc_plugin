class FlexibleRange<T extends num> {
  final T min;
  final T ideal;
  final T max;

  const FlexibleRange(this.min, this.ideal, this.max)
      : assert(max >= min),
        assert(ideal >= min && ideal <= max);

  bool get isExact => min == max;

  bool get isRange => min != max;

  FlexibleRange ideally(int ideal) {
    assert(min <= ideal && ideal <= max);
    return FlexibleRange(min, ideal, max);
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is FlexibleRange &&
          runtimeType == other.runtimeType &&
          min == other.min &&
          ideal == other.ideal &&
          max == other.max;

  @override
  int get hashCode => min.hashCode ^ ideal.hashCode ^ max.hashCode;

  @override
  String toString() {
    if (ideal == max) return '[$min..$max]';
    return '[$min..$max]{$ideal}';
  }

  Map<String, dynamic> toMap() => {'min': min, 'ideal': ideal, 'max': max};
}

class Exactly extends FlexibleRange {
  const Exactly(int val) : super(val, val, val);
}

class Range extends FlexibleRange {
  const Range(int min, int max) : super(min, max, max);
}
