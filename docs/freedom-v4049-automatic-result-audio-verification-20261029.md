# 항행의자유 V4.0.49 — 자동채점 빵빠레·축하·낙선 음성 인계 기록

## 선장 지시 기준
- 2026-10-29 KST. 일반 배포판 V4, Android `com.navigator.freedom.v3final`. V3 항해사앱 및 V7 중앙통제실과 별개.
- 20조합 5×4 묶음 **한 화면 표시**, 상하단 **홈 버튼**, 신규설치·업데이트 링크 **각각 독립 유지**.
- 조합표에는 불필요한 낙선 반복 표시 없이 **당첨 시 빵빠레+축하멘트**, 당첨 조합 없을 때 **낙선·격려멘트**를 유지.

## V4.0.49 분리된 수정 소스
- 브랜치: `fix/freedom-v4049-score-voice-fanfare-20261029`, V4.0.48 기준.
- 변경 파일은 일반 배포판 소스 `build405/FREEDOM_V402_BUILD_THIS/app/src/freedom/assets/index.html`, 모바일 CSS `v4047_compact.css`, `app/build.gradle` (`versionCode 40049`), 회귀검사 `.github/scripts/verify_freedom_v4046_emergency.cjs`.
- **기존 음성 및 시각 효과 재사용**: `showFanfare`, `showComfort`, `successVoiceText`, `comfortVoiceText`, `playFanfareTone`, native `playCelebrationWithVoice`/`speakConsolation` 및 브라우저 SpeechSynthesis 백업.
- `v4049Announce(round,force)`: 공식 내장 또는 확인된 당첨번호와 저장된 20조합이 모두 있을 때만 발표; 발표 전에는 침묵. 20조합과 당첨번호에 따른 **세션내 중복 팝업 방지**, 명시적 `🔊 다시듣기` 지원.
- `v4049ScoreSummary`: 적중 및 보너스에 따라 정식 1~5등을 평가; **5개+보너스가 2등**인 경우 기존 음성도 2등으로 읽도록 기존 팡파레 함수의 선택적 `rankOverride`를 확장.
- `analysisResearch`는 사후 분석조합이므로 구매실적처럼 축하/낙선 음성을 자동 재생하지 않음. 실제 원본이 없는 경우 아무도 당첨 전 조합으로 재구성하지 않음.
- 음성 오버레이는 CSS `z-index:2147483647`로 20조합 모달보다 앞으로 나오게 변경. 조합표 자체는 `v4047Compact` 2×2 배치 및 4개 하단 버튼을 그대로 사용.
- 설치 페이지 `hanghae-freedom-distribution.html`, 공식 Supabase 업데이트 `latest-apk`, V3, V7, 원본 DB-1·최초잠금·기기 승인 기록 미변경.

## 회귀검증
- 인라인 JavaScript 6개 정적 구문 검사 통과.
- 별도 검증 스크립트는 **원문 없는 1248회=1~1247 전체 분석**, 원문 있는 1248회 가중치, 20조합 4묶음, 최신 5회, 2등 판정, 최초잠금 불변, 미발표회차 음성 침묵, 사후 연구 음성 제외, 빵빠레/낙선 음성 분기, 명시적 재생, 재방문 중복 방지를 검사.
- 최초 CI 실행 `40725189724`는 **테스트 자료가 낙선 조합 하나에 5개 숫자만 들어간 오류**로 중단(실제 소스의 조합 수 불변 검증 기능은 정상). 테스트 데이터만 6개로 수정했고 CI 재실행: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40725675400
- 빌드/서명/아티팩트 결과는 **CI 최종 결과 확인 후 별도 추가**. 실제 휴대전화 설치·음성 청취는 완료로 보고하지 않음.

## 남은 작업
1. CI 성공 확인: 40049, 기존 원본 서명 `bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00`, APK 내 동일 소스·compact CSS·음성 함수 확인.
2. 아티팩트 APK 추출, zip·SHA256 검증, 시험판으로만 제공.
3. 안드로이드 실기기에서 실제 소리/음량/음성 겹침 및 20조합 한 화면 확인.
4. 선장 승인 전 V4 신규설치와 업데이트 공식 링크 변경 금지. 어떤 자료/원본/설치도 삭제·초기화 금지.

## 최종 V4.0.49 시험용 APK 검증 추가 (2026-10-29 KST)
- Android **최종 빌드 소스 고정 커밋** `42029565d404b2a5b14fdd2898174cfcad28b5bc`.
- 원본 서명 CI 재검증 최종 **성공**: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40726475588
- GitHub Actions 아티팩트 `Freedom_V4049_VOICE_20_OriginalSigned_TEST_ONLY`, ID `12833685809`.
- 최종 APK 원본 바이트 수: **48,011,015**.
- 최종 APK SHA-256: **`bf5b6d4b5f5ba107287adb73a17f533a612a35741e3de23f992e6e2603cd18e7`**. CI `SHA256.txt` 기대값과 추출 APK 실제 재계산값 일치.
- Android 패키지 `com.navigator.freedom.v3final`, `versionCode=40049`, `versionName=4.0.49-EMERGENCY-SCORE-VOICE`.
- Android 서명 인증서 SHA-256은 기존 검증된 `bf281903f422fe925a23a7e72e82d690eaef899236e6d10365143ff32d9beb00`와 일치. 신규 서명키 생성 없음.
- ZIP/APK CRC 검사 통과; `assets/index.html`, `assets/v4047_compact.css`, `assets/raw_bank.js`, `assets/db1_v1247_patch.js`, `assets/freedom_reference_v4043.js`, reference manifest, 엔진 검증.
- Android `MainActivity.java`의 `@JavascriptInterface playCelebrationWithVoice`, `speakConsolation`, `stopScoreVoice`와 웹뷰 브리지의 `AndroidHost`/`BaekhakNative` 연결 소스 확인.
- 최종 APK 실바이트 기반 Chromium(Playwright) 렌더검증 **320×600, 360×640, 360×740, 392×852, 704×1440 5종 전부 통과**: 4묶음/20줄/공과 적중표시 겹침 0/아래 홈 버튼 화면 내/🔊 다시듣기 표시/낙선 오버레이 상단 출력. **넓은 세로화면에서 아래 버튼 6px 넘침** 발견 후, `@media(min-width:641px)`에서 24px 바닥 여유를 두어 최종 APK 재빌드한 결과 해결.
- **실제 선장 휴대전화의 음성, 음량, 설치 후 저장 데이터 보존 검증은 미완료**. 본 파일은 **TEST ONLY**.
- V4 신규설치 URL 및 공식 업데이트 함수/URL, V3/V7 원본, 최초잠금/DB-1/기존 사용기록 **미변경**. 선장 실사용 확인 및 명시적 승인 없이 정식 배포 경로 변경 금지.
