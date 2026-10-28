# 항행의자유 일반 배포판 V4.0.44 — 동일 서명 시험 APK 인계 기록

- 기준 시각: 2026-10-29 KST
- 대상 앱: **일반 배포판** `com.navigator.freedom.v3final`; V3 항해사앱/V7 중앙통제실/중앙통제 APK와 혼동 금지
- 정식 자동배포 경로: `latest-apk` v84 / `latest-apk-info` v54 — 아직 **V4.0.36, versionCode 40036**
- 시험 소스: `fix/freedom-v4044-reference-preserving-20261029`, 빌드에서 사용한 고정 커밋 `4cbe40fda32e5b66314844f24a5246ae4e8cca43`
- 시험 앱: `versionCode 40044`, `versionName 4.0.44-RAW-GRADE-RECOVERY`
- 검증 기준판: `ca2ecd50ed76f2b879c28067616419c1b02452eb` (공식 원본 참조자료 다수 포함된 V4.0.43 검증 소스)
- 성공 CI 실행: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40714680028
- CI 실행 결론: `success` (2026-10-28T15:20:48Z)
- 시험용 ZIP 아티팩트 이름: `Freedom_V4044_OriginalSigned_TEST_ONLY`, GitHub Actions 아티팩트 ID `12826979354`
- 아티팩트 보관 만료 예정: 2026-10-31 15:20:42 UTC (2026-11-01 00:20:42 KST)
- ZIP 내부 APK 이름: `HANGHAE_FREEDOM_V4044_ORIGINAL_SIGNED_TEST_ONLY.apk`
- 아티팩트 ZIP 크기: 48,007,615 bytes (APK 자체 크기와 다름)

## 소스 수정

1. 최근 5회차(1243~1247회) 원문 화면과 저장된 조합 채점 화면 연결; `rawPage` 및 `scorePage`도 결과 화면 접근.
2. 기기 저장 원문이 6문장 미만이면 기존 값은 변경하지 않고 DB-1 내장 6문장 표시.
3. 저장된 자동분석 20개 조합 사이에 같은 조합이 있어도 원본 상태로 채점하고 중복 안내. 각 조합 내부 숫자 중복/범위 검증 유지.
4. 저장 5조합 `buy.locked`의 성적을 별도 인증 표기로 비교; 사후 연구/이관 `analysis.groups` 또는 `legacy.groups`는 최초잠금 인증과 명시적으로 분리.
5. 조합 원본이 없으면 결과 후 자동 재생성 또는 결과 전 원본 위조 금지.
6. 640px 이하 긴 제목 줄바꿈 및 닫기 버튼 겹침 완화.
7. 원래 수정 소스에 없었던 `freedom_reference_v4043.js`, 공식 원본 텍스트 자산 9개 및 참조 사전 검증 스크립트를 `ca2...` 기준판에서 **그대로 유지**.
8. V4.0.43 기준판의 `V330_WIN[1247]` 누락을 기존 검증 번호 `8 14 21 22 42 45`, 보너스 `24`로 보완.
9. 독립 테스트 `.github/scripts/verify_freedom_v4044.cjs` 추가; V4.0.43 라우팅 테스트는 새 보존 기록 조회 방식으로 업데이트(잠금/원본 불변 검사는 유지).

## 검증 결과

- GitHub Actions `same_signer_test` 11개 작업 단계 전체 성공.
- **기존 서명키 캐시 접근 성공:** 원래 signer cache scope `fix-freedom-v3-screen-1244-20261022`에 배치한 격리 빌드 워크플로를 사용. 기존 서명 인증서 공개 SHA-256 `bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00` 일치. 서명키를 새로 생성하거나 외부 공개하지 않음.
- 온보딩 단계, DB-1 원문, 자동분석/잠금 보존, 9개 검증 원본 참조자료 및 인덱스, 저장 조합·중복·이관/원문 자동채점, 6개 인라인 JS 문법 검사 통과.
- `Gradle :app:assembleDebug` 실제 컴파일 성공.
- Android 패키지 이름 `com.navigator.freedom.v3final` / 버전코드 `40044` / 동일 서명 인증서 SHA-256 일치.
- ZIP 압축 정합성 및 APK 내 `assets/index.html` 소스와 GitHub 기준 소스 **바이트 일치**, `assets/raw_bank.js`, `assets/db1_v1247_patch.js`, 원본 참조 엔진 및 인덱스 존재 확인.
- CI 시험 아티팩트 업로드 성공; GitHub Release 및 공식 `latest-apk` 수정/배포는 수행하지 않음.

## 남은 업무와 금지 조치

1. 선장 실제 휴대전화의 **설치된 일반 배포판 패키지·서명·버전 확인** 후, 기존 앱 제거 없이 시험 APK를 업그레이드 설치할 수 있는지 검증.
2. 선장의 기기 내 원문·잠금 구매 5조합·자동분석 20조합·승인·자료 이관 저장 여부 확인. 테스트 전에 데이터 백업 권장. **설정 데이터/앱 저장공간 초기화, 삭제·재설치 금지**.
3. 제1247회 화면에서 원문 6구절, 닫기 버튼 제목 겹침, 당첨번호, 기존 저장 조합 1~5등/낙선 결과 검증. 실제 선장 기기에서 조합 데이터가 없는 경우 “저장 조합 확인 대기” 표기가 정상.
4. 사용자 실기기 검증 전 공식 업데이트 경로 변경·정식 배포 및 완료 선언 금지.
5. 기존 원본 1~16회차, DB-1, V3, V7, 최초잠금 자료는 삭제·덮어쓰기·초기화하지 않음.

## 시험 APK 실파일 추가 검증

- ChatGPT 작업 환경에서 GitHub Actions 아티팩트 ZIP을 실제 다운로드해 `HANGHAE_FREEDOM_V4044_ORIGINAL_SIGNED_TEST_ONLY.apk`를 추출함.
- 실 APK 바이트 크기: **48,004,558**.
- 실 APK SHA-256: **`6da43500c1986201f97ca0c8bb1570658e564d0d77ca31e8aef90ad27b9414be`**.
- 아티팩트에 포함된 `SHA256.txt` 기대값과 추출 APK의 새 계산값 **일치**.
- 추출 ZIP/APK 압축 무결성 확인, `assets/index.html` 원문/채점·모바일 제목 수정 함수 내장, `assets/raw_bank.js`, `assets/db1_v1247_patch.js`, reference JS/manifest/engine 내장 확인.
- 아티팩트의 Android package 확인자료는 `com.navigator.freedom.v3final` / `versionCode 40044` / `4.0.44-RAW-GRADE-RECOVERY`; 인증서 SHA-256은 기존 공개 지문 `bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00`와 일치함.
- APK 파일은 현재 채팅 산출물로 제공함. **정식 배포가 아니라 실기기 시험용**이며, 앱 저장공간 삭제·초기화 없이 업데이트로만 사용해야 함. 공식 배포 포인터는 계속 V4.0.36.
