import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../format.dart';
import '../widgets/file_actions.dart';
import 'links_controller.dart';

class LinksScreen extends ConsumerStatefulWidget {
  const LinksScreen({super.key});

  @override
  ConsumerState<LinksScreen> createState() => _LinksScreenState();
}

class _LinksScreenState extends ConsumerState<LinksScreen> {
  final _controller = TextEditingController();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _paste() async {
    final data = await Clipboard.getData(Clipboard.kTextPlain);
    if (data?.text case final text?) _controller.text = text;
  }

  void _unlock() {
    final text = _controller.text;
    if (LinksController.parse(text).isEmpty) return;
    _controller.clear();
    FocusScope.of(context).unfocus();
    ref.read(linksProvider.notifier).unlockAll(text);
  }

  @override
  Widget build(BuildContext context) {
    final entries = ref.watch(linksProvider);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Links'),
        actions: [
          if (entries.isNotEmpty)
            IconButton(
              tooltip: 'Clear',
              icon: const Icon(Icons.clear_all),
              onPressed: ref.read(linksProvider.notifier).clear,
            ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                TextField(
                  controller: _controller,
                  minLines: 1,
                  maxLines: 5,
                  keyboardType: TextInputType.url,
                  decoration: InputDecoration(
                    labelText: 'Links to unlock',
                    hintText: 'One link per line',
                    border: const OutlineInputBorder(),
                    suffixIcon: IconButton(
                      tooltip: 'Paste',
                      icon: const Icon(Icons.content_paste),
                      onPressed: _paste,
                    ),
                  ),
                ),
                const SizedBox(height: 8),
                FilledButton.icon(
                  icon: const Icon(Icons.lock_open),
                  label: const Text('Unlock'),
                  onPressed: _unlock,
                ),
              ],
            ),
          ),
          Expanded(
            child: entries.isEmpty
                ? const Center(child: Text('Unlocked links will appear here'))
                : ListView.builder(
                    itemCount: entries.length,
                    itemBuilder: (_, i) => _LinkTile(entries[i]),
                  ),
          ),
        ],
      ),
    );
  }
}

class _LinkTile extends ConsumerWidget {
  const _LinkTile(this.entry);

  final LinkEntry entry;

  @override
  Widget build(BuildContext context, WidgetRef ref) => Dismissible(
    key: ValueKey(entry.id),
    onDismissed: (_) => ref.read(linksProvider.notifier).remove(entry.id),
    child: switch (entry.result) {
      AsyncData(:final value) => ListTile(
        title: Text(
          value.filename,
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
        ),
        subtitle: Text('${value.host} · ${formatBytes(value.size)}'),
        trailing: FileActions(url: value.link, filename: value.filename),
      ),
      AsyncError(:final error) => ListTile(
        leading: Icon(
          Icons.error_outline,
          color: Theme.of(context).colorScheme.error,
        ),
        title: Text(
          entry.original,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
        subtitle: Text('$error'),
      ),
      _ => ListTile(
        leading: const SizedBox.square(
          dimension: 24,
          child: CircularProgressIndicator(strokeWidth: 2),
        ),
        title: Text(
          entry.original,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
        subtitle: const Text('Unlocking…'),
      ),
    },
  );
}
