typedef Json = Map<String, dynamic>;

DateTime? _epoch(Object? seconds) => switch (seconds) {
  final int s when s > 0 => DateTime.fromMillisecondsSinceEpoch(s * 1000),
  _ => null,
};

class User {
  const User({
    required this.username,
    required this.email,
    required this.isPremium,
    required this.premiumUntil,
  });

  factory User.fromJson(Json json) => User(
    username: json['username'] as String,
    email: json['email'] as String? ?? '',
    isPremium: json['isPremium'] as bool? ?? false,
    premiumUntil: _epoch(json['premiumUntil']),
  );

  final String username;
  final String email;
  final bool isPremium;
  final DateTime? premiumUntil;

  int daysLeft(DateTime now) =>
      premiumUntil == null ? 0 : premiumUntil!.difference(now).inDays;
}

class Pin {
  const Pin({
    required this.pin,
    required this.check,
    required this.userUrl,
    required this.expiresIn,
  });

  factory Pin.fromJson(Json json) => Pin(
    pin: json['pin'] as String,
    check: json['check'] as String,
    userUrl: json['user_url'] as String,
    expiresIn: Duration(seconds: json['expires_in'] as int),
  );

  final String pin;
  final String check;
  final String userUrl;
  final Duration expiresIn;
}

class UnlockedLink {
  const UnlockedLink({
    required this.original,
    required this.link,
    required this.filename,
    required this.host,
    required this.size,
    this.delayedId,
  });

  factory UnlockedLink.fromJson(String original, Json json) => UnlockedLink(
    original: original,
    link: json['link'] as String? ?? '',
    filename: json['filename'] as String? ?? original,
    host: json['host'] as String? ?? '',
    size: json['filesize'] as int? ?? 0,
    delayedId: json['delayed'] as int?,
  );

  final String original;
  final String link;
  final String filename;
  final String host;
  final int size;
  final int? delayedId;

  UnlockedLink withLink(String link) => UnlockedLink(
    original: original,
    link: link,
    filename: filename,
    host: host,
    size: size,
  );
}

enum MagnetState { processing, ready, error }

class Magnet {
  const Magnet({
    required this.id,
    required this.filename,
    required this.size,
    required this.status,
    required this.statusCode,
    required this.downloaded,
    required this.seeders,
    required this.downloadSpeed,
  });

  factory Magnet.fromJson(Json json) => Magnet(
    id: json['id'] as int,
    filename: json['filename'] as String? ?? '',
    size: json['size'] as int? ?? 0,
    status: json['status'] as String? ?? '',
    statusCode: json['statusCode'] as int? ?? 0,
    downloaded: json['downloaded'] as int? ?? 0,
    seeders: json['seeders'] as int? ?? 0,
    downloadSpeed: json['downloadSpeed'] as int? ?? 0,
  );

  final int id;
  final String filename;
  final int size;
  final String status;
  final int statusCode;
  final int downloaded;
  final int seeders;
  final int downloadSpeed;

  // Status code table: https://docs.alldebrid.com/ (magnet/status)
  MagnetState get state => switch (statusCode) {
    < 4 => MagnetState.processing,
    4 => MagnetState.ready,
    _ => MagnetState.error,
  };

  double get progress => size == 0 ? 0 : (downloaded / size).clamp(0, 1);
}

class MagnetFile {
  const MagnetFile({
    required this.path,
    required this.size,
    required this.link,
  });

  final String path;
  final int size;
  final String link;

  /// Flattens the `n`/`e`/`s`/`l` tree returned by `magnet/files`.
  static List<MagnetFile> flatten(List<dynamic> nodes, [String prefix = '']) =>
      [
        for (final node in nodes.cast<Json>())
          if (node['e'] case final List<dynamic> children)
            ...flatten(children, '$prefix${node['n']}/')
          else
            MagnetFile(
              path: '$prefix${node['n']}',
              size: node['s'] as int? ?? 0,
              link: node['l'] as String? ?? '',
            ),
      ];
}

class Host {
  const Host({required this.name, required this.type, required this.status});

  factory Host.fromJson(Json json) => Host(
    name: json['name'] as String,
    type: json['type'] as String? ?? '',
    status: json['status'] as bool? ?? true,
  );

  final String name;
  final String type;
  final bool status;
}
