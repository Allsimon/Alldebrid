{
  androidenv,
  runCommand,
  ninja,
}:

let
  android = androidenv.composeAndroidPackages {
    platformVersions = [
      "35"
      "36"
      "37"
    ];
    buildToolsVersions = [
      "35.0.0"
      "36.0.0"
    ];
    includeNDK = true;
    ndkVersions = [ "28.2.13676358" ];
    cmakeVersions = [ "3.22.1" ];
  };
  upstream = "${android.androidsdk}/libexec/android-sdk";
in
# The ninja bundled with the SDK's cmake segfaults on NixOS (exit 139).
runCommand "android-sdk"
  {
    passthru = {
      inherit (android) androidsdk;
      aapt2 = "${upstream}/build-tools/36.0.0/aapt2";
    };
  }
  ''
    mkdir $out
    for entry in ${upstream}/*; do ln -s "$entry" $out/; done
    rm $out/cmake
    cp -rsL ${upstream}/cmake $out/cmake
    chmod -R u+w $out/cmake
    ln -sf ${ninja}/bin/ninja $out/cmake/3.22.1/bin/ninja
  ''
