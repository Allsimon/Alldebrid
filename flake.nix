{
  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  nixConfig = {
    extra-substituters = [ "https://alldebrid.cachix.org" ];
    extra-trusted-public-keys = [
      "alldebrid.cachix.org-1:ODQmrzE20I21PVIIy+D5ggK7e49N0pLXtQccK5eF5bU="
    ];
  };

  outputs =
    { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
        config = {
          allowUnfree = true;
          android_sdk.accept_license = true;
        };
      };
      androidSdk = pkgs.callPackage ./nix/android-sdk.nix { };
    in
    {
      packages.${system} = rec {
        linux = pkgs.callPackage ./nix/linux.nix { };
        android = pkgs.callPackage ./nix/android.nix { inherit androidSdk linux; };
        default = linux;
      };

      devShells.${system}.default = pkgs.mkShell {
        packages = [
          pkgs.flutter
          pkgs.jdk17
          androidSdk.androidsdk
          pkgs.cmake
          pkgs.ninja
          pkgs.pkg-config
          pkgs.clang
          pkgs.gtk3
          pkgs.libsecret
          pkgs.jsoncpp
        ];
        ANDROID_HOME = androidSdk;
        ANDROID_SDK_ROOT = androidSdk;
        JAVA_HOME = pkgs.jdk17.home;
        # aapt2 from maven is dynamically linked against FHS paths
        GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk.aapt2}";
      };

      checks.${system} = {
        inherit (self.packages.${system}) linux android;
        devShell = self.devShells.${system}.default;
      };
    };
}
