import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'downloads_controller.dart';

class DownloadsPanel extends ConsumerWidget {
  const DownloadsPanel({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final downloads = ref.watch(downloadsProvider);
    if (downloads.isEmpty) return const SizedBox.shrink();
    return Material(
      color: Theme.of(context).colorScheme.surfaceContainer,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxHeight: 200),
        child: ListView(
          shrinkWrap: true,
          children: [for (final d in downloads) _DownloadTile(d)],
        ),
      ),
    );
  }
}

class _DownloadTile extends ConsumerWidget {
  const _DownloadTile(this.download);

  final DownloadEntry download;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = Theme.of(context).colorScheme;
    final controller = ref.read(downloadsProvider.notifier);
    return ListTile(
      dense: true,
      leading: switch (download.state) {
        DownloadState.running => const Icon(Icons.downloading),
        DownloadState.done => Icon(Icons.check_circle, color: colors.primary),
        DownloadState.failed => Icon(Icons.error, color: colors.error),
      },
      title: Text(
        download.filename,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      subtitle: switch (download.state) {
        DownloadState.running => Padding(
          padding: const EdgeInsets.only(top: 4),
          child: Row(
            children: [
              Expanded(
                child: LinearProgressIndicator(
                  value: download.progress > 0 ? download.progress : null,
                ),
              ),
              const SizedBox(width: 8),
              Text('${(download.progress * 100).floor()} %'),
            ],
          ),
        ),
        DownloadState.done => const Text('Saved to Downloads'),
        DownloadState.failed => Text(
          download.error ?? 'Download failed',
          style: TextStyle(color: colors.error),
        ),
      },
      trailing: download.state == DownloadState.running
          ? IconButton(
              tooltip: 'Cancel',
              icon: const Icon(Icons.close),
              onPressed: () => controller.cancel(download.id),
            )
          : IconButton(
              tooltip: 'Dismiss',
              icon: const Icon(Icons.clear),
              onPressed: () => controller.dismiss(download.id),
            ),
    );
  }
}
