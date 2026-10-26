{
  lib,
  flutter,
  runCommand,
  yq,
  makeDesktopItem,
  copyDesktopItems,
  libsecret,
  jsoncpp,
  xdg-user-dirs,
}:

let
  toJSON = file: lib.importJSON (runCommand "json" { } "${lib.getExe yq} . ${file} > $out");
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
  pubspecLock = toJSON ../pubspec.lock;
in
flutter.buildFlutterApplication {
  pname = "alldebrid";
  version = (toJSON ../pubspec.yaml).version;
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
