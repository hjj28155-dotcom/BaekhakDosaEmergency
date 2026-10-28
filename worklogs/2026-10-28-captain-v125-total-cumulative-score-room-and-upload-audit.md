# ⚓ V1.0.25 선장 중앙통제실 — 총 누적 성적 결과방 + 자동업로드 실사

## 선장 지시
2026-10-28: “0/6...............5/6.5/6(보너스).6/6 총회수 백분율 총성공확률 이 방이 없어요. 그리고 v3.v4.v7 자료실 업로드가 안돼고 있어요...자동연동 맞아요”
V1.0.24에는 회차별 결과후 7방 35칸만 있었고 누적 채점 집계방이 없었음. 자동업로드도 검증되지 않음.

## 코드 변경 / 보존
- GitHub `hjj28155-dotcom/BaekhakDosaEmergency`, branch `feature/crew-meeting-standalone-apk-20261025`.
- `captain_home_v114.html`: 홈에 **🏆 총 누적 성적 결과방** 독립 버튼, 통합 누적DB 화면 내 **총 누적 성적 결과방** 바로가기, 별도 `cumulativeScoreDashboard`. 0/6·1/6·2/6·3/6·4/6·5/6·5/6+보너스·6/6의 8등급, 등급별 회수와 백분율, 채점 완료 총회수, 총성공확률, 성공기준 3/6·4/6·5/6·6/6 선택, 전체+7방 개별 합계, 3앱 연결 점검. 뒤로 가기에서 7방 회차별 결과후로 복귀. 원본 보존.
- `cumulative_after_only.css`: 모바일 320~412px 반응형 8등급 4×2 카드, 7방 성적 목록 스크롤, 서버 상태.
- `cumulative_after_only.js`: `captainCumulativeScoreRender`는 기존 읽기 전용 3앱 결과후 메타 API로 연결 상태만 점검. **원본 6번호가 없는 동안 모든 등급/회수/백분율/성공률은 — 또는 미집계로 유지**, 누락=0점 금지. 3앱별 연결 실패를 독립 표시. 성공 기준 선택만으로 가짜 수치 생성 금지.
- `MainActivity.java` app_version=125, `app/build.gradle` versionCode 125 / versionName `1.0.25-CAPTAIN-TOTAL-CUMULATIVE-SCORES`; 기존 package 및 서명 그대로. GitHub Actions 검증에 8등급·8합계카드·성공확률·연결상태 검증 추가.
- 기존 V3/V7/V4 원본 APK·서버·DB·결과전/결과후 기록·공식 배포 URL 삭제·초기화·변경 없음.

## 서버 실제 조사 (Supabase project jyqkohpeowkhuaohlezc)
- `crew-three-app-materials` Edge Function v1 실제 소스 확인: `raw_predictions_included:false`; V3 및 V4는 `linked:false`, 이유 `SOURCE_CONNECTOR_REQUIRED`. V7은 `v7_grading_receipts` 등의 메타만 읽으며 원본 6번호/실제 점수는 제공하지 않음.
- `crew-six-room-before-feed` v3 실제 소스 확인: `source_submission_connected:false`, `actual_app_auto_push_verified:false`; V3 실제 자동송신기 검증 안 됨. V4 원본 업로드 0건.
- DB SQL 직접 확인: `v7_first_reports` 16건, `v7_grading_receipts` 16건, `v7_after_report_jobs` 128건, `v7_cumulative_verification` 0건, `crew_v4_research_reports` 0건, `v7_library_items` 0건, `v7_archive_entries` 6건, `v6_round_sources` 1247건. **작업 큐 128건을 실제 업로드/채점 완료로 오인 금지**.
- 보안 별도 점검: `public.v7_jotasu_transfer_queue` RLS disabled, row count 0. 보안 정책 승인 없이 자동 수정하지 않음. 선장에게 고지 필요.

## 빌드 및 검증
- GitHub Actions **40661715263 SUCCESS** https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40661715263
- 아티팩트 ID **12803712363**, 이름 `HANGHAE-CREW-MEETING-1.0.25-TOTAL-CUMULATIVE-SCORES-APK`.
- APK `HANGHAE_CREW_MEETING_1.0.25_TOTAL_CUMULATIVE_SCORES.apk`, **6,499,166 bytes**, SHA256 **97b7814e21c2a983085c01d7db4600e709f0300cee01b0309ead55176132f863**. ZIP CRC/sha256 PASS. 기존 signing certificate CI PASS.
- APK 실제 HTML/CSS/JS 추출, Chromium Playwright **320×600 / 360×700 / 390×844 / 412×915**: 8등급, 8개 전체+방별 합계, 미집계·—% 표기, 화면 너비 overflow 0, 페이지 JS 오류 0, 성공기준 변경 후 —% 유지, 누적 성적↔회차별 결과후 이동 성공.
- 모의 API: V3 linked:false / V7 2건 메타 only / V4 Failed to fetch → 앱별 연결상태 정확히 구분, 가짜 누적 통계 없음, JS 오류 0. 인증되지 않은 화면에서는 기존 선장 로그인 게이트 표시.
- Google Drive ZIP 업로드/읽기 검증: https://drive.google.com/file/d/1C0lnJRPVVbdiKPGCMWKtvob64hlhTAx1/view?usp=drivesdk

## HOLD / 다음 단계
1. **V3/V4/V7 자동업로드 미완료**. 각 원본 앱에 실제 발신기 및 인증된 서버 수신/원본 read-back이 있어야 함. 중앙통제실의 읽기 전용 API만으로는 자동연동이 아님.
2. V4 선장엔진 5조합·연락관엔진 5조합은 독립 출처·회차·엔진 실행 증빙 필요. DB-1 1247건은 분석 원본이지 결과후 35조합 아님.
3. 7방×회차별 6번호+공식 당첨번호+보너스+중복 제거 후 0/6~6/6 및 선택 성공기준 누적 계산/검증 구현 필요. 아직 실제 수치 계산 불가.
4. Android 실제 선장 휴대전화 설치·로그인·화면·자동송신 실기기 검증 미실시.
5. RLS disabled `v7_jotasu_transfer_queue`에 대한 승인받은 정책 설계·적용 필요.
