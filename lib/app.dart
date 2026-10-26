import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'account/account_screen.dart';
import 'auth/apikey_login_screen.dart';
import 'auth/pin_login_screen.dart';
import 'intents/incoming_intents.dart';
import 'links/links_screen.dart';
import 'magnets/magnets_screen.dart';
import 'providers.dart';

// brand_primary from the legacy app's colors.xml
const _seed = Color(0xFFFAC233);

const _tabs = [
  (path: '/links', icon: Icons.link, label: 'Links'),
  (path: '/magnets', icon: Icons.downloading, label: 'Magnets'),
  (path: '/account', icon: Icons.person, label: 'Account'),
];

final routerProvider = Provider((ref) {
  final loggedIn = ValueNotifier(ref.read(authProvider) != null);
  ref
    ..listen(authProvider, (_, key) => loggedIn.value = key != null)
    ..onDispose(loggedIn.dispose);
  return GoRouter(
    initialLocation: '/links',
    refreshListenable: loggedIn,
    redirect: (_, state) {
      final onLogin = state.matchedLocation.startsWith('/login');
      if (!loggedIn.value) return onLogin ? null : '/login';
      return onLogin ? '/links' : null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (_, _) => const PinLoginScreen(),
        routes: [
          GoRoute(path: 'key', builder: (_, _) => const ApiKeyLoginScreen()),
        ],
      ),
      ShellRoute(
        builder: (context, state, child) =>
            _Shell(location: state.matchedLocation, child: child),
        routes: [
          GoRoute(path: '/links', builder: (_, _) => const LinksScreen()),
          GoRoute(path: '/magnets', builder: (_, _) => const MagnetsScreen()),
          GoRoute(path: '/account', builder: (_, _) => const AccountScreen()),
        ],
      ),
    ],
  );
});

class AllDebridApp extends ConsumerWidget {
  const AllDebridApp({super.key});

  ThemeData _theme(Brightness brightness) => ThemeData(
    colorScheme: ColorScheme.fromSeed(seedColor: _seed, brightness: brightness),
  );

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);
    return MaterialApp.router(
      title: 'AllDebrid',
      theme: _theme(Brightness.light),
      darkTheme: _theme(Brightness.dark),
      routerConfig: router,
      builder: (_, child) => IncomingIntents(router: router, child: child!),
    );
  }
}

class _Shell extends StatelessWidget {
  const _Shell({required this.location, required this.child});

  final String location;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final index = _tabs.indexWhere((t) => location.startsWith(t.path));
    void select(int i) => context.go(_tabs[i].path);
    final wide = MediaQuery.sizeOf(context).width >= 720;
    if (wide) {
      return Scaffold(
        body: Row(
          children: [
            NavigationRail(
              selectedIndex: index,
              onDestinationSelected: select,
              labelType: NavigationRailLabelType.all,
              destinations: [
                for (final t in _tabs)
                  NavigationRailDestination(
                    icon: Icon(t.icon),
                    label: Text(t.label),
                  ),
              ],
            ),
            const VerticalDivider(width: 1),
            Expanded(child: child),
          ],
        ),
      );
    }
    return Scaffold(
      body: child,
      bottomNavigationBar: NavigationBar(
        selectedIndex: index,
        onDestinationSelected: select,
        destinations: [
          for (final t in _tabs)
            NavigationDestination(icon: Icon(t.icon), label: t.label),
        ],
      ),
    );
  }
}
