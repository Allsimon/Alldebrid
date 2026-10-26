import 'dart:async';

import 'package:alldebrid/download/download_service.dart';
import 'package:alldebrid/download/downloads_controller.dart';
import 'package:alldebrid/providers.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

class FakeDownloadService extends Fake implements DownloadService {
  final pending = <String, Completer<void>>{};
  final progress = <String, void Function(double)>{};

  @override
  Future<void> download(
    String url,
    String filename, {
    String? taskId,
    void Function(double progress)? onProgress,
  }) {
    progress[filename] = onProgress!;
    return (pending[filename] = Completer<void>()).future;
  }

  @override
  Future<void> cancel(String taskId) async {}
}

void main() {
  late FakeDownloadService service;
  late ProviderContainer container;

  setUp(() {
    service = FakeDownloadService();
    container = ProviderContainer(
      overrides: [downloadServiceProvider.overrideWithValue(service)],
    );
    addTearDown(container.dispose);
  });

  List<DownloadEntry> downloads() => container.read(downloadsProvider);
  DownloadsController controller() =>
      container.read(downloadsProvider.notifier);

  test('tracks progress until done', () async {
    final done = controller().start('https://cdn/a', 'a.zip');
    expect(downloads().single.state, DownloadState.running);

    service.progress['a.zip']!(0.42);
    expect(downloads().single.progress, 0.42);

    service.pending['a.zip']!.complete();
    await done;
    expect(downloads().single.state, DownloadState.done);
  });

  test('keeps the error of a failed download until dismissed', () async {
    final done = controller().start('https://cdn/b', 'b.zip');
    service.pending['b.zip']!.completeError(Exception('HTTP 404'));
    await done;

    final entry = downloads().single;
    expect(entry.state, DownloadState.failed);
    expect(entry.error, contains('HTTP 404'));

    controller().dismiss(entry.id);
    expect(downloads(), isEmpty);
  });

  test('canceled downloads disappear', () async {
    final done = controller().start('https://cdn/c', 'c.zip');
    service.pending['c.zip']!.completeError(const DownloadCanceled());
    await done;

    expect(downloads(), isEmpty);
  });
}
