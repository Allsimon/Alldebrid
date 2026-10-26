import 'dart:io';

import 'package:background_downloader/background_downloader.dart';
import 'package:path_provider/path_provider.dart';
import 'package:url_launcher/url_launcher.dart';

class DownloadService {
  var _initialized = false;

  Future<void> open(String url) async {
    if (!await launchUrl(
      Uri.parse(url),
      mode: LaunchMode.externalApplication,
    )) {
      throw Exception('Could not open $url');
    }
  }

  Future<void> _initialize() async {
    if (_initialized) return;
    // On Linux, path_provider resolves user directories through `xdg-user-dir`;
    // without it background_downloader fails with an unhandled exception.
    if (Platform.isLinux && await getDownloadsDirectory() == null) {
      throw Exception(
        'Could not find the Downloads folder: install xdg-user-dirs',
      );
    }
    FileDownloader().configureNotification(
      running: const TaskNotification('Downloading', '{filename}'),
      complete: const TaskNotification('Downloaded', '{filename}'),
      error: const TaskNotification('Download failed', '{filename}'),
      progressBar: true,
    );
    if (Platform.isAndroid) {
      await FileDownloader().permissions.request(PermissionType.notifications);
    }
    _initialized = true;
  }

  /// Downloads to the system Downloads folder; completes when the file is there.
  Future<void> download(
    String url,
    String filename, {
    String? taskId,
    void Function(double progress)? onProgress,
  }) async {
    await _initialize();
    final task = DownloadTask(
      taskId: taskId,
      url: url,
      filename: filename,
      updates: Updates.statusAndProgress,
      retries: 2,
      allowPause: true,
    );
    final result = await FileDownloader().download(
      task,
      onProgress: (p) {
        if (p >= 0) onProgress?.call(p);
      },
    );
    if (result.status == TaskStatus.canceled) throw const DownloadCanceled();
    if (result.status != TaskStatus.complete) {
      throw Exception(
        result.exception?.description ?? 'Download ${result.status.name}',
      );
    }
    if (await FileDownloader().moveToSharedStorage(
          task,
          SharedStorage.downloads,
        ) ==
        null) {
      throw Exception('Could not move $filename to Downloads');
    }
  }

  Future<void> cancel(String taskId) =>
      FileDownloader().cancelTaskWithId(taskId);
}

class DownloadCanceled implements Exception {
  const DownloadCanceled();
}
