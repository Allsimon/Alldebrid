import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../download/downloads_controller.dart';
import '../providers.dart';

/// Open / download / copy buttons for an unlocked direct link.
class FileActions extends ConsumerWidget {
  const FileActions({super.key, required this.url, required this.filename});

  final String url;
  final String filename;

  void _download(WidgetRef ref) {
    if (ref.read(useDownloaderProvider)) {
      ref.read(downloadsProvider.notifier).start(url, filename);
    } else {
      ref.read(downloadServiceProvider).open(url);
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) => Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      IconButton(
        tooltip: 'Copy link',
        icon: const Icon(Icons.copy),
        onPressed: () {
          Clipboard.setData(ClipboardData(text: url));
          ScaffoldMessenger.of(context)
              .showSnackBar(const SnackBar(content: Text('Link copied')));
        },
      ),
      IconButton(
        tooltip: 'Open',
        icon: const Icon(Icons.open_in_new),
        onPressed: () => ref.read(downloadServiceProvider).open(url),
      ),
      IconButton(
        tooltip: 'Download',
        icon: const Icon(Icons.download),
        onPressed: () => _download(ref),
      ),
    ],
  );
}
