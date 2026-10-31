# V13 중앙통제실 R8.27 배포 인계 — 2026-10-31

## 선장 지시
- V1 방식 고정: 앱이 GitHub 배포 정보 확인 → 새 버전 팝업 → 설치자 다운로드 버튼 → APK 직접 덮어 설치.
- 공지 팝업은 APK 교체 없이 가능. 선장 기기에서 앱을 종료하지 않고 홈 화면으로 나갔다 돌아올 때 공지 팝업 실기기 확인 완료.
- 우측 기능 창은 유지. 삭제 지시한 불필요한 안내 문구 제거, 중복 업데이트 확인 버튼 1개로 정리, 수동 확인 후 업데이트 없으면 '최신 버전입니다.'는 유지.
- 기존 설치 앱/데이터/서명키/V3/V7 원본 보존.

## 확인된 현재 상태
- GitHub releases/v13-release.json: R8.25 / versionCode 31333, manual_apk_v1, announcement=null, silent uiUpdate 없음.
- 선장 기기에서 R8.26 / versionCode 31334 APK 설치 및 원격 공지 팝업 확인(선장 보고).
- R8.27 / versionCode 31335 전체 AndroidIDE 프로젝트 ZIP 생성: `V13_R8_27_V1업데이트_문구삭제_AndroidIDE전체프로젝트.zip` (대화 첨부).
- ZIP SHA256: `7786442f14c37c1a07c799b36da45a0a6c24c6e9b54eda8877fca3e82bbf9438`, 77,171,961 bytes; zip CRC 정상, 143 entries.
- `app/src/main/assets/index.html`: 안내 문구 삭제, 중복 업데이트 확인 버튼 1개로 정리, '최신 버전입니다.' 유지, V1 공지 팝업 함수 존재.
- JavaScript `node --check` 통과; AndroidIDE 소스 app/build.gradle 31335 확인.

## 아직 하지 못한 일 (반드시 미완료로 표시)
- R8.27 설치용 APK 컴파일/서명/기존 APK 덮어쓰기 실기기 검증: 미완료.
- 이번 실행 환경에 Android SDK/Gradle 및 선장 기존 서명키가 없어 동일서명 빌드 불가. 임의 신규 서명 APK를 배포하면 기존 설치 앱의 덮어쓰기 불가 위험.
- R8.27 APK Drive 업로드 및 GitHub 배포 manifest 갱신: **미실시**. ZIP이나 패치 마크다운을 설치용 APK처럼 게시하지 말 것.
- 앱 화면을 종료하지 않은 상태에서 계속 켜 두는 동안 즉시 주기적 공지를 확인하는 기능은 현 R8.26에서 미지원; 홈 나갔다 복귀 시 확인 동작만 실기기 검증.

## 다음 작업 1순위
1. R8.27 프로젝트를 기존 R8.26을 만들었던 동일한 AndroidIDE/서명키로 build.
2. 빌드된 APK 실물 확보 후 applicationId `com.baekhak.dosa.v13r3preview`, versionCode 31335 및 R8.26과 서명 인증서 동일성/해시 검증.
3. Google Drive V13 공식 폴더에 새 APK 업로드 및 URL 유효성 확인.
4. **실물 APK 검증 후만** GitHub `releases/v13-release.json`의 versionCode=31335, versionName=V13 R8.27, downloadUrl을 새 APK 링크로 변경.
5. R8.26 설치 기기에서 종료 없이 앱 포그라운드 복귀 후 업데이트 팝업 → 다운로드 → 덮어쓰기 설치 → R8.27 수동 업데이트 확인에서 '최신 버전입니다.' 표시 검증.

## 주의
- V13, V3 항해사앱, V7 중앙통제실 별도.
- 구버전이나 미서명 APK를 출시하지 않는다.
- GitHub 코드 패치 `releases/patches/V13_R8_26_UI_3FIX_20261031.md`는 설치용 APK가 아니다.
