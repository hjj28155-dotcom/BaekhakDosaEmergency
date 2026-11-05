# V13 선장 전용 AndroidIDE 빌드 APK 실제 수신 검증 — 2026-11-05

## 검증 대상
- 선장 업로드: `V13_R913_R25_ASCII_BUILD_20261105-debug.apk`
- 크기 **18,074,976 bytes**.
- SHA-256: `f2a9d16ab81a5416a4d0441f3534361c32fa8c9b49c434a524e186ff018465e3`.
- APK ZIP CRC PASS (88 entries).

## 기존 R8.72 APK 비교
- 비교 원본 `V13_R8_72_AndroidIDE_PROJECT-debug.apk`, SHA256 `e636d0c27ef232d6a4887ee8ca58ecca59709fa8a72187a000e3a280a3e0a933`.
- 패키지 `com.baekhak.dosa.v13r3preview`: 기존/신규 **동일**.
- `versionCode`: 기존 31380 → 신규 **31421**.
- `minSdkVersion`: 26 동일. `targetSdkVersion`: 34 동일.
- Android v2 signing block 인증서 SHA-256: 양쪽 **`3fe57967c15f81ee39ea2b9b88dd96a7506c1b5b9032606ecbea04bbdb15ee3c` 정확히 일치**.
- v2 signed-data `0x0103` (RSA PKCS#1/SHA-256) 암호학적 인증서 공개키 서명: **각 APK 검증 PASS**. 다만 apksigner의 전체 APK 콘텐츠 digest 검증을 완전히 재구현한 것은 아님.

## 통합 홈 화면 포함
- APK `assets/index.html` = 기존 검증된 통합 소스의 동일 경로 HTML (1,754,975 bytes).
- HTML 양쪽 SHA256 `87693b272ccb5c3eb4d625724837b2de6bdabb831c21dd33f6769b84ee9f5622` **완전히 일치**.
- 중앙 원문분석 버튼 및 신서방(구 도사방) 레이블 포함 확인.
- 검증된 통합 소스 전체 브라우저 시나리오 20/20 통과 기록은 유지.

## 판정 및 잔여
- **기존 R8.72 APK 바이너리와의 패키지/버전/서명 조건 통과**.
- 선장 실제 설치 APK가 비교한 R8.72와 동일한지, 설치 후 데이터 보존, Android 실기기 UX는 여전히 미검증.
- 기존 앱 삭제/데이터 초기화 금지. 데이터 백업 후 기존 앱 위에 설치 테스트 가능. 동일 서명 설치 불가 메시지가 나오면 설치 중단하고 기존 앱 유지.
- V13 선장 전용. V3 및 V7 분리, 본 APK 공개 자동 배포 없음.
