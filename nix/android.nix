{
  lib,
  flutter,
  gradle_9,
  jdk17,
  runtimeShell,
  androidSdk,
  linux,
}:

let
  gradle = gradle_9.override { java = jdk17; };

  # buildFlutterApplication only targets linux/web and strips the other engine
  # artifacts; its multiShell variant keeps the full Flutter, android included.
  app = flutter.buildFlutterApplication {
    pname = "alldebrid-android";
    inherit (linux) version src;
    inherit (linux.passthru) pubspecLock;

    nativeBuildInputs = [ gradle ];

    mitmCache = gradle.fetchDeps {
      pkg = app.multiShell;
      data = ./android-deps.json;
    };

    env = {
      ANDROID_HOME = androidSdk;
      ANDROID_SDK_ROOT = androidSdk;
      JAVA_HOME = jdk17.home;
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
      flutter build apk --release --no-pub
      runHook postBuild
    '';

    # Record what the real build fetches. The generic nixDownloadDeps task that
    # would run next fails on Flutter's included gradle plugin build.
    preGradleUpdate = ''
      flutter build apk --release --no-pub
      exit 0
    '';

    installPhase = ''
      runHook preInstall
      install -Dm644 build/app/outputs/flutter-apk/app-release.apk $out/alldebrid.apk
      mkdir -p $debug
      runHook postInstall
    '';

    meta = linux.meta // {
      platforms = [ "x86_64-linux" ];
    };
  };
in
app.multiShell
