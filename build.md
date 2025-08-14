# Build package

## Build process

* docker or podman is installed
* use [docker-android-build-box](https://github.com/mingchen/docker-android-build-box)
* build process

    ```
    docker run -v `pwd`:/root -it mingc/android-build-box bash -l
    root@9ebf114d21a8:/project# cd
    root@9ebf114d21a8:~# eval "$(jenv init -)"
    root@9ebf114d21a8:~# jenv global 17
    root@9ebf114d21a8:~# ./gradlew build -x lint
    root@9ebf114d21a8:~# keytool -genkey -v -keystore ankihelper-jennings.keystore -alias ankihelper-jennings -keyalg RSA -keysize 2048 -validity 10000
    root@9ebf114d21a8:~# /opt/android-sdk/build-tools/34.0.0/apksigner sign --ks ankihelper-jennings.keystore  --out ankihelper-3.1.0-release-signed.apk app/build/outputs/apk/release/ankihelper-3.1.0-release-unsigned.apk
    ```
docker run -v `pwd`:/project -it mingc/android-build-box bash -l


## debug

- use adb logcat to get the crash log
  ```shell
  adb logcat | grep "PopupActivity"


## qwen code

ask it to compile without sdk

```
check if Java compilation works without the full Android SDK:
javac -cp "app/src/main/java:app/build/generated/source/buildConfig/debug" app/src/main/java/com/mmjang/ankihelper/ui/popup/PopupActivity.java -d /tmp
```