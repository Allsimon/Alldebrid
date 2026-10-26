{
  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs =
    { nixpkgs, ... }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
        config = {
          allowUnfree = true;
          android_sdk.accept_license = true;
        };
      };
      android = pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ "35" "36" "37" ];
        buildToolsVersions = [ "35.0.0" "36.0.0" ];
        includeNDK = true;
        ndkVersions = [ "28.2.13676358" ];
        cmakeVersions = [ "3.22.1" ];
      };
      upstreamSdk = "${android.androidsdk}/libexec/android-sdk";
      # The ninja bundled with the SDK's cmake segfaults on NixOS (exit 139).
      sdk = pkgs.runCommand "android-sdk" { } ''
        mkdir $out
        for entry in ${upstreamSdk}/*; do ln -s "$entry" $out/; done
        rm $out/cmake
        cp -rsL ${upstreamSdk}/cmake $out/cmake
        chmod -R u+w $out/cmake
        ln -sf ${pkgs.ninja}/bin/ninja $out/cmake/3.22.1/bin/ninja
      '';
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        packages = [
          pkgs.flutter
          pkgs.jdk17
          android.androidsdk
          pkgs.cmake
          pkgs.ninja
          pkgs.pkg-config
          pkgs.clang
          pkgs.gtk3
          pkgs.libsecret
          pkgs.jsoncpp
        ];
        ANDROID_HOME = sdk;
        ANDROID_SDK_ROOT = sdk;
        JAVA_HOME = pkgs.jdk17.home;
        # aapt2 from maven is dynamically linked against FHS paths
        GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${upstreamSdk}/build-tools/36.0.0/aapt2";
      };
    };
}
