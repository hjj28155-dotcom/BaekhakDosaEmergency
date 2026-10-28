# ⚓ V1.0.28 — 1회차 실제 분석번호의 자료실 송신·조회 연동 (부분 구현, 7방 완성 아님)

## 선장 지시
“현재 선장이 1회차 분석했으면 모두 연동이 되어 1회차 결과는 나와야지요...오로지 원문분석만 나오잖아요”.
기존 V1.0.27: V4 R46 5조합은 기기 화면의 PNG만 생성, 자료실 전송 없음. 통합 누적DB는 `crew-three-app-materials`의 결과후 **메타데이터만** 읽어 6번호를 모두 `—`로 표시. V3 네 방·V7 통제관방·V4 연락관 독립 원본 발신 검증 안 됨.

## 원본 서버 조사 — 절대 혼동 금지
- Supabase `jyqkohpeowkhuaohlezc`의 `public.v7_documents`에 **제1회 `room_key='captain'`, `document_type='V7_PURCHASE_5'` 5조합이 실제 보관**. `received_before_draw=false`, 2026-10-23 등록, 공식 사전 잠금 증거 아님. `room_key='controller'` 원본이 아님. 따라서 V7 통제관방 5조합으로 오인하거나 이관하지 않음.
- `public.crew_v4_research_reports` 0건, `public.v4_sync_external_reports` 0건, `public.v7_cumulative_verification` 0건. V3 4방 원본 송신 검증 안 됨.
- V4 R46 1회차 실제 엔진 5조합은 앞선 V1.0.26 엔진 테스트에서 검증되었으나, 선장님이 V1.0.27 기기에서 실행한 결과가 서버에 업로드된 증거는 **없음**. 이번에 임의로 사전 저장하거나 그 실행을 복원했다고 주장하지 않음.

## 변경
1. Supabase migration `captain_v4_research_engine_receipts_append_only_20261028`:
   - 신규 `public.crew_engine_result_receipts` append-only V4 R46 연구 수신 테이블 (round_no, room_key, engine, source SHA256, 5×6 combinations, report SHA256 unique, research_only=true, official_eligible=false, created_at).
   - RLS enabled; anon SELECT false, authenticated INSERT false, service_role SELECT/INSERT true; 기존 DB/원본/잠금 변경 없음.
2. 신규 Edge Function `crew-verified-round-results` v1 ACTIVE:
   - 기존 중앙통제실 선장방 bearer token의 SHA256을 `crew_meeting_rooms.access_hash`와 비교하는 서버 인증.
   - POST V4 선장 R46만 허용: 1~1247회차, 정확히 5개×6번호, 1~45 중복 검사, 출처/엔진/원문 SHA, DB1 원문 실제 SHA 검증, 동일 보고서 SHA 중복 저장 방지. 공식 결과전 잠금 아님.
   - GET 해당 회차 V4 연구 보고서 읽기; V7 `captain` 5조합은 **별도 역사 보관 참고**로만 반환, `not_v7_controller:true`. V3 4방·V7 통제관·V4 연락관 원본 미연동 상태 명시.
3. GitHub `crew-meeting-android/app/src/main/assets/captain_home_v114.html`:
   - `captain-v4-verified` 이벤트에서 원본 R46 5×6을 선장 인증으로 서버에 POST, 실제 수신확인 기록. `window.captainEngineRoundRead`로 GET. 자료실 버튼을 곧바로 눌러도 in-flight POST 완료 후 GET 하도록 동기화.
   - V7 선장방 별도 보관 5조합 `cumV7ArchiveReference` 펼침, **통제관방 7방 합계에 섞지 않음**.
4. `cumulative_after_only.js`: 통합 누적DB 7방 결과후에서 인증된 V4 R46 수신 보고서가 있으면 **실제 5×6 번호 표시**, 내장 공식 당첨번호로 겹치는 번호 강조·과거 재분석 참고 적중수 표시. 공식 사전잠금/총 성공률에 편입하지 않음. 미수신 방은 기존대로 `—`.
5. Android `versionCode=128`, `versionName=1.0.28-CAPTAIN-ROUND-RESULT-SYNC`, 패키지 `kr.hanghae.crewmeeting.compact`·기존 서명 유지. CI에서 브리지·API 문자열·아카이브 분리 검증.

## 검증
- GitHub Actions **40688806562 SUCCESS** https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40688806562, artifact **12815139053**, `HANGHAE-CREW-MEETING-1.0.28-ROUND-RESULT-SYNC-APK`.
- APK `HANGHAE_CREW_MEETING_1.0.28_ROUND_RESULT_SYNC.apk` 6,508,467 bytes, SHA256 **eaeceb09fb76c36ebf50660a1bed36397226916c13f28ff7d3c7da7fb13a8b38**; ZIP CRC PASS, SHA256 matches included checksum, existing signer CI PASS.
- APK에서 추출한 실제 HTML·CSS·JS, Playwright Chromium **320×600, 360×700, 390×844, 412×915**. 독립 모의 서버 readback에서 V4 R46 1회차 5행×6번호 표시, V7 captain 별도 역사 보관 표시, 화면 가로 넘침 없음, 페이지 JS 오류 0. **모의 응답 시험이지 실서버 인증 POST/GET E2E가 아님**.
- Supabase 신규 테이블 RLS true / anon SELECT false / authenticated INSERT false / service_role 접근 true / 현재 실제 저장 0건 확인.
- Google Drive 기존 **동일 파일 ID** `1NtFkXk2HPqUOPsp2K-as29r9SHgKJmYc`에 APK 바이트를 **덮어쓰기 업데이트** 성공; 다시 내려받아 SHA256 원본과 일치 확인: https://drive.google.com/file/d/1NtFkXk2HPqUOPsp2K-as29r9SHgKJmYc/view?usp=drivesdk
- APK 로컬: `/mnt/data/항행의자유_중앙통제실_선장전용_V1.0.28_1회차_자료실연동.apk`.

## HOLD / 중요 고지
1. **V3 4방 20조합·V7 통제관방 5조합·V4 연락관 R1 5조합 자동 송신 미완료.** V7 captain 과거 5조합은 통제관 원본으로 대체 불가. 7방 전부 35조합 동시 표시 아직 불가.
2. V1.0.27 기기에서 이미 수행한 R46 분석 결과는 서버에 남아 있지 않으므로 **그 실행을 자동 복구할 수 없음**. V1.0.28 설치 후 동일 회차 R46 재분석 시 새 POST 및 readback 시도. 기존 결과 삭제하지 않음.
3. 실제 선장 인증 토큰을 이용한 Android 휴대전화 서버 POST→GET, 기기 설치, 자료실 수신, V3/V7 앱별 송신은 **실기기 미검증**. 서버 endpoint ACTIVE와 DB RLS, CI·모의 UI까지만 검증.
4. 과거 제1회차 재분석 적중은 **연구 참고치**이며 공식 추첨 전 잠금·실전 성적 0/6~6/6 총계에 편입하지 않음.
5. 다음 단계: V3 원본 앱 4방 실제 발신기, V7 통제관방 독립 원본 송신, V4 연락관 독립 엔진 사전원본 검증, 인증 수신증/원본 readback, 7방 누적 집계.
