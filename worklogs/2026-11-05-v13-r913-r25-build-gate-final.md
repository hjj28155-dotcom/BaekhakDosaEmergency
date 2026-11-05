# V13 선장 전용 APK 빌드 게이트 — 2026-11-05 (추가 단계)

## 확인된 사실
- 소스 ZIP은 V13 R9.13 + R25 통합본. applicationId `com.baekhak.dosa.v13r3preview`, versionCode 31421, Android Gradle Plugin 8.5.2.
- 선장 기존 R8.72 APK SHA256 `e636d0c27ef232d6a4887ee8ca58ecca59709fa8a72187a000e3a280a3e0a933`; v2 서명 인증서 지문 `3fe57967c15f81ee39ea2b9b88dd96a7506c1b5b9032606ecbea04bbdb15ee3c`, subject `CN=Android Debug`.
- AndroidIDE 프로젝트 ZIP에 기존 Debug keystore/jks/p12 파일 없음 (압축 내부 파일명 검사).
- V13 소유자용 보관 P12는 비어 있는 암호로 열리지 않음. 인증서 지문 미확인. 암호 추측/공유/기록 없음.
- 현재 실행 환경에 Android SDK, Gradle, apksigner 없음. **완성 서명 APK 빌드 불가**, 설치 성공 주장 불가.

## 작업 산출물
- 사용자 작업공간 `V13_R913_R25_빌드_서명_최종검증.sh` 작성. `assembleDebug` 수행 후 서명 지문을 기존 인증서와 대조, 다를 경우 설치 금지로 중단하는 fail-closed 검사.
- `V13_R913_R25_선장전용_APK_빌드_안전절차_20261105.md` 설명서와 이전 `V13_동일서명_안전검증.py`를 묶어 `V13_R913_R25_서명검증_도구모음_20261105.zip` 제작.
- bash -n 구문검사 PASS. 실제 Gradle APK 빌드/서명 검증은 환경·서명 미확보로 미실시.
- 기존 설치 V13, 선장용 원문/DB, Google Drive에 보존된 원본 및 키, main 릴리스 설정은 변경하지 않음.

## 필수 다음 단계
1. 기존 R8.72 서명과 일치하는 개인키·인증서를 안전하게 확인(소유자용 P12가 일치하는지 비밀번호 비노출 방식으로 검사).
2. 동일 서명을 사용할 수 없다면 APK 덮어쓰기는 중단. 기존 앱 삭제/재설치로 우회하지 않는다.
3. 동일 서명이 확인되면 AndroidIDE/Gradle 빌드 → APK 서명/무결성 검사 → 백업 완료 확인 → 선장 휴대전화 기존 앱 위에 설치 및 데이터 보존 확인.
