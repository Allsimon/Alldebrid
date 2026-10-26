{
  lib,
  flutter,
  makeDesktopItem,
  copyDesktopItems,
  libsecret,
  jsoncpp,
  xdg-user-dirs,
}:

let
  src = lib.fileset.toSource {
    root = ../.;
    fileset = lib.fileset.unions [
      ../android
      ../assets
      ../lib
      ../linux
      ../pubspec.yaml
      ../pubspec.lock
    ];
  };
  # Import-from-derivation (yq at eval time) breaks nix-update, which evaluates
  # read-only; regenerate with `yq . pubspec.lock > nix/pubspec.lock.json`.
  pubspecLock = lib.importJSON ./pubspec.lock.json;
in
flutter.buildFlutterApplication {
  pname = "alldebrid";
  version = builtins.head (builtins.match ".*\nversion: ([^\n]+).*" (builtins.readFile ../pubspec.yaml));
  inherit src;
  inherit pubspecLock;
  passthru = { inherit pubspecLock; };

  nativeBuildInputs = [ copyDesktopItems ];
  buildInputs = [
    libsecret
    jsoncpp
  ];

  # Downloads need `xdg-user-dir` to locate the Downloads folder.
  extraWrapProgramArgs = "--prefix PATH : ${lib.makeBinPath [ xdg-user-dirs ]}";

  desktopItems = [
    (makeDesktopItem {
      name = "com.malek.alldebrid";
      desktopName = "AllDebrid";
      exec = "alldebrid";
      icon = "alldebrid";
      categories = [ "Network" ];
    })
  ];

  postInstall = ''
    install -Dm644 assets/icon.png $out/share/icons/hicolor/1024x1024/apps/alldebrid.png
  '';

  meta = {
    description = "AllDebrid client";
    license = lib.licenses.gpl3Only;
    mainProgram = "alldebrid";
    platforms = lib.platforms.linux;
  };
}
