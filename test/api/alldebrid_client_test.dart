import 'package:alldebrid/api/alldebrid_client.dart';
import 'package:alldebrid/api/errors.dart';
import 'package:alldebrid/api/models.dart';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http_mock_adapter/http_mock_adapter.dart';

Map<String, dynamic> success(Map<String, dynamic> data) => {
  'status': 'success',
  'data': data,
};

Map<String, dynamic> failure(String code) => {
  'status': 'error',
  'error': {'code': code, 'message': 'upstream $code'},
};

void main() {
  late Dio dio;
  late DioAdapter server;
  late AllDebridClient client;
  var authErrors = 0;

  setUp(() {
    dio = Dio();
    server = DioAdapter(dio: dio, matcher: const UrlRequestMatcher());
    authErrors = 0;
    client = AllDebridClient(
      apiKey: () => 'secret',
      onAuthError: () => authErrors++,
      dio: dio,
    );
  });

  test('sends the API key as a bearer token', () async {
    server.onGet('v4/user', (s) {
      s.reply(
        200,
        success({
          'user': {
            'username': 'jane',
            'email': 'jane@example.com',
            'isPremium': true,
            'premiumUntil': 1893456000,
          },
        }),
      );
    });

    String? authorization;
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          authorization = options.headers['Authorization'] as String?;
          handler.next(options);
        },
      ),
    );

    final user = await client.user();

    expect(authorization, 'Bearer secret');
    expect(user.username, 'jane');
    expect(user.isPremium, isTrue);
    expect(user.premiumUntil, DateTime.utc(2030).toLocal());
    expect(user.daysLeft(DateTime.utc(2029, 12, 22)), 10);
  });

  test('premiumUntil of 0 means no expiry date', () async {
    server.onGet('v4/user', (s) {
      s.reply(
        200,
        success({
          'user': {'username': 'free', 'isPremium': false, 'premiumUntil': 0},
        }),
      );
    });

    final user = await client.user();

    expect(user.premiumUntil, isNull);
    expect(user.daysLeft(DateTime.now()), 0);
  });

  test('unlock parses the direct link', () async {
    server.onPost('v4/link/unlock', (s) {
      s.reply(
        200,
        success({
          'link': 'https://cdn.example/f.zip',
          'filename': 'f.zip',
          'host': 'example',
          'filesize': 1048576,
          'id': 'abc',
        }),
      );
    });

    final link = await client.unlock('https://example.com/f');

    expect(link.link, 'https://cdn.example/f.zip');
    expect(link.filename, 'f.zip');
    expect(link.size, 1048576);
    expect(link.delayedId, isNull);
  });

  test('error envelopes become ApiException with a friendly message', () async {
    server.onPost('v4/link/unlock', (s) => s.reply(400, failure('LINK_DOWN')));

    await expectLater(
      client.unlock('https://example.com/f'),
      throwsA(
        isA<ApiException>()
            .having((e) => e.code, 'code', 'LINK_DOWN')
            .having(
              (e) => e.userMessage,
              'userMessage',
              'The file is unavailable on the host.',
            ),
      ),
    );
    expect(authErrors, 0);
  });

  test('unknown error codes fall back to the API message', () {
    expect(
      const ApiException('NEW_CODE', 'Something').userMessage,
      'Something',
    );
  });

  test('auth errors trigger onAuthError', () async {
    server.onGet('v4/user', (s) => s.reply(401, failure('AUTH_BAD_APIKEY')));

    await expectLater(client.user(), throwsA(isA<ApiException>()));
    expect(authErrors, 1);
  });

  test('delayed links are polled until ready', () async {
    var calls = 0;
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) => handler.resolve(
          Response(
            requestOptions: options,
            statusCode: 200,
            data: success(
              ++calls < 3
                  ? {'status': 1}
                  : {'status': 2, 'link': 'https://cdn/x'},
            ),
          ),
        ),
      ),
    );

    final link = await client.waitDelayed(42, interval: Duration.zero);

    expect(link, 'https://cdn/x');
    expect(calls, 3);
  });

  test('checkPin returns the key once activated', () async {
    const pin = Pin(
      pin: 'ABCD',
      check: 'c',
      userUrl: 'u',
      expiresIn: Duration(minutes: 10),
    );
    server.onPost(
      'v4/pin/check',
      (s) => s.reply(200, success({'activated': false})),
    );
    expect(await client.checkPin(pin), isNull);

    server.onPost(
      'v4/pin/check',
      (s) => s.reply(200, success({'activated': true, 'apikey': 'k3y'})),
    );
    expect(await client.checkPin(pin), 'k3y');
  });

  test('magnet status maps codes to states', () async {
    server.onPost('v4.1/magnet/status', (s) {
      s.reply(
        200,
        success({
          'magnets': [
            {
              'id': 1,
              'filename': 'a',
              'size': 100,
              'downloaded': 25,
              'statusCode': 1,
            },
            {'id': 2, 'filename': 'b', 'size': 100, 'statusCode': 4},
            {'id': 3, 'filename': 'c', 'size': 100, 'statusCode': 15},
          ],
        }),
      );
    });

    final magnets = await client.magnets();

    expect(magnets.map((m) => m.state), [
      MagnetState.processing,
      MagnetState.ready,
      MagnetState.error,
    ]);
    expect(magnets.first.progress, 0.25);
  });

  test('magnet upload surfaces per-item errors', () async {
    server.onPost('v4/magnet/upload', (s) {
      s.reply(
        200,
        success({
          'magnets': [
            {
              'magnet': 'magnet:?bad',
              'error': {'code': 'MAGNET_INVALID_URI', 'message': 'bad'},
            },
          ],
        }),
      );
    });

    await expectLater(
      client.uploadMagnet('magnet:?bad'),
      throwsA(
        isA<ApiException>().having((e) => e.code, 'code', 'MAGNET_INVALID_URI'),
      ),
    );
  });

  test('magnet files tree is flattened', () async {
    server.onPost('v4/magnet/files', (s) {
      s.reply(
        200,
        success({
          'magnets': [
            {
              'id': '1',
              'files': [
                {
                  'n': 'Show',
                  'e': [
                    {'n': 'ep1.mkv', 's': 10, 'l': 'https://h/1'},
                    {
                      'n': 'Subs',
                      'e': [
                        {'n': 'en.srt', 's': 1, 'l': 'https://h/2'},
                      ],
                    },
                  ],
                },
                {'n': 'readme.txt', 's': 2, 'l': 'https://h/3'},
              ],
            },
          ],
        }),
      );
    });

    final files = await client.magnetFiles(1);

    expect(files.map((f) => f.path), [
      'Show/ep1.mkv',
      'Show/Subs/en.srt',
      'readme.txt',
    ]);
    expect(files.first.link, 'https://h/1');
  });

  test('network failures become ApiException', () async {
    server.onGet(
      'v4/user',
      (s) => s.throws(
        0,
        DioException.connectionError(
          requestOptions: RequestOptions(),
          reason: 'offline',
        ),
      ),
    );

    await expectLater(
      client.user(),
      throwsA(isA<ApiException>().having((e) => e.code, 'code', 'NETWORK')),
    );
  });
}
