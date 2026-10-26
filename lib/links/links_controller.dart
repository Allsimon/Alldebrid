import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../api/models.dart';
import '../providers.dart';

class LinkEntry {
  const LinkEntry(this.id, this.original, this.result);

  final int id;
  final String original;
  final AsyncValue<UnlockedLink> result;
}

final linksProvider = NotifierProvider<LinksController, List<LinkEntry>>(
  LinksController.new,
);

class LinksController extends Notifier<List<LinkEntry>> {
  var _nextId = 0;

  @override
  List<LinkEntry> build() => const [];

  static List<String> parse(String text) => [
    for (final token in text.split(RegExp(r'\s+')))
      if (Uri.tryParse(token)?.scheme case 'http' || 'https') token,
  ];

  Future<void> unlockAll(String text) => Future.wait(parse(text).map(_unlock));

  Future<void> _unlock(String link) async {
    final id = _nextId++;
    state = [LinkEntry(id, link, const AsyncLoading()), ...state];
    final client = ref.read(clientProvider);
    final result = await AsyncValue.guard(() async {
      final unlocked = await client.unlock(link);
      final delayed = unlocked.delayedId;
      return delayed == null
          ? unlocked
          : unlocked.withLink(await client.waitDelayed(delayed));
    });
    if (!ref.mounted) return;
    state = [
      for (final e in state) e.id == id ? LinkEntry(id, link, result) : e,
    ];
  }

  void remove(int id) => state = [
    for (final e in state)
      if (e.id != id) e,
  ];

  void clear() => state = const [];
}
