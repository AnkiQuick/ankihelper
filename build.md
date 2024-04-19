# Build package

## Build process

* docker or podman is installed
* use [docker-android-build-box](https://github.com/mingchen/docker-android-build-box)
* build process

    ```
    docker run -v `pwd`:/project -it mingc/android-build-box bash -l
    root@9ebf114d21a8:/project# jenv global 1.8
    root@9ebf114d21a8:/project# ./gradlew build -x lint
    root@9ebf114d21a8:/project# keytool -genkey -v -keystore ankihelper-jennings.keystore -alias ankihelper-jennings -keyalg RSA -keysize 2048 -validity 10000
    root@9ebf114d21a8:/project# /opt/android-sdk/build-tools/28.0.0/apksigner sign --ks ankihelper-jennings.keystore  --out ankihelper-jennings.apk app/build/outputs/apk/release/ankihelper-2.30.7-release-unsigned.apk
    ```
