# AllDebrid

An unofficial [AllDebrid](https://alldebrid.com) client for Android and Linux, built with Flutter.

- Unlock hoster links, then open them or download them to your Downloads folder
- Add magnets and `.torrent` files, follow their progress and unlock the resulting files
- See your premium status and host availability
- On Android, share links, magnets or `.torrent` files to the app, or open `magnet:` links with it

## Logging in

The app uses the [AllDebrid API](https://docs.alldebrid.com). Log in with the PIN shown in the app
by entering it on alldebrid.com, or paste an API key from
[alldebrid.com/apikeys](https://alldebrid.com/apikeys).

## Building

The dev environment is provided by the Nix flake (Flutter, JDK, Android SDK, Linux desktop deps):

```bash
nix develop        # or `direnv allow`
flutter test
flutter run -d linux
flutter build apk
```

Nix packages:

```bash
nix build .#linux     # result/bin/alldebrid
nix build .#android   # result/alldebrid.apk
```

The Android build replays the Gradle downloads pinned in `nix/android-deps.json`.
Refresh it after changing Flutter, plugins or Gradle config:

```bash
$(nix build .#android.mitmCache.updateScript --print-out-paths --no-link)
```

## License

GPLv3
