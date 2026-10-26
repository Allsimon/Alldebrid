import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:receive_sharing_intent/receive_sharing_intent.dart';

import '../links/links_controller.dart';
import '../magnets/magnets_controller.dart';
import '../providers.dart';

/// Routes shared text, `magnet:` links and .torrent files (Android only).
class IncomingIntents extends ConsumerStatefulWidget {
  const IncomingIntents({super.key, required this.router, required this.child});

  final GoRouter router;
  final Widget child;

  @override
  ConsumerState<IncomingIntents> createState() => _IncomingIntentsState();
}

class _IncomingIntentsState extends ConsumerState<IncomingIntents> {
  final _pending = <SharedMediaFile>[];
  StreamSubscription<List<SharedMediaFile>>? _subscription;

  @override
  void initState() {
    super.initState();
    if (!Platform.isAndroid) return;
    final intents = ReceiveSharingIntent.instance;
    _subscription = intents.getMediaStream().listen(_receive);
    intents.getInitialMedia().then((media) {
      _receive(media);
      intents.reset();
    });
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  void _receive(List<SharedMediaFile> media) {
    _pending.addAll(media);
    _drain();
  }

  void _drain() {
    if (ref.read(authProvider) == null) return;
    for (final media in List.of(_pending)) {
      _handle(media);
    }
    _pending.clear();
  }

  Future<void> _handle(SharedMediaFile media) async {
    final client = ref.read(clientProvider);
    final messenger = ScaffoldMessenger.maybeOf(context);
    try {
      if (media.type == SharedMediaType.file) {
        widget.router.go('/magnets');
        final file = File(media.path);
        await client.uploadTorrent(
          file.uri.pathSegments.last,
          await file.readAsBytes(),
        );
      } else if (_magnets(media.path) case final magnets
          when magnets.isNotEmpty) {
        widget.router.go('/magnets');
        await Future.wait(magnets.map(client.uploadMagnet));
      } else {
        widget.router.go('/links');
        await ref.read(linksProvider.notifier).unlockAll(media.path);
        return;
      }
      ref.invalidate(magnetsProvider);
    } catch (e) {
      messenger?.showSnackBar(SnackBar(content: Text('$e')));
    }
  }

  static List<String> _magnets(String text) => [
    for (final token in text.split(RegExp(r'\s+')))
      if (token.startsWith('magnet:')) token,
  ];

  @override
  Widget build(BuildContext context) {
    ref.listen(authProvider, (_, _) => _drain());
    return widget.child;
  }
}
