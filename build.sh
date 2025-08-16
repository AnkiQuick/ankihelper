#cd /root
eval "$(jenv init -)"
#jenv global 17
./gradlew build -x lint
if [[ $? -eq 0 ]]
then
#    /opt/android-sdk/build-tools/34.0.0/apksigner sign --ks ankihelper-jennings.keystore  --out ankihelper-3.1.1-release-signed.apk app/build/outputs/apk/release/ankihelper-3.1.1-release-unsigned.apk
    ~/development/android-sdk/build-tools/34.0.0/apksigner sign --ks ankihelper-jennings.keystore  --out ankihelper-3.1.1-release-signed.apk app/build/outputs/apk/release/ankihelper-3.1.1-release-unsigned.apk


fi
