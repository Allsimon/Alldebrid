import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../api/alldebrid_client.dart';
import '../providers.dart';

class ApiKeyLoginScreen extends ConsumerStatefulWidget {
  const ApiKeyLoginScreen({super.key});

  @override
  ConsumerState<ApiKeyLoginScreen> createState() => _ApiKeyLoginScreenState();
}

class _ApiKeyLoginScreenState extends ConsumerState<ApiKeyLoginScreen> {
  final _controller = TextEditingController();
  bool _busy = false;
  String? _error;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final key = _controller.text.trim();
    if (key.isEmpty) return;
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await AllDebridClient(apiKey: () => key).user();
      await ref.read(authProvider.notifier).login(key);
    } catch (e) {
      if (mounted) setState(() => _error = '$e');
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('API key')),
    body: Center(
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 420),
        child: ListView(
          shrinkWrap: true,
          padding: const EdgeInsets.all(24),
          children: [
            const Text(
              'Create a key at alldebrid.com/apikeys and paste it here.',
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _controller,
              autofocus: true,
              obscureText: true,
              decoration: InputDecoration(
                labelText: 'API key',
                border: const OutlineInputBorder(),
                errorText: _error,
              ),
              onSubmitted: (_) => _submit(),
            ),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: _busy ? null : _submit,
              child: const Text('Log in'),
            ),
          ],
        ),
      ),
    ),
  );
}
