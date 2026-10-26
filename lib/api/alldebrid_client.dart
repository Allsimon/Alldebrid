import 'package:dio/dio.dart';

import 'errors.dart';
import 'models.dart';

class AllDebridClient {
  AllDebridClient({
    required String? Function() apiKey,
    this.onAuthError,
    Dio? dio,
  }) : _dio = dio ?? Dio() {
    _dio.options
      ..baseUrl = 'https://api.alldebrid.com/'
      ..contentType = Headers.formUrlEncodedContentType
      // Errors come back in the JSON envelope, sometimes with a 4xx.
      ..validateStatus = (status) => status != null && status < 500;
    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          final key = apiKey();
          if (key != null) options.headers['Authorization'] = 'Bearer $key';
          handler.next(options);
        },
      ),
    );
  }

  final Dio _dio;
  final void Function()? onAuthError;

  Future<Json> _call(
    String path, {
    Object? data,
    String method = 'POST',
  }) async {
    final Response<dynamic> response;
    try {
      response = await _dio.request<dynamic>(
        path,
        data: data,
        options: Options(method: method),
      );
    } on DioException catch (e) {
      throw ApiException('NETWORK', e.message ?? 'Network error');
    }
    final body = response.data;
    if (body is! Json) {
      throw ApiException('BAD_RESPONSE', 'HTTP ${response.statusCode}');
    }
    if (body['status'] != 'success') {
      final error = body['error'] as Json? ?? const {};
      final exception = ApiException(
        error['code'] as String? ?? 'GENERIC',
        error['message'] as String? ?? 'Unknown error',
      );
      if (exception.isAuthError) onAuthError?.call();
      throw exception;
    }
    return body['data'] as Json;
  }

  static void _throwItemError(Json item) {
    if (item['error'] case final Json error) {
      throw ApiException(error['code'] as String, error['message'] as String);
    }
  }

  Future<Pin> getPin() async =>
      Pin.fromJson(await _call('v4.1/pin/get', method: 'GET'));

  /// Returns the API key once the user has entered the PIN, `null` until then.
  Future<String?> checkPin(Pin pin) async {
    final data = await _call(
      'v4/pin/check',
      data: {'pin': pin.pin, 'check': pin.check},
    );
    return data['activated'] == true ? data['apikey'] as String : null;
  }

  Future<User> user() async =>
      User.fromJson((await _call('v4/user', method: 'GET'))['user'] as Json);

  Future<List<Host>> hosts() async {
    final hosts = (await _call('v4.1/user/hosts', method: 'GET'))['hosts'];
    final list = hosts is Map ? hosts.values : hosts as List<dynamic>;
    return [for (final h in list) Host.fromJson(h as Json)]
      ..sort((a, b) => a.name.compareTo(b.name));
  }

  Future<UnlockedLink> unlock(String link) async => UnlockedLink.fromJson(
    link,
    await _call('v4/link/unlock', data: {'link': link}),
  );

  /// Polls `link/delayed` until the link is generated.
  Future<String> waitDelayed(
    int id, {
    Duration interval = const Duration(seconds: 5),
  }) async {
    while (true) {
      final data = await _call('v4/link/delayed', data: {'id': id});
      switch (data['status']) {
        case 2:
          return data['link'] as String;
        case 3:
          throw const ApiException('DELAYED_FAILED', 'Link generation failed.');
      }
      await Future<void>.delayed(interval);
    }
  }

  Future<void> uploadMagnet(String magnet) async {
    final data = await _call('v4/magnet/upload', data: {'magnets[]': magnet});
    _throwItemError((data['magnets'] as List<dynamic>).single as Json);
  }

  Future<void> uploadTorrent(String filename, List<int> bytes) async {
    final data = await _call(
      'v4/magnet/upload/file',
      data: FormData.fromMap({
        'files[]': MultipartFile.fromBytes(bytes, filename: filename),
      }),
    );
    _throwItemError((data['files'] as List<dynamic>).single as Json);
  }

  Future<List<Magnet>> magnets() async {
    final data = await _call('v4.1/magnet/status');
    return [
      for (final m in data['magnets'] as List<dynamic>)
        Magnet.fromJson(m as Json),
    ];
  }

  Future<List<MagnetFile>> magnetFiles(int id) async {
    final data = await _call('v4/magnet/files', data: {'id[]': id});
    final entry = (data['magnets'] as List<dynamic>).single as Json;
    _throwItemError(entry);
    return MagnetFile.flatten(entry['files'] as List<dynamic>);
  }

  Future<void> deleteMagnet(int id) =>
      _call('v4/magnet/delete', data: {'id': id});
}
