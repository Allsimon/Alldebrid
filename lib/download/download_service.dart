import 'dart:io';

import 'package:background_downloader/background_downloader.dart';
import 'package:url_launcher/url_launcher.dart';

class DownloadService {
  DownloadService() {
    FileDownloader().configureNotification(
      running: const TaskNotification('Downloading', '{filename}'),
      complete: const TaskNotification('Downloaded', '{filename}'),
      error: const TaskNotification('Download failed', '{filename}'),
      progressBar: true,
    );
  }

  Future<void> open(String url) async {
    if (!await launchUrl(
      Uri.parse(url),
      mode: LaunchMode.externalApplication,
    )) {
      throw Exception('Could not open $url');
    }
  }

  /// Downloads to the system Downloads folder; completes when the file is there.
  Future<void> download(String url, String filename) async {
    if (Platform.isAndroid) {
      await FileDownloader().permissions.request(PermissionType.notifications);
    }
    final task = DownloadTask(
      url: url,
      filename: filename,
      updates: Updates.statusAndProgress,
      retries: 2,
      allowPause: true,
    );
    final result = await FileDownloader().download(task);
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
}
