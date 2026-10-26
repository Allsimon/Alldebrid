import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../api/models.dart';
import '../providers.dart';

final userProvider = FutureProvider.autoDispose<User>(
  (ref) => ref.read(clientProvider).user(),
);
final hostsProvider = FutureProvider.autoDispose<List<Host>>(
  (ref) => ref.read(clientProvider).hosts(),
);

class AccountScreen extends ConsumerWidget {
  const AccountScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(userProvider);
    final hosts = ref.watch(hostsProvider);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Account'),
        actions: [
          IconButton(
            tooltip: 'Log out',
            icon: const Icon(Icons.logout),
            onPressed: ref.read(authProvider.notifier).logout,
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => Future.wait([
          ref.refresh(userProvider.future),
          ref.refresh(hostsProvider.future),
        ]),
        child: ListView(
          children: [
            switch (user) {
              AsyncData(value: final user) => _UserCard(user),
              AsyncError(:final error) => ListTile(title: Text('$error')),
              _ => const LinearProgressIndicator(),
            },
            SwitchListTile(
              title: const Text('Download in the app'),
              subtitle: const Text('Otherwise downloads open in the browser'),
              value: ref.watch(useDownloaderProvider),
              onChanged: ref.read(useDownloaderProvider.notifier).set,
            ),
            const Divider(),
            switch (hosts) {
              AsyncData(value: final hosts) => ExpansionTile(
                title: const Text('Hosts'),
                subtitle: Text(
                  '${hosts.where((h) => h.status).length} of ${hosts.length} available',
                ),
                children: [for (final host in hosts) _HostTile(host)],
              ),
              AsyncError(:final error) => ListTile(title: Text('$error')),
              _ => const SizedBox.shrink(),
            },
          ],
        ),
      ),
    );
  }
}

class _UserCard extends StatelessWidget {
  const _UserCard(this.user);

  final User user;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final until = user.premiumUntil;
    return Card(
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(user.username, style: theme.textTheme.headlineSmall),
            Text(user.email, style: theme.textTheme.bodyMedium),
            const SizedBox(height: 12),
            if (user.isPremium && until != null)
              Text(
                'Premium until ${DateFormat.yMMMd().format(until)} '
                '(${user.daysLeft(DateTime.now())} days left)',
              )
            else
              Text(
                'Not premium',
                style: TextStyle(color: theme.colorScheme.error),
              ),
          ],
        ),
      ),
    );
  }
}

class _HostTile extends StatelessWidget {
  const _HostTile(this.host);

  final Host host;

  @override
  Widget build(BuildContext context) => ListTile(
    dense: true,
    leading: Icon(
      host.status ? Icons.check_circle_outline : Icons.cancel_outlined,
      color: host.status
          ? Theme.of(context).colorScheme.primary
          : Theme.of(context).colorScheme.error,
    ),
    title: Text(host.name),
    trailing: Text(host.type),
  );
}
