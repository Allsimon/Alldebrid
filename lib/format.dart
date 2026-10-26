import 'package:intl/intl.dart';

final _number = NumberFormat('0.#');

String formatBytes(int bytes) {
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  var value = bytes.toDouble();
  var unit = 0;
  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024;
    unit++;
  }
  return '${_number.format(value)} ${units[unit]}';
}
