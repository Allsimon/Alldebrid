import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'api/alldebrid_client.dart';
import 'download/download_service.dart';

const _apiKeyStorageKey = 'apikey';
const _useDownloaderKey = 'useDownloader';

final secureStorageProvider = Provider((_) => const FlutterSecureStorage());

/// Overridden in `main` with values loaded before the first frame.
final initialApiKeyProvider = Provider<String?>((_) => null);
final preferencesProvider = Provider<SharedPreferences>(
  (_) => throw UnimplementedError(),
);

final authProvider = NotifierProvider<AuthNotifier, String?>(AuthNotifier.new);

class AuthNotifier extends Notifier<String?> {
  @override
  String? build() => ref.read(initialApiKeyProvider);

  Future<void> login(String apiKey) async {
    await ref
        .read(secureStorageProvider)
        .write(key: _apiKeyStorageKey, value: apiKey);
    state = apiKey;
  }

  Future<void> logout() async {
    await ref.read(secureStorageProvider).delete(key: _apiKeyStorageKey);
    state = null;
  }
}

Future<String?> readStoredApiKey(FlutterSecureStorage storage) =>
    storage.read(key: _apiKeyStorageKey);

final clientProvider = Provider(
  (ref) => AllDebridClient(
    apiKey: () => ref.read(authProvider),
    onAuthError: () => ref.read(authProvider.notifier).logout(),
  ),
);

final downloadServiceProvider = Provider((_) => DownloadService());

final useDownloaderProvider = NotifierProvider<UseDownloaderNotifier, bool>(
  UseDownloaderNotifier.new,
);

class UseDownloaderNotifier extends Notifier<bool> {
  @override
  bool build() =>
      ref.read(preferencesProvider).getBool(_useDownloaderKey) ?? true;

  Future<void> set(bool value) async {
    await ref.read(preferencesProvider).setBool(_useDownloaderKey, value);
    state = value;
  }
}
