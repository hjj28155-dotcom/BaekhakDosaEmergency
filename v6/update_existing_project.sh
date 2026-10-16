#!/bin/sh
# 항행의자유 V6.0.7: keep the existing AndroidIDE project, signing key, and app data.
set -eu
if [ ! -f settings.gradle ] || [ ! -f app/build.gradle ] || [ ! -f app/src/main/AndroidManifest.xml ]; then
  echo "[STOP] AndroidIDE의 V6_AndroidIDE_Project 최상위 폴더에서 실행해 주세요."
  exit 2
fi
if command -v curl >/dev/null 2>&1; then
  DOWNLOADER=curl
elif command -v wget >/dev/null 2>&1; then
  DOWNLOADER=wget
else
  echo "[중단] curl·wget이 모두 없습니다. AndroidIDE 터미널에서 'pkg install curl'을 실행해 주세요."
  exit 2
fi
case "$(pwd)" in
  */V6_AndroidIDE_Project) ;;
  *) echo "[주의] 현재 경로: $(pwd). 위 파일들이 확인되어 이 경로에서 진행합니다." ;;
esac
BASE='https://raw.githubusercontent.com/hjj28155-dotcom/BaekhakDosaEmergency/main/v6'
JAVA='app/src/main/java/kr/hanghae/v6/centralcontrol/MainActivity.java'
ASSET='app/src/main/assets/v6_fallback.html'
STAGE=$(mktemp -d './.v6_600008_stage.XXXXXX')
trap 'rm -rf "$STAGE"' EXIT HUP INT TERM
fetch() {
  echo "파일 내려받는 중: $1"
  if [ "$DOWNLOADER" = curl ]; then
    curl --fail --location --silent --show-error --retry 2 --connect-timeout 20 --max-time 90 "$BASE/$1" --output "$STAGE/$2"
  else
    wget -q -T 30 -t 3 -O "$STAGE/$2" "$BASE/$1"
  fi
  test -s "$STAGE/$2" || { echo "[중단] 받은 파일이 비었습니다."; exit 3; }
}
fetch 'index.html' index.html
fetch 'app/build.gradle' build.gradle
fetch 'app/src/main/assets/v6_fallback.html' v6_fallback.html
fetch 'app/src/main/java/kr/hanghae/v6/centralcontrol/MainActivity.java' MainActivity.java
grep -Fq 'versionCode 600008' "$STAGE/build.gradle" || { echo '[중단] V6 버전 검증 실패'; exit 3; }
grep -Fq "applicationId 'kr.hanghae.v6.centralcontrol'" "$STAGE/build.gradle" || { echo '[중단] 앱 식별자 불일치'; exit 3; }
grep -Fq 'V6NativeApp' "$STAGE/MainActivity.java" || { echo '[중단] 종료 기능 검증 실패'; exit 3; }
grep -Fq 'CURRENT_VERSION_CODE=600008' "$STAGE/v6_fallback.html" || { echo '[중단] 홈 화면 버전 불일치'; exit 3; }
grep -Fq 'V6.exitApp()' "$STAGE/v6_fallback.html" || { echo '[중단] 홈 종료 버튼 누락'; exit 3; }
cmp "$STAGE/index.html" "$STAGE/v6_fallback.html" >/dev/null || { echo '[중단] 화면 파일 불일치'; exit 3; }
for file in index.html app/build.gradle "$JAVA" "$ASSET"; do
  if [ ! -f "$file" ]; then echo "[중단] 기존 프로젝트 파일 없음: $file"; exit 2; fi
done
BACKUP="V6_BACKUP_BEFORE_600008_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP/$(dirname "$JAVA")" "$BACKUP/$(dirname "$ASSET")"
cp index.html "$BACKUP/index.html"
cp app/build.gradle "$BACKUP/app/build.gradle"
cp "$JAVA" "$BACKUP/$JAVA"
cp "$ASSET" "$BACKUP/$ASSET"
cp "$STAGE/index.html" index.html
cp "$STAGE/build.gradle" app/build.gradle
cp "$STAGE/MainActivity.java" "$JAVA"
cp "$STAGE/v6_fallback.html" "$ASSET"
echo
echo '============================================'
echo 'V6.0.7 업데이트 소스 반영 완료 (버전 600008)'
echo "기존 파일 백업: $(pwd)/$BACKUP"
echo '기존 V6 설치 앱, 개인 데이터, 서명키는 변경하지 않았습니다.'
echo 'AndroidIDE로 돌아가 Build APK를 실행하세요.'
echo '빌드한 APK를 기존 V6 위에 설치하세요 (앱 삭제 금지).'
echo '홈 화면 하단 🚪 앱 종료 버튼을 확인하세요.'
echo '============================================'
