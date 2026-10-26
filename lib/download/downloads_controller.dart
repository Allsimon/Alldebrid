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

  @override
  List<DownloadEntry> build() => const [];

  Future<void> start(String url, String filename) async {
    final id =
        'alldebrid-${DateTime.now().millisecondsSinceEpoch}-${_nextId++}';
    state = [...state, DownloadEntry(id: id, filename: filename)];
    try {
      await ref
          .read(downloadServiceProvider)
          .download(
            url,
            filename,
            taskId: id,
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
    }
  }

  Future<void> cancel(String id) =>
      ref.read(downloadServiceProvider).cancel(id);

  void dismiss(String id) => state = [
    for (final e in state)
      if (e.id != id) e,
  ];

  void _update(String id, DownloadEntry Function(DownloadEntry) change) =>
      state = [for (final e in state) e.id == id ? change(e) : e];
}
