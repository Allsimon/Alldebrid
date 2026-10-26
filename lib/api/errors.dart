class ApiException implements Exception {
  const ApiException(this.code, this.message);

  final String code;
  final String message;

  static const _authCodes = {
    'AUTH_MISSING_APIKEY',
    'AUTH_BAD_APIKEY',
    'AUTH_USER_BANNED',
  };

  bool get isAuthError => _authCodes.contains(code);

  String get userMessage => _messages[code] ?? message;

  @override
  String toString() => userMessage;
}

const _messages = {
  'AUTH_BAD_APIKEY':
      'Your API key is invalid or was revoked. Please log in again.',
  'AUTH_BLOCKED': 'This IP or country is blocked. Validate it in the email AllDebrid sent you.',
  'AUTH_USER_BANNED': 'This account is banned.',
  'MUST_BE_PREMIUM': 'You need a premium account.',
  'MAGNET_MUST_BE_PREMIUM': 'You need a premium account to use magnets.',
  'FREE_TRIAL_LIMIT_REACHED': 'You reached the free trial limit.',
  'LINK_HOST_NOT_SUPPORTED': 'This host is not supported.',
  'LINK_NOT_SUPPORTED': 'This link is not supported.',
  'LINK_DOWN': 'The file is unavailable on the host.',
  'LINK_HOST_UNAVAILABLE': 'The host is under maintenance or down.',
  'LINK_TEMPORARY_UNAVAILABLE':
      'The file is temporarily unavailable on the host.',
  'LINK_TOO_MANY_DOWNLOADS': 'Too many simultaneous downloads on this host.',
  'LINK_HOST_FULL': 'All servers for this host are full. Retry later.',
  'LINK_HOST_LIMIT_REACHED': 'You reached your download limit for this host.',
  'LINK_PASS_PROTECTED': 'This link is password protected.',
  'NO_SERVER': 'Servers are not allowed to use this feature.',
  'MAGNET_INVALID_URI': 'This magnet link is invalid.',
  'MAGNET_INVALID_FILE': 'This torrent file is invalid.',
  'MAGNET_TOO_MANY_ACTIVE': 'You already have too many active magnets.',
  'PIN_EXPIRED': 'The PIN expired. Request a new one.',
};
