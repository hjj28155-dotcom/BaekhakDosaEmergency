# 항행의자유 **일반 배포판** 비상용 V4.0.46 · 제1248회 자동 20조합 인계서

## 선장 최종 기능 지시 (2026-10-29 KST)
- V3 항해사앱 / V7 중앙통제실이 아닌 **항행의자유 일반 배포판**. 앱 패키지 ID \`com.navigator.freedom.v3final\`.
- **당회차 포함 최근 5회만 조회:** 현재 1248회를 기준으로 1244~1248회. 과거 DB-1 원본 범위를 줄이라는 뜻이 아님.
- **1248회 원문이 없으면** 내장 DB-1 1~1247회 전체 6구절 원문을 분석해 5조합씩 4묶음, 총 20조합 생성.
- **1248회 원문이 있으면** 1248회 원문을 **주 분석자료**, 1~1247회 전체 원문을 **보조자료**로 활용해 20조합 생성.
- 당첨번호는 발표 후 연결하여 저장 20조합을 개별 채점. 발표 전 당첨번호를 만들어내거나 과거 결과를 주분석에 주입하지 않음.

## 코드 기준과 변경
- 격리 작업 브랜치: \`fix/freedom-v4046-emergency-corpus-1248-20261029\`.
- 시험·빌드 고정 커밋: \`ab96af71c4bfc8de54ee344520e689c6cdf3f6da\`.
- V4.0.46: \`versionCode 40046\`, \`versionName 4.0.46-EMERGENCY-CORPUS-1248\`.
- app 코드: \`build405/FREEDOM_V402_BUILD_THIS/app/src/freedom/assets/index.html\`; Android 설정: \`build405/FREEDOM_V402_BUILD_THIS/app/build.gradle\`.
- 핵심 변경:
  1. 기존 1247 상한선을 1248로 확대; 이전 \`D.rounds\` 자료 변경 없이 활성 회차 포인터만 갱신.
  2. 1248 원문은 **선택 입력**. 입력/수정 6구절은 \`D.rounds['1248'].raw\`에 저장, 이전 원문은 \`rawRevisions\`에 기록. 원문 등록 변경 시 기존 분석을 \`analysisVersions\`에 보존하고 재분석(잠금 시 재분석 차단).
  3. 1~1247회 \`v4043Db1Text\`와 \`FREEDOM_REF_V4043.scoreRaw\`를 12회차 단위 배치로 실제 **전수 처리**. 분석 진행률 \`n/1247\` 화면 표시. 원본 참조 사전 및 DB-1 PASS 확인 없이 임의번호 생성 금지.
  4. 제공 원문이 있으면 주원문 70%, 과거 전체 참조 분석 30%의 **초기 운영 가중치** 적용. 주원문이 없거나 사전 대응 점수가 0이면 과거 자료 100%. 45개 수의 참조점수를 정규화하여 상위 후보 12수 선별, 기존 \`v123Auto20FromUniverse\`로 중복 없는 20조합 생성. **70/30은 선장이 직접 지정한 수치가 아니라 구현 선택**으로 추후 조정 가능.
  5. \`v330GroupsPurchase\` / \`v330GroupsResult\` 5조합×4묶음 렌더; 당첨번호·보너스와 비교하여 1~5등 또는 낙선 표시. 1248회 발표 전에는 당첨번호 확인 대기.
  6. \`V4046_VISIBLE=[1244,1245,1246,1247,1248]\`로 화면 선택 5회 제한. 기존 원본 1~1247회는 삭제하지 않음.
  7. 응답시점의 \`cur()\`를 응답 \`round\`와 직접 비교하던 "조회 회차와 현재 회차가 다릅니다" 경고를 제거. \`V4045_WIN_PENDING\` 요청 회차와 실제 결과 회차를 대조하고 올바른 \`D.rounds[round].score\`에만 저장. 유효하지 않은/다른 회차/공식 내장번호 불일치 결과는 저장하지 않음.
  8. 과거 당첨 발표 후 새로 만든 조합은 \`analysisResearch\`에만 저장하고 \`researchOnly\`, \`historicalResultExcluded\` 적용. 기존 \`buy.locked\`, 결과전 원본, 기존 분석 및 V3/V7 미변경.
  9. V4.0.44의 원문 중심 최근 5회 화면은 잘못된 요구해석으로 **V4.0.46 시험판에서 대체**.

## 검증
- 독립 테스트: \`.github/scripts/verify_freedom_v4046_emergency.cjs\`, 무원문 1247개 전수분석, 입력원문 주분석 70% 재실행, 전원문 전수 1247개, 4×5 조합, 중복 없음, 원본 잠금 불변, 공식 발표 이전 빈 번호, 발표 이후 2등(5+보너스), 과거 사후연구 분리, 최신 5회 표시.
- 기존 보존검사: \`.github/scripts/validate_freedom_r14.cjs\`, \`.github/scripts/verify_freedom_v4043.cjs\`, \`.github/scripts/verify_freedom_v4043_references.cjs\`. 새 기능 JS 인라인 6개 구문 확인.
- **동일 서명 실제 Android 컴파일 성공:** [GitHub Actions 실행 40719501565](https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40719501565), 완료 \`success\` (2026-10-28T15:48:57Z).
- 원래 signer 캐시 \`freedom-v436-production-debug-signer-20261022\`를 기존 캐시 접근 스코프에서 복원; 인증서 SHA256 \`bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00\` 동일. 키 미노출/신규 키 미생성.
- signed test APK 패키지 이름 \`com.navigator.freedom.v3final\`, \`versionCode 40046\`, \`targetSdk 35\`.
- CI ZIP artifact ID \`12828823658\`, 이름 \`Freedom_V4046_EMERGENCY_1248_OriginalSigned_TEST_ONLY\`, 만료 2026-10-31T15:48:53Z.
- 실제 APK 크기 **48,007,925 bytes**, SHA-256 **\`d09f5d640e1e495dcca0cd2d6ea6ff3fb72c5905beb8653fe361225497f59769\`**. ZIP 내 \`SHA256.txt\`과 작업 환경에서 다시 계산한 해시 일치.
- 아티팩트 ZIP 무결성 및 추출 APK 압축 무결성 통과. 내장 \`assets/index.html\`, \`assets/raw_bank.js\`, \`assets/db1_v1247_patch.js\`, \`assets/freedom_reference_v4043.js\`, 9개 runtime 참조파일 manifest, 엔진 파일 존재 확인.
- signed test APK는 **ChatGPT 채팅 첨부 파일로 선장에게 제공**. 실제 기기에 설치/실행되었다고 가정하지 않음.

## 기능 성격 및 검증 한계
- 현 구현의 "전수 분석"은 모든 원문을 \`FREEDOM_REF_V4043\`의 검증된 2026-06 꿈사전/번호 대응 모듈로 읽는 **비상용 참고 분석**. 미검증 공식 해독 규칙 전체를 가동했다고 주장하지 않음.
- 본선 공식 결과전 인증, 최초잠금 승인, 예측 정확도, 휴대전화 실사용 결과를 의미하지 않음.
- 실기기에서 1248회 20조합, 1248회 원문 등록 시 변경, 1244~1248회만 조회, 당첨번호 발표 전 대기, 업데이트 시 기존 기록 보존 확인 필요.
- 기존 배포판 앱 삭제·설정 데이터 삭제·앱 캐시/저장공간 초기화 금지. 설치 시 패키지/서명 불일치 오류가 나면 삭제 없이 보고.
- **정식 자동배포 포인터 \`latest-apk\` / \`latest-apk-info\`는 여전히 V4.0.36**. 실기기 검증 및 선장 승인 전 변경 금지.
