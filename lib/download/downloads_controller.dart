import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers.dart';
import 'download_service.dart';

enum DownloadState { running, done, failed }

class DownloadEntry {
  const DownloadEntry({
    required this.id,
    required this.filename,
    this.state = DownloadState.running,
    this.progress = 0,
    this.error,
  });

  final String id;
  final String filename;
  final DownloadState state;
  final double progress;
  final String? error;

  DownloadEntry copyWith({
    DownloadState? state,
    double? progress,
    String? error,
  }) => DownloadEntry(
    id: id,
    filename: filename,
    state: state ?? this.state,
    progress: progress ?? this.progress,
    error: error ?? this.error,
  );
}

final downloadsProvider =
    NotifierProvider<DownloadsController, List<DownloadEntry>>(
      DownloadsController.new,
    );

class DownloadsController extends Notifier<List<DownloadEntry>> {
  var _nextId = 0;
  final _canceled = <String>{};
  // Unlocks are serialized to stay under the API rate limit.
  Future<void> _unlocks = Future.value();

  @override
  List<DownloadEntry> build() => const [];

  Future<void> start(String url, String filename) =>
      _run(filename, (id) async => (url, filename, ''));

  /// Unlocks a hoster link (e.g. a magnet file) and downloads it to
  /// Downloads/[path], keeping its folders.
  Future<void> unlockAndStart(String link, String path) {
    final slash = path.lastIndexOf('/');
    final directory = slash < 0 ? '' : path.substring(0, slash);
    final filename = path.substring(slash + 1);
    return _run(path, (id) async {
      final url = _unlocks.then((_) async {
        if (_canceled.contains(id)) return '';
        final unlocked = await ref.read(clientProvider).unlock(link);
        final delayed = unlocked.delayedId;
        return delayed == null
            ? unlocked.link
            : await ref.read(clientProvider).waitDelayed(delayed);
      });
      _unlocks = url.then((_) {}, onError: (_) {});
      return (await url, filename, directory);
    });
  }

  Future<void> _run(
    String label,
    Future<(String url, String filename, String directory)> Function(String id)
    resolve,
  ) async {
    final id =
        'alldebrid-${DateTime.now().millisecondsSinceEpoch}-${_nextId++}';
    state = [...state, DownloadEntry(id: id, filename: label)];
    try {
      final (url, filename, directory) = await resolve(id);
      if (_canceled.contains(id)) throw const DownloadCanceled();
      await ref
          .read(downloadServiceProvider)
          .download(
            url,
            filename,
            taskId: id,
            directory: directory,
            onProgress: (p) => _update(id, (e) => e.copyWith(progress: p)),
          );
      _update(id, (e) => e.copyWith(state: DownloadState.done, progress: 1));
    } on DownloadCanceled {
      dismiss(id);
    } catch (e) {
      _update(
        id,
        (entry) => entry.copyWith(
          state: DownloadState.failed,
          error: '$e'.replaceFirst('Exception: ', ''),
        ),
      );
    } finally {
      _canceled.remove(id);
    }
  }

  Future<void> cancel(String id) {
    _canceled.add(id);
    return ref.read(downloadServiceProvider).cancel(id);
  }

  void dismiss(String id) => state = [
    for (final e in state)
      if (e.id != id) e,
  ];

  void _update(String id, DownloadEntry Function(DownloadEntry) change) =>
      state = [for (final e in state) e.id == id ? change(e) : e];
}
