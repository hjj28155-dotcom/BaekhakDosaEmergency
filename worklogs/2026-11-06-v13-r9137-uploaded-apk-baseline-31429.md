# V13 R9.13.7 기존 APK 실물 확인 — 2026-11-06 KST

## 선장 제공 자료
- '공유' 파일은 ZIP 구조가 정상인 실물 APK (17,222,587바이트).
- SHA-256: `4c49ccedc3c343fabf4276ec6ed308647b7186fe29e2010edacf319c10c5799f`.
- APK AndroidManifest 바이너리 XML 파싱: 패키지 `com.baekhak.dosa.v13r3preview`, `versionCode=31429`, `versionName=V13 R9.13.7 NAV PRIVATE CUM DB`.
- 대상 선장 휴대전화 앱 정보 화면의 versionName과 일치 (파일이 휴대전화의 설치 바이트와 완전히 동일하다는 독립 검증은 수행하지 않음).
- APK v2 signing block 추출 인증서 SHA256 `3fe57967c15f81ee39ea2b9b88dd96a7506c1b5b9032606ecbea04bbdb15ee3c`.

## 선장에게 이전에 보낸 APK와 직접 비교
- 신규 `V13_R913_R25_ASCII_BUILD_20261105-debug.apk`: `versionCode=31421`, 패키지 및 v2 인증서 SHA256 동일.
- 31421 < 31429: 구버전으로 취급되어 기존 설치본 위에 일반 업데이트 불가. 설치 실패의 **명확한 원인** 발견. 서명 인증서 불일치는 원인이 아님.
- 양쪽 APK ZIP CRC PASS. 엔트리: 기존 87개, 신규 88개; 공통 동일 78개, 공통 내용 변경 4개, 기존에만 5개, 신규에만 6개.
- 특히 `classes2.dex`, `assets/index.html`, `assets/v13_r905_captain_backup_bootstrap.js`, `AndroidManifest.xml` 내용 차이. 현재 R9.13.7에만 `assets/v13_nav_private_r9136.js` 존재.
- 따라서 버전코드만 31430으로 올려 기존 구버전 소스를 설치하면 **R9.13.7 기능 퇴행 가능성** 있음. 버전코드 임의 변경·덮어설치 배포 금지.

## 필수 다음 단계
1. 현재 31429 R9.13.7의 해당 Native 소스/Gradle 프로젝트를 Drive에서 찾아 최신 기능 유지 가능한 베이스라인 확인. APK 자산으로 HTML/JS 복구 가능하지만 DEX 네이티브 소스 동등성은 별도 검증.
2. 최신 R9.13.7 베이스라인에 승인 홈 메뉴/개별 화면 배경/분석방/실제 5조합 통계 참고 연결만 기능 보존형으로 이식.
3. 새 versionCode는 **31430 이상**으로 설정하고 동일 인증서로 빌드, 실물 자산/기능 회귀검사 후 설치 시험. 기존 APK 삭제·DB 초기화 금지.
- V13 선장 전용. V3 항해사앱 및 V7 중앙통제실과 분리.
