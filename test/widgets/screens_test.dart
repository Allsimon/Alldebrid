import 'dart:async';

import 'package:alldebrid/api/alldebrid_client.dart';
import 'package:alldebrid/api/errors.dart';
import 'package:alldebrid/api/models.dart';
import 'package:alldebrid/auth/pin_login_screen.dart';
import 'package:alldebrid/links/links_screen.dart';
import 'package:alldebrid/magnets/magnets_screen.dart';
import 'package:alldebrid/providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

class FakeClient extends Fake implements AllDebridClient {
  final unlocks = <String, Completer<UnlockedLink>>{};
  var pinChecks = 0;

  @override
  Future<UnlockedLink> unlock(String link) =>
      (unlocks[link] = Completer<UnlockedLink>()).future;

  @override
  Future<Pin> getPin() async => const Pin(
    pin: 'W8XZ',
    check: 'check',
    userUrl: 'https://alldebrid.com/pin/?pin=W8XZ',
    expiresIn: Duration(minutes: 10),
  );

  @override
  Future<String?> checkPin(Pin pin) async => ++pinChecks < 2 ? null : 'k3y';

  @override
  Future<List<Magnet>> magnets() async => const [
    Magnet(
      id: 7,
      filename: 'show',
      size: 10,
      status: 'Ready',
      statusCode: 4,
      downloaded: 10,
      seeders: 0,
      downloadSpeed: 0,
    ),
  ];

  @override
  Future<List<MagnetFile>> magnetFiles(int id) async => const [
    MagnetFile(
      path: 'show/ep1.mkv',
      size: 10,
      link: 'https://host.example/ep1',
    ),
  ];
}

Future<ProviderContainer> pump(
  WidgetTester tester,
  FakeClient client,
  Widget screen,
) async {
  SharedPreferences.setMockInitialValues({});
  FlutterSecureStorage.setMockInitialValues({});
  final container = ProviderContainer(
    overrides: [
      clientProvider.overrideWithValue(client),
      preferencesProvider.overrideWithValue(
        await SharedPreferences.getInstance(),
      ),
    ],
  );
  addTearDown(container.dispose);
  await tester.pumpWidget(
    UncontrolledProviderScope(
      container: container,
      child: MaterialApp(home: screen),
    ),
  );
  return container;
}

void main() {
  testWidgets('links screen shows loading, success and error', (tester) async {
    final client = FakeClient();
    await pump(tester, client, const LinksScreen());
    expect(find.text('Unlocked links will appear here'), findsOneWidget);

    await tester.enterText(
      find.byType(TextField),
      'https://host.example/ok\nnot a link https://host.example/down',
    );
    await tester.tap(find.text('Unlock'));
    await tester.pump();

    expect(client.unlocks.keys, [
      'https://host.example/ok',
      'https://host.example/down',
    ]);
    expect(find.text('Unlocking…'), findsNWidgets(2));

    client.unlocks['https://host.example/ok']!.complete(
      const UnlockedLink(
        original: 'https://host.example/ok',
        link: 'https://cdn.example/ok.zip',
        filename: 'ok.zip',
        host: 'example',
        size: 2048,
      ),
    );
    client.unlocks['https://host.example/down']!.completeError(
      const ApiException('LINK_DOWN', 'down'),
    );
    await tester.pump();

    expect(find.text('ok.zip'), findsOneWidget);
    expect(find.text('example · 2 KB'), findsOneWidget);
    expect(find.text('The file is unavailable on the host.'), findsOneWidget);
    expect(find.text('Unlocking…'), findsNothing);
  });

  testWidgets('PIN screen polls until the key is granted', (tester) async {
    final client = FakeClient();
    final container = await pump(
      tester,
      client,
      const PinLoginScreen(pollInterval: Duration(seconds: 1)),
    );
    await tester.pump();
    expect(find.text('W8XZ'), findsOneWidget);
    expect(container.read(authProvider), isNull);

    await tester.pump(const Duration(seconds: 1));
    expect(container.read(authProvider), isNull);

    await tester.pump(const Duration(seconds: 1));
    await tester.pump();
    expect(client.pinChecks, 2);
    expect(container.read(authProvider), 'k3y');
  });

  testWidgets('magnet files unlock on tap', (tester) async {
    final client = FakeClient();
    final container = await pump(tester, client, const MagnetsScreen());
    await tester.pumpAndSettle();

    await tester.tap(find.text('show'));
    await tester.pumpAndSettle();
    expect(find.text('show/ep1.mkv'), findsOneWidget);
    expect(client.unlocks, isEmpty);

    await tester.tap(find.byTooltip('Unlock'));
    await tester.pump();
    expect(tester.takeException(), isNull);
    expect(client.unlocks.keys, ['https://host.example/ep1']);

    client.unlocks['https://host.example/ep1']!.complete(
      const UnlockedLink(
        original: 'https://host.example/ep1',
        link: 'https://cdn.example/ep1.mkv',
        filename: 'ep1.mkv',
        host: 'example',
        size: 10,
      ),
    );
    await tester.pump();
    expect(find.byTooltip('Download'), findsOneWidget);

    await tester.pumpWidget(const SizedBox());
    container.dispose();
  });
}
