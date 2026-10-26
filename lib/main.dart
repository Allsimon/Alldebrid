import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'app.dart';
import 'providers.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final (apiKey, preferences) = await (
    readStoredApiKey(const FlutterSecureStorage()),
    SharedPreferences.getInstance(),
  ).wait;
  runApp(
    ProviderScope(
      overrides: [
        initialApiKeyProvider.overrideWithValue(apiKey),
        preferencesProvider.overrideWithValue(preferences),
      ],
      child: const AllDebridApp(),
    ),
  );
}
