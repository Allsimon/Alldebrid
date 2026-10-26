import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../api/models.dart';
import '../providers.dart';

class PinLoginScreen extends ConsumerStatefulWidget {
  const PinLoginScreen({
    super.key,
    this.pollInterval = const Duration(seconds: 5),
  });

  final Duration pollInterval;

  @override
  ConsumerState<PinLoginScreen> createState() => _PinLoginScreenState();
}

class _PinLoginScreenState extends ConsumerState<PinLoginScreen> {
  Pin? _pin;
  Object? _error;
  Timer? _timer;
  DateTime? _expiresAt;

  @override
  void initState() {
    super.initState();
    _requestPin();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _requestPin() async {
    _timer?.cancel();
    setState(() {
      _pin = null;
      _error = null;
    });
    try {
      final pin = await ref.read(clientProvider).getPin();
      if (!mounted) return;
      setState(() {
        _pin = pin;
        _expiresAt = DateTime.now().add(pin.expiresIn);
      });
      _timer = Timer.periodic(widget.pollInterval, (_) => _check(pin));
    } catch (e) {
      if (mounted) setState(() => _error = e);
    }
  }

  Future<void> _check(Pin pin) async {
    if (DateTime.now().isAfter(_expiresAt!)) return _requestPin();
    try {
      final key = await ref.read(clientProvider).checkPin(pin);
      if (key != null) {
        _timer?.cancel();
        await ref.read(authProvider.notifier).login(key);
      }
    } catch (e) {
      _timer?.cancel();
      if (mounted) setState(() => _error = e);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final pin = _pin;
    return Scaffold(
      appBar: AppBar(title: const Text('Log in to AllDebrid')),
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 420),
          child: ListView(
            shrinkWrap: true,
            padding: const EdgeInsets.all(24),
            children: [
              if (_error != null) ...[
                Text('$_error', textAlign: TextAlign.center),
                const SizedBox(height: 16),
                FilledButton(
                  onPressed: _requestPin,
                  child: const Text('Retry'),
                ),
              ] else if (pin == null)
                const Center(child: CircularProgressIndicator())
              else ...[
                const Text(
                  'Open the AllDebrid PIN page and enter this code:',
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),
                SelectableText(
                  pin.pin,
                  textAlign: TextAlign.center,
                  style: theme.textTheme.displayMedium?.copyWith(
                    letterSpacing: 8,
                  ),
                ),
                const SizedBox(height: 16),
                FilledButton.icon(
                  icon: const Icon(Icons.open_in_new),
                  label: const Text('Open alldebrid.com'),
                  onPressed: () =>
                      ref.read(downloadServiceProvider).open(pin.userUrl),
                ),
                TextButton.icon(
                  icon: const Icon(Icons.copy),
                  label: const Text('Copy link'),
                  onPressed: () =>
                      Clipboard.setData(ClipboardData(text: pin.userUrl)),
                ),
                const SizedBox(height: 8),
                const LinearProgressIndicator(),
                const SizedBox(height: 8),
                Text(
                  'Waiting for confirmation…',
                  textAlign: TextAlign.center,
                  style: theme.textTheme.bodySmall,
                ),
              ],
              const SizedBox(height: 32),
              OutlinedButton(
                onPressed: () => context.push('/login/key'),
                child: const Text('Use an API key instead'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
