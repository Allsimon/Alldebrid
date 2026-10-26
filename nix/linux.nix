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
in
flutter.buildFlutterApplication {
  pname = "alldebrid";
  # Not parsed with yq: nix-update evaluates `name` read-only and can't build it.
  version = builtins.head (builtins.match ".*\nversion: ([^\n]+).*" (builtins.readFile ../pubspec.yaml));
  inherit src;
  autoPubspecLock = ../pubspec.lock;

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
