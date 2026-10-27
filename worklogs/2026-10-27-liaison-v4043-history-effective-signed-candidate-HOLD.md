# ⚓ 연락관방 — V4.0.43 실제 후보 반영 개선·동일서명 검증 (2026-10-27 KST)

**대상:** 일반 항행의자유, `com.navigator.freedom.v3final`, `versionCode=40043`, `versionName=4.0.43-APPROVAL-ROUND`. V3 항해사앱 / V33 / V7 / 선장 전용 중앙통제실과 별개.

**현재 상태:** 과거 원문 공동분석이 실제 20조합 후보군에 반영되도록 격리 소스 수정, 회귀 테스트 통과, 동일서명 **비공개 후보 APK** 빌드 및 ZIP 재검증 완료. **공식 배포, 이용자 업데이트, 실기기 설치 완료 아님**.

## 1. 수정 소스와 정책
- 격리 브랜치 `fix/freedom-v4043-history-effective-20261027`, 검증 소스 SHA **`02ee97208d89e767c5dd4a4514be91522de45889`**.
- 수정: `build405/FREEDOM_V402_BUILD_THIS/app/src/freedom/assets/index.html` 및 `.github/scripts/verify_freedom_v4043.cjs`.
- 기존 방식은 과거 DB-1 회차 분석을 실행해 점수를 일부 혼합해도 현재 회차 상위 10수에 밀려 **구매 20조합이 변하지 않는 경우**가 확인됐다.
- 수정된 `v4043MergedRanks`: 현재 원문 엔진 상위 6수는 그대로 유지. 나머지 후보 자리에 현재 엔진 후보 2개를 우선 유지하고, 현재 후보와 구별되는 과거 DB-1 엔진 기원 후보 최대 2개를 추가할 수 있도록 함. 기존 현재/과거 점수 비율 `80/20` 근거는 유지하고 `historySpecificCandidates`/ `historyCandidateSlots`를 분석 증거로 저장. 새 번호는 과거 **원문만을 처리한 엔진**의 반환에서 선택하며 외부 정답·당첨번호를 가져오지 않음.
- 과거 결과후 소급등록 금지 / 결과전 구매잠금 보존 / 기존 승인 사용자 재승인 요구 방지 / 최근 5회차 일반 화면 정책 변경 없음.

## 2. 검증
- GitHub CI `40425696788`: DB-1·승인/조합 회귀 및 **과거 후보가 실제 20조합을 변화시키는 테스트 통과**, 참조자산 9종 무결성·Android asset merge 및 unsigned Release ZIP 검사 통과. 전체 run의 최종 실패는 격리 브랜치에서 원본 signer cache 접근 불가 때문이며, 관련 서명은 별도 원본 signer 브랜치에서 수행.
- 원본 서명키 전용 브랜치의 **CI `40425848586` 성공**. 정확한 소스 SHA를 checkout하고, 이전 배포 서명 인증서 SHA-256 일치 → `:app:assembleDebug` → APK 패키지명/버전/서명 및 참조자료 9종 SHA 검사 통과.
- GitHub private artifact ID: **`12699587312`**, 이름 `Freedom_V4043_Original_Signed_PRIVATE_Test_Candidate`. 공식 출시 링크 아님. Drive 업로드·기존 공개 URL 교체 없음.
- CI ZIP 회수 후 실제 APK 독립 검사 결과:
  - 내장 APK 파일: `FREEDOM_V4043_ORIGINAL_SIGNED_CANDIDATE_NOT_RELEASE.apk`.
  - APK 크기: **48,002,462 bytes**.
  - APK SHA-256: **`f8db78b048f263d071696ffac34695f775ab599ade79cd78e27498cd2e3d43fb`**, CI `sha256-original-apk.txt` 영수증과 일치.
  - Package: `com.navigator.freedom.v3final`, versionCode `40043`.
  - Certificate SHA-256: `bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00` (기존 인증서와 일치).
  - APK 내부 참조자료 9개 실제 바이트 SHA와 선언 해시 전부 일치. 개선된 `index.html`과 기존 8.7 MB 엔진 파일 포함.
  - **실제 APK에서 추출한 JS**를 Node VM으로 실행한 추가 검증: DB-1 1~1247 모든 회차 6문장, 1247회 과거 분석 선택 1246·909·623, 실원문 1247회 꿈사전 일치 용어 12건(11개 번호), **모의 엔진 응답**의 서로 다른 20조합, 과거 DB-1 도출 후보가 20조합을 바꾸는 결과 `true`, 잠금 구매기록 불변. **모의 엔진 응답 시험**이며 실제 엔진 결과·Android WebView·이용자 기기 성공으로 해석 금지.
- 이 실행 환경의 Chromium은 localhost/file URL 탐색을 `ERR_BLOCKED_BY_ADMINISTRATOR`로 막음. 실제 화면 자동시험 성공이라고 주장하지 않음.

## 3. 남은 작업 (빠른 우선순위)
1. 실제 Android 기기에서 **업데이트 설치**, 기존 기기 승인 `approved` 유지 및 승인요청 0건, 신규 기기 최초 승인·pending 중복 방지, 기존 DB/구매 잠금 불변을 검증.
2. 엔진의 **실제** 현재 원문 + 과거 DB-1 분석 응답과 20조합 저장 확인. 실행 시 원문 유무 변화, 과거 결과후 분리, 1243~1247 화면 검사. 모의 테스트를 실기기 성공으로 둔갑하지 않음.
3. 정본 4종의 최종 승인된 `백학설명서`/ `성공사례` 완전성 확인, 백학 문장/관계 중심 규칙이 실제 엔진에 통합됐는지 검증. 꿈사전 검색만으로 4자료 공식 해독 완료라고 주장 금지.
4. 위 완료 전 **기존 V4.0.41 정식 APK·Google Drive 고정 다운로드·V3·V33·V7·원본 승인 DB·서명키를 변경하지 않음**.

**연락관 상태:** 동일서명 V4.0.43 연구용 후보 보관 완료, 정식 배포 HOLD.
