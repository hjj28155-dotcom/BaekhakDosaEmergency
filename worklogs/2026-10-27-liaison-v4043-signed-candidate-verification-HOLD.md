# ⚓ 항행의자유 일반 배포판 V4.0.43 — 연락관 연속 작업 기록 (2026-10-27 KST)

대상: **일반 항행의자유 배포판** `com.navigator.freedom.v3final`, V4.0.43 / versionCode 40043. V3 항해사앱·V33·V7 중앙통제실·선장 전용 독립 중앙통제실과 별도.
**판정: 동일 서명 비공개 후보 APK 생성·검증 성공. 정식 배포·선장폰 설치·실동작 검증은 HOLD.**

## 정본 및 코드 출처
- 보존한 이전 배포판: V4.0.41 정식 APK와 기존 Google Drive 고정 링크 (변경 없음).
- 원본 V4.0.43 후보: `fix/freedom-v4043-repeat-approval-round-combos-20261027` SHA `72be12acdf37c7b78bc16ef1a1063b9f4261e5b6`.
- 별도 참조자료 격리 브랜치: `fix/freedom-v4043-reference-pack-20261027`. **서명·APK 생성에 사용한 정확한 소스 커밋**: `be6acd119674d45629f31a17b5f3e79e81d38425`.
- 수정내역: 원본/변환 참조자료 9개, SHA-256 manifest, Android APK ZIP에서 한글 경로가 비정상 표기되어 생긴 탑재 검사 문제를 피하는 ASCII 경로/동일바이트 사본 9개, 꿈사전 45번호 색인과 부모 `index.html` 엔진 응답 연결, 결과후 성공기록 점수 사용 차단, DB-1 원문 우선/과거 비교 연구 경로, 승인 및 구매 잠금 단위시험.
- 원본의 한글 파일명과 바이트는 `reference_sources`에 그대로 보존. APK 실행 호환성 사본은 `reference_runtime`의 영문 경로, 매핑 및 SHA-256은 `reference_sources_manifest_v4043.json`에 기록.
- 정확한 승인 `백학설명서`와 전체 `성공사례` 원본 완전성은 **미검증**. `백학_공식해독규칙_V1.0.txt`와 일부 성공사례는 감사 보관본이며, 결과전 숫자 선택의 자동 해독 규칙으로 승격하지 않음.

## 검증 증거
1. CI `40423835052` (격리 브랜치): 소스/DB-1·승인·참조 무결성 검사 및 unsigned `:app:assembleRelease` / ZIP 해시·버전 검증 **통과**. 해당 작업의 전체 CI 결론 실패는 새 브랜치에 접근 권한 없는 원본 keystore cache 복원 실패 때문. 미서명 내부 APK는 검사 뒤 삭제해 배포하지 않음.
2. 실제 원본 서명 캐시가 있는 브랜치: `fix-freedom-v3-screen-1244-20261022`. 이 브랜치의 새 워크플로는 **소스 브랜치 HEAD가 아닌 정확한 SHA** `be6acd1`만 checkout하고 기존 키 캐시를 읽음. 서명키 생성·변경·출력·저장소 복사 없음.
3. **CI `40423882784` 성공**: 기존 cert 확인 → DB-1/승인/소스회귀 검사 → Android `:app:assembleDebug` → 패키지·서명 인증서·참조자료 해시 검사 → 비공개 후보 아티팩트 저장까지 모두 성공.
   - Run: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40423882784
   - Artifact: `Freedom_V4043_Original_Signed_PRIVATE_Test_Candidate`, ID `12699106856`; 2026-10-30 00:04 UTC 만료 예정. **정식 다운로드/업데이트 링크 아님.**
4. 서명된 후보 APK를 별도로 회수해 ZIP 및 영수증을 재검사:
   - 내부 APK 파일명: `FREEDOM_V4043_ORIGINAL_SIGNED_CANDIDATE_NOT_RELEASE.apk`
   - APK 바이트: **48,001,988**
   - **APK SHA-256**: `bb1017a8b4f87a94bde0dac0e6ec7c35bf538e2ff8a19d3527b8d6b944fe87bb`
   - Package: `com.navigator.freedom.v3final`; `versionCode=40043`; `versionName=4.0.43-APPROVAL-ROUND`.
   - 기존 서명 인증서 SHA-256: `bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00`.
   - APK 내 필수 ASCII 참조 5개 SHA-256, `classes.dex`, `assets/index.html`의 JS 로더와 `referencePack.scoreRaw(primary.task.raw)` 코드 존재 확인.
   - 결과는 **동일 서명·자산 탑재 검증**이지, 실제 사용자 승인 상태·WebView 동작·분석 품질을 검증했다는 의미가 아님.

## 변경하지 않은 대상
- `main`, 기존 V4.0.41 공개 APK/고정 Drive URL, 승인 데이터베이스, 기존 이용자 상태, 결과전 구매조합/잠금, V3·V33·V7 소스/배포.
- 과거 성공사례 결과후 번호를 결과전 분석에 넣거나 당첨 전 기록으로 소급 표시하지 않음.
- 선장 명시 승인 없이 정식 공개 배포/실기기 강제 업데이트/이용자 재승인 수행하지 않음.

## 다음 작업 1순위
1. **선장 승인 정본 4종의 누락 여부/버전 대조**: 특히 정확한 `백학설명서`와 `성공사례` 전체 자료 여부를 다시 대조하고 원문 문장관계 규칙 우선순위를 실제 엔진 해독 경로에서 검증. 단어 색인 연동 ≠ 백학 해독 엔진 완성.
2. **실동작**: 현재 원문 있음/없음, 과거 DB-1 공동 분석과 20조합 결과 차이, 결과전 잠금 불변, 최근 5회 제한, 승인 완료→업데이트 재요청 0건 / pending 중복 0건 / 신규 최초 승인 정상.
3. 선장폰에서 기존 V4.0.41 데이터 보존 업데이트 시험, APK 서명·패키지·버전·기기 상태 재확인. 이 검증 전 공식 Drive APK/설치 버튼을 교체하지 않음.

새 대화방 시작 문장: **"연락관 2026-10-27 V4.0.43 signed candidate 작업대장을 확인하고, CI 40423882784와 APK SHA-256부터 대조한 뒤 미검증 정본·실기기 단계부터 이어가십시오."**
