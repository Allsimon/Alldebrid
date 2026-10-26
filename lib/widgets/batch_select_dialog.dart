import 'package:flutter/material.dart';

import '../format.dart';

class BatchItem<T> {
  const BatchItem({
    required this.label,
    required this.size,
    required this.value,
  });

  final String label;
  final int size;
  final T value;
}

/// Lets the user pick items; returns the checked ones.
Future<List<T>?> showBatchSelectDialog<T>(
  BuildContext context, {
  required String title,
  required List<BatchItem<T>> items,
}) => showDialog<List<T>>(
  context: context,
  builder: (_) => _BatchSelectDialog(title: title, items: items),
);

class _BatchSelectDialog<T> extends StatefulWidget {
  const _BatchSelectDialog({required this.title, required this.items});

  final String title;
  final List<BatchItem<T>> items;

  @override
  State<_BatchSelectDialog<T>> createState() => _BatchSelectDialogState<T>();
}

class _BatchSelectDialogState<T> extends State<_BatchSelectDialog<T>> {
  late final _checked = {...widget.items};

  @override
  Widget build(BuildContext context) {
    final items = widget.items;
    final selected = [
      for (final item in items)
        if (_checked.contains(item)) item,
    ];
    final size = selected.fold(0, (sum, item) => sum + item.size);
    return Dialog(
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 640),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 24, 24, 8),
              child: Text(
                widget.title,
                style: Theme.of(context).textTheme.titleLarge,
              ),
            ),
            CheckboxListTile(
              controlAffinity: ListTileControlAffinity.leading,
              tristate: true,
              value: selected.isEmpty
                  ? false
                  : selected.length == items.length
                  ? true
                  : null,
              title: Text('${selected.length} of ${items.length} selected'),
              onChanged: (_) => setState(() {
                if (selected.length == items.length) {
                  _checked.clear();
                } else {
                  _checked.addAll(items);
                }
              }),
            ),
            const Divider(height: 1),
            Flexible(
              child: ListView(
                shrinkWrap: true,
                children: [
                  for (final item in items)
                    CheckboxListTile(
                      dense: true,
                      controlAffinity: ListTileControlAffinity.leading,
                      value: _checked.contains(item),
                      title: Text(item.label),
                      secondary: Text(formatBytes(item.size)),
                      onChanged: (checked) => setState(
                        () => checked!
                            ? _checked.add(item)
                            : _checked.remove(item),
                      ),
                    ),
                ],
              ),
            ),
            const Divider(height: 1),
            Padding(
              padding: const EdgeInsets.all(16),
              child: OverflowBar(
                alignment: MainAxisAlignment.end,
                spacing: 8,
                children: [
                  TextButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('Cancel'),
                  ),
                  FilledButton.icon(
                    icon: const Icon(Icons.download),
                    label: Text(
                      'Download ${selected.length} (${formatBytes(size)})',
                    ),
                    onPressed: selected.isEmpty
                        ? null
                        : () => Navigator.pop(context, [
                            for (final item in selected) item.value,
                          ]),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
