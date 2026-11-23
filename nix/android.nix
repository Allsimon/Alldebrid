{
  lib,
  flutter,
  gradle,
  jq,
  jdk,
  runtimeShell,
  androidSdk,
  linux,
}:

let
  # --no-pub skips generating the plugin registrant, and Gradle 9 refuses
  # read-only project dirs, so plugins are built from writable copies.
  buildApk = ''
    flutter build apk --release --split-per-abi --config-only
    mkdir plugins
    for dir in $(jq -r '.plugins.android[].path' .flutter-plugins-dependencies); do
      cp -r --no-preserve=mode "$dir" plugins/
    done
    sed -i "s|/nix/store/|$PWD/plugins/|g" .flutter-plugins-dependencies
    flutter build apk --release --no-pub --split-per-abi
  '';

  # buildFlutterApplication only targets linux/web and strips the other engine
  # artifacts; its multiShell variant keeps the full Flutter, android included.
  app = flutter.buildFlutterApplication {
    pname = "alldebrid-android";
    inherit (linux) version src;
    autoPubspecLock = ../pubspec.lock;

    nativeBuildInputs = [
      gradle
      jq
    ];

    mitmCache = gradle.fetchDeps {
      pkg = app.multiShell;
      data = ./android-deps.json;
    };

    env = {
      ANDROID_HOME = androidSdk;
      ANDROID_SDK_ROOT = androidSdk;
      JAVA_HOME = jdk.home;
      NIX_AAPT2_BINARY_PATH = androidSdk.aapt2;
    };

    # Flutter runs android/gradlew; route it to nixpkgs' gradle so the
    # mitm-cache proxy (or --offline) flags from the gradle setup hook apply.
    postConfigure = ''
      cat > android/gradlew <<EOF
      #!${runtimeShell}
      exec ${lib.getExe gradle} $(printf '%q ' "''${gradleFlagsArray[@]}") "\$@"
      EOF
      chmod +x android/gradlew
    '';

    buildPhase = ''
      runHook preBuild
      ${buildApk}
      runHook postBuild
    '';

    # Record what the real build fetches. The generic nixDownloadDeps task that
    # would run next fails on Flutter's included gradle plugin build.
    preGradleUpdate = ''
      ${buildApk}
      exit 0
    '';

    installPhase = ''
      runHook preInstall
      for apk in build/app/outputs/flutter-apk/app-*-release.apk; do
        abi=''${apk##*/app-}
        install -Dm644 "$apk" "$out/alldebrid-''${abi%-release.apk}.apk"
      done
      mkdir -p $debug
      runHook postInstall
    '';

    meta = linux.meta // {
      platforms = [ "x86_64-linux" ];
    };
  };
in
app.multiShell
