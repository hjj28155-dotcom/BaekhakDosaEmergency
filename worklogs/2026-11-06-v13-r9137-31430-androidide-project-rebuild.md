# V13 선장 전용 R9.13.7(31429) → 31430 AndroidIDE 빌드 소스 — 2026-11-06

## 선장 지시 / 보호 기준
- V13 선장 전용 앱만 대상. V3 항해사앱·V7 중앙통제실과 분리.
- 선장이 제공한 R9.13.7 실물 APK(`공유`), `versionCode=31429`, SHA256 `4c49ccedc3c343fabf4276ec6ed308647b7186fe29e2010edacf319c10c5799f` 기준.
- 기존 V13 삭제·데이터 초기화 및 과거 설치본 덮어쓰기 금지. 원본 APK와 기존 31421 AndroidIDE ZIP 보존.

## 실제 네이티브 DEX 추가 검사 (신규)
- R9.13.7 실물 APK와 선장이 이전에 AndroidIDE에서 빌드한 31421 APK의 `classes.dex`, `classes2.dex`를 DEX 헤더/메서드 인덱스/실행 명령 단위로 파싱 비교.
- `classes.dex` 3/3, `classes2.dex` 139/139, 총 **142/142 메서드 실행 명령 SHA256 일치**, 메서드 목록 추가·삭제 없음.
- DEX 전체 파일은 같지 않으므로 클래스 메타데이터·정적 필드·try 핸들러/주석·비코드 자원까지 동등하다는 뜻은 아님. 실제 Android에서 완전 호환 보증 아님.
- 검사 스크립트 `v13_dex_method_audit.py`, 결과 `V13_R9137_31429_네이티브_메소드비교_20261106.json` 로컬 제작.

## 새 AndroidIDE 완전 프로젝트 (실제 제작)
- 기준 소스: 이전 `V13_R913_R25_ANDROIDIDE_ASCII_NEW_PROJECT_20261105.zip`의 Java/Gradle. 정확한 31429 원본 Java 프로젝트가 아니므로 APK 네이티브 메서드 동등성 근거로만 제한적으로 사용.
- 해당 Gradle 소스에 R9.13.7 **실제 APK의 assets 81개를 전체 교체**, 그중 80개는 실물 APK와 SHA/바이트 동일.
- 유일하게 `assets/index.html`은 선장 승인 **홈 10개 메뉴 + 설정 위 분석방 + 신규 연구용 통계 참고** 반영 검증본으로 교체. HTML SHA256 `39f8282b1ca982bb385fe765c1c7a4188765a97db2dd18a6806b66772730d5b9`.
- `app/build.gradle`: `versionCode=31430`, `versionName=V13 R9.13.8 NAV PRIVATE CUM DB + ANALYSIS`; 패키지/minSdk/targetSdk 보존.
- 모든 파일/루트 폴더 경로 ASCII, 새 루트 `V13_R9137_31430_ANALYSIS_ROOM_BUILD`; AndroidIDE의 기존 폴더와 혼동 방지.
- 기존 R9.13.7에만 있던 `assets/v13_nav_private_r9136.js`와 `assets/v13_r905_captain_backup_bootstrap.js`도 원본 바이트 그대로 포함.
- 산출물 `V13_R9137_31430_ANALYSIS_ROOM_ANDROIDIDE_BUILD_20261106.zip` **20,409,145바이트**, SHA256 `4fd02d3cc0147ee5bb7a25f63003bc3c2a7c956c7dc9ab38bc07d1b5ba3fa98b`, ZIP 302개 엔트리, CRC PASS. 기존 APK 보존.
- Google Drive 루트 신규 업로드 및 metadata 재조회 확인: https://drive.google.com/file/d/1Bs2aTU-HKQX5xcTnlYmgn8E9TJU1DlcV/view?usp=drivesdk (20,409,145 bytes).

## 검증 / 남은 작업
- **검증됨**: 네이티브 메서드 명령 142/142 일치(기존 APK 두 개), 원본 80개 자산 보존, ZIP CRC/영문 경로/Gradle 버전 31430.
- **미검증**: 이 새 소스의 AndroidIDE Gradle 빌드, 새 APK v2 서명/전체 무결성, 새 APK DEX 142개 재비교, Android 실제 설치/누적 DB/4방 연구 5조합 실증.
- 동일 Debug 서명 인증서 SHA256 기준 `3fe57967c15f81ee39ea2b9b88dd96a7506c1b5b9032606ecbea04bbdb15ee3c`. 빌드된 APK 수신 후 확인 필수.
- 선장폰에 바로 설치하지 말고, 새 APK를 먼저 업로드받아 검증한 후 덮어쓰기 여부 결정. 기존 앱 삭제/데이터 초기화 금지.
