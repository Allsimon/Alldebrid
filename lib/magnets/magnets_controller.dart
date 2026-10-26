import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../api/models.dart';
import '../providers.dart';

const _pollInterval = Duration(seconds: 5);

final magnetsProvider =
    AsyncNotifierProvider.autoDispose<MagnetsController, List<Magnet>>(
      MagnetsController.new,
    );

class MagnetsController extends AsyncNotifier<List<Magnet>> {
  @override
  Future<List<Magnet>> build() async {
    final timer = Timer.periodic(_pollInterval, (_) => refresh());
    ref.onDispose(timer.cancel);
    return ref.read(clientProvider).magnets();
  }

  Future<void> refresh() async {
    final result = await AsyncValue.guard(ref.read(clientProvider).magnets);
    if (ref.mounted) state = result;
  }

  Future<void> addMagnet(String magnet) async {
    await ref.read(clientProvider).uploadMagnet(magnet);
    await refresh();
  }

  Future<void> addTorrent(String filename, List<int> bytes) async {
    await ref.read(clientProvider).uploadTorrent(filename, bytes);
    await refresh();
  }

  Future<void> delete(int id) async {
    await ref.read(clientProvider).deleteMagnet(id);
    await refresh();
  }
}

final magnetFilesProvider = FutureProvider.autoDispose
    .family<List<MagnetFile>, int>(
      (ref, id) => ref.read(clientProvider).magnetFiles(id),
    );
