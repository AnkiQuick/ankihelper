cd /root
eval "$(jenv init -)"
jenv global 17
./gradlew build -x lint
/opt/android-sdk/build-tools/34.0.0/apksigner sign --ks ankihelper-jennings.keystore  --out ankihelper-3.1.0-release-signed.apk app/build/outputs/apk/release/ankihelper-3.1.0-release-unsigned.apk