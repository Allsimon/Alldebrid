import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../api/models.dart';
import '../format.dart';
import '../providers.dart';
import '../widgets/file_actions.dart';
import 'magnets_controller.dart';

class MagnetsScreen extends ConsumerWidget {
  const MagnetsScreen({super.key});

  Future<void> _run(
    BuildContext context,
    Future<void> Function() action,
  ) async {
    final messenger = ScaffoldMessenger.of(context);
    try {
      await action();
    } catch (e) {
      messenger.showSnackBar(SnackBar(content: Text('$e')));
    }
  }

  Future<void> _addMagnet(BuildContext context, WidgetRef ref) async {
    final controller = TextEditingController();
    final magnet = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Add magnet'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(hintText: 'magnet:?xt=urn:btih:…'),
          onSubmitted: (value) => Navigator.pop(context, value),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, controller.text),
            child: const Text('Add'),
          ),
        ],
      ),
    );
    controller.dispose();
    if (magnet == null || magnet.trim().isEmpty || !context.mounted) return;
    await _run(
      context,
      () => ref.read(magnetsProvider.notifier).addMagnet(magnet.trim()),
    );
  }

  Future<void> _addTorrent(BuildContext context, WidgetRef ref) async {
    final picked = await FilePicker.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['torrent'],
    );
    if (picked.isEmpty || !context.mounted) return;
    final file = picked.single;
    final bytes = await file.readAsBytes();
    if (!context.mounted) return;
    await _run(
      context,
      () => ref.read(magnetsProvider.notifier).addTorrent(file.name, bytes),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final magnets = ref.watch(magnetsProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Magnets')),
      floatingActionButton: MenuAnchor(
        menuChildren: [
          MenuItemButton(
            leadingIcon: const Icon(Icons.link),
            onPressed: () => _addMagnet(context, ref),
            child: const Text('Magnet link'),
          ),
          MenuItemButton(
            leadingIcon: const Icon(Icons.file_open),
            onPressed: () => _addTorrent(context, ref),
            child: const Text('Torrent file'),
          ),
        ],
        builder: (_, menu, _) => FloatingActionButton(
          tooltip: 'Add',
          onPressed: () => menu.isOpen ? menu.close() : menu.open(),
          child: const Icon(Icons.add),
        ),
      ),
      body: switch (magnets) {
        AsyncValue(value: final list?) when list.isEmpty => const Center(
          child: Text('No magnets yet'),
        ),
        AsyncValue(value: final list?) => RefreshIndicator(
          onRefresh: ref.read(magnetsProvider.notifier).refresh,
          child: ListView.builder(
            padding: const EdgeInsets.only(bottom: 88),
            itemCount: list.length,
            itemBuilder: (_, i) => _MagnetTile(
              list[i],
              onDelete: () => _run(
                context,
                () => ref.read(magnetsProvider.notifier).delete(list[i].id),
              ),
            ),
          ),
        ),
        AsyncError(:final error) => Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('$error'),
              TextButton(
                onPressed: () => ref.invalidate(magnetsProvider),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
        _ => const Center(child: CircularProgressIndicator()),
      },
    );
  }
}

class _MagnetTile extends StatelessWidget {
  const _MagnetTile(this.magnet, {required this.onDelete});

  final Magnet magnet;
  final VoidCallback onDelete;

  String get _details => switch (magnet.state) {
    MagnetState.processing => [
      magnet.status,
      '${formatBytes(magnet.downloaded)} / ${formatBytes(magnet.size)}',
      if (magnet.downloadSpeed > 0) '${formatBytes(magnet.downloadSpeed)}/s',
      '${magnet.seeders} seeders',
    ].join(' · '),
    _ => '${magnet.status} · ${formatBytes(magnet.size)}',
  };

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return ListTile(
      leading: switch (magnet.state) {
        MagnetState.ready => Icon(Icons.check_circle, color: colors.primary),
        MagnetState.error => Icon(Icons.error, color: colors.error),
        MagnetState.processing => SizedBox.square(
          dimension: 24,
          child: CircularProgressIndicator(
            value: magnet.progress > 0 ? magnet.progress : null,
            strokeWidth: 3,
          ),
        ),
      },
      title: Text(
        magnet.filename,
        maxLines: 2,
        overflow: TextOverflow.ellipsis,
      ),
      subtitle: Text(_details),
      trailing: IconButton(
        tooltip: 'Delete',
        icon: const Icon(Icons.delete_outline),
        onPressed: onDelete,
      ),
      onTap: magnet.state == MagnetState.ready
          ? () => showModalBottomSheet<void>(
              context: context,
              isScrollControlled: true,
              showDragHandle: true,
              builder: (_) => _MagnetFilesSheet(magnet),
            )
          : null,
    );
  }
}

class _MagnetFilesSheet extends ConsumerWidget {
  const _MagnetFilesSheet(this.magnet);

  final Magnet magnet;

  @override
  Widget build(BuildContext context, WidgetRef ref) => DraggableScrollableSheet(
    expand: false,
    builder: (_, scroll) => switch (ref.watch(magnetFilesProvider(magnet.id))) {
      AsyncData(value: final files) => ListView(
        controller: scroll,
        children: [
          for (final file in files)
            ListTile(
              title: Text(file.path),
              subtitle: Text(formatBytes(file.size)),
              trailing: _UnlockFileButton(file),
            ),
        ],
      ),
      AsyncError(:final error) => Center(child: Text('$error')),
      _ => const Center(child: CircularProgressIndicator()),
    },
  );
}

/// Magnet file links are hoster links that still need unlocking.
class _UnlockFileButton extends ConsumerStatefulWidget {
  const _UnlockFileButton(this.file);

  final MagnetFile file;

  @override
  ConsumerState<_UnlockFileButton> createState() => _UnlockFileButtonState();
}

class _UnlockFileButtonState extends ConsumerState<_UnlockFileButton> {
  Future<UnlockedLink>? _unlocked;

  @override
  Widget build(BuildContext context) => FutureBuilder(
    future: _unlocked,
    builder: (context, snapshot) => switch (snapshot) {
      AsyncSnapshot(connectionState: ConnectionState.none) => IconButton(
        tooltip: 'Unlock',
        icon: const Icon(Icons.lock_open),
        onPressed: () => setState(() {
          _unlocked = ref.read(clientProvider).unlock(widget.file.link);
        }),
      ),
      AsyncSnapshot(hasData: true, :final data) => FileActions(
        url: data!.link,
        filename: data.filename,
      ),
      AsyncSnapshot(hasError: true, :final error) => Tooltip(
        message: '$error',
        child: Icon(
          Icons.error_outline,
          color: Theme.of(context).colorScheme.error,
        ),
      ),
      _ => const SizedBox.square(
        dimension: 24,
        child: CircularProgressIndicator(strokeWidth: 2),
      ),
    },
  );
}
