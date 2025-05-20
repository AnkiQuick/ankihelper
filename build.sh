cd /root
eval "$(jenv init -)"
jenv global 1.8
./gradlew build -x lint
/opt/android-sdk/build-tools/28.0.0/apksigner sign --ks ankihelper-jennings.keystore  --out ankihelper-2.30.8-release-signed.apk app/build/outputs/apk/release/ankihelper-2.30.8-release-unsigned.apk