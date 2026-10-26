import 'dart:async';

import 'package:alldebrid/api/alldebrid_client.dart';
import 'package:alldebrid/api/models.dart';
import 'package:alldebrid/download/download_service.dart';
import 'package:alldebrid/download/downloads_controller.dart';
import 'package:alldebrid/providers.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

class FakeDownloadService extends Fake implements DownloadService {
  final pending = <String, Completer<void>>{};
  final progress = <String, void Function(double)>{};
  final started = <(String url, String directory, String filename)>[];

  @override
  Future<void> download(
    String url,
    String filename, {
    String? taskId,
    String directory = '',
    void Function(double progress)? onProgress,
  }) {
    started.add((url, directory, filename));
    progress[filename] = onProgress!;
    return (pending[filename] = Completer<void>()).future;
  }

  @override
  Future<void> cancel(String taskId) async {}
}

class FakeClient extends Fake implements AllDebridClient {
  final unlocks = <String, Completer<UnlockedLink>>{};

  @override
  Future<UnlockedLink> unlock(String link) =>
      (unlocks[link] = Completer<UnlockedLink>()).future;

  void grant(String link) => unlocks[link]!.complete(
    UnlockedLink(
      original: link,
      link: '$link.direct',
      filename: 'ignored',
      host: 'host',
      size: 1,
    ),
  );
}

void main() {
  late FakeDownloadService service;
  late FakeClient client;
  late ProviderContainer container;

  setUp(() {
    service = FakeDownloadService();
    client = FakeClient();
    container = ProviderContainer(
      overrides: [
        downloadServiceProvider.overrideWithValue(service),
        clientProvider.overrideWithValue(client),
      ],
    );
    addTearDown(container.dispose);
  });

  List<DownloadEntry> downloads() => container.read(downloadsProvider);
  DownloadsController controller() =>
      container.read(downloadsProvider.notifier);

  test('tracks progress until done', () async {
    final done = controller().start('https://cdn/a', 'a.zip');
    await pumpEventQueue();
    expect(downloads().single.state, DownloadState.running);

    service.progress['a.zip']!(0.42);
    expect(downloads().single.progress, 0.42);

    service.pending['a.zip']!.complete();
    await done;
    expect(downloads().single.state, DownloadState.done);
  });

  test('keeps the error of a failed download until dismissed', () async {
    final done = controller().start('https://cdn/b', 'b.zip');
    await pumpEventQueue();
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
    await pumpEventQueue();
    service.pending['c.zip']!.completeError(const DownloadCanceled());
    await done;

    expect(downloads(), isEmpty);
  });

  test('batch unlocks one at a time and keeps folders', () async {
    controller().unlockAndStart('https://h/1', 'Show/ep1.mkv');
    controller().unlockAndStart('https://h/2', 'Show/Subs/en.srt');
    controller().unlockAndStart('https://h/3', 'readme.txt');
    await pumpEventQueue();

    expect(downloads().map((d) => d.filename), [
      'Show/ep1.mkv',
      'Show/Subs/en.srt',
      'readme.txt',
    ]);
    expect(client.unlocks.keys, ['https://h/1']);

    client.grant('https://h/1');
    await pumpEventQueue();
    expect(client.unlocks.keys, ['https://h/1', 'https://h/2']);

    client.unlocks['https://h/2']!.completeError(Exception('LINK_DOWN'));
    await pumpEventQueue();
    client.grant('https://h/3');
    await pumpEventQueue();

    expect(service.started, [
      ('https://h/1.direct', 'Show', 'ep1.mkv'),
      ('https://h/3.direct', '', 'readme.txt'),
    ]);
    expect(downloads()[1].state, DownloadState.failed);
  });

  test('canceling before the unlock skips the download', () async {
    controller().unlockAndStart('https://h/1', 'a.mkv');
    controller().unlockAndStart('https://h/2', 'b.mkv');
    await pumpEventQueue();

    await controller().cancel(downloads()[1].id);
    client.grant('https://h/1');
    await pumpEventQueue();

    expect(client.unlocks.keys, ['https://h/1']);
    expect(downloads().map((d) => d.filename), ['a.mkv']);
  });
}
