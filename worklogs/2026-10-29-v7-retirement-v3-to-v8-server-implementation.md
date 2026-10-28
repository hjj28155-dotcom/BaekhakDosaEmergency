# ⚓ 2026-10-29 V7 전체 폐기 + V3 R60 → V8 서버연결 실작업 대장

## 선장 지시
- “V7 삭제하세요” → “모두요” → “진행하십시오.....왜 v3와 서버연결이 안돼는 건데요”.
- V7 전용 UI·앱·자료·서버 기능 전체 폐기 대상. 단, 공유 서버에서 V3/V4/V8에 필요한 자료와 인프라는 구분하여 보존.
- 원본 데이터 삭제 전 **복구 가능한 외부 백업 + 의존성 확인** 필수. 기존 연구자료를 새 결과전 실적으로 위장하지 않음.

## V3 연결 차단 원인 — 원본 APK와 서버 직접 확인
- V3 R60 원본 APK (Drive 파일 ID `1VvRoYFiZ9WORbTGp2MNyuOTvtsN7am3S`) 다운로드·SHA-256 검증: `87ccc77af2a1bd2e5a3b19f31ef84e5e21eccffda717daf2e183c726f9f58026`.
- 소스 ZIP (Drive 파일 ID `1yO8Hp5vAT3CtZOTwS6TjabRDrW0CuEb7`) 안의 `apk_resources/assets/engines/nav_v3_r30_fresh20_central_outbox.js` 실제 코드 확인: IndexedDB `shipmate_v3_r53_fresh20_epoch1`에 `centralOutbox`을 `PENDING_UNSENT`로 저장. 자동 HTTP 전송/ACK/markVerified **없음**.
- APK classes.dex 문자열 검사: 승인·업데이트·로또조회용 `HttpURLConnection`은 존재하지만 V3 결과전 업로드 엔드포인트/WorkManager 전송기 미발견. 문자열 검사만으로 모든 경로 부재를 증명하지는 않으나 원본 JS 및 인계서와 일치.
- 기존 V6 업로드 서버 `v6-room-library-sync` v14 ACTIVE. 기존 V7 `v7-control/ingest`는 extra_navigator를 `EXTRA_ROOM_FILE`로 제외.
- 등록 기기 3건 중 1건만 유효, 명칭은 “항행의자유 V7”; V3 전용 인증 등록 확인되지 않음.
- `v6_room_assets`의 extra_navigator 16건은 V7 과거 RESULT_AFTER. V3 명칭의 업로드 0건, `v7_archive_entries` 항해사방 result_before 0건.

## 실제 완료 — 서버 독립화 (기존 V7 데이터 무변경)
- GitHub 분리 브랜치 `feature/v8-v7-retirement-v3-link-20261029`.
- 신규 `public.v8_v3_resultbefore_receipts` 테이블 생성, RLS enabled, 일반 사용자 접근 불허, 최초 등록 불변 트리거, 고유 (origin_app,epoch,round_no) 및 asset_id, `official_predraw_certified=false` 강제. 초기 접수 0건.
- 신규 Edge Function `v8-v3-resultbefore-receive` v1 ACTIVE, `verify_jwt=false` 대신 기존 단말별 64-hex bearer SHA-256 인증, 원본 Storage 파일 재다운로드 및 해시 확인, 4방20조합·RAW6 해시·중복·출처·결과후 혼입 차단, 동일 영수증 재시도/충돌 409, 인증된 GET receipt 재조회 구현.
- **V7 테이블이나 Edge Function을 새 수신기가 호출하지 않음.** 기존 V6 공용 Storage 및 단말 등록만 재사용.
- GitHub Actions [수신기 헬스/401 인증검증](https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40717958588) **성공**. 공개 health 응답, 무인증 POST/GET 401 확인. 실제 유효 단말 업로드·ACK는 미시험.

## 실제 완료 — V8 UI 스테이징
- 홈의 V7 카드 및 V7 원본방 버튼 제거. V3·V4·V8 + “V3 서버 연결” 2x2 카드 구성.
- V8 홈 업데이트 버튼 노출. **정식 APK 업데이트 URL 미검증으로 버튼은 현재 배포상태 안내만 제공하며 실제 설치를 실행하지 않음**.
- V3 수신기 health 점검 버튼 및 단말 미연결 안내 추가. 원본방 진단 3개로 조정.
- 360×740, 390×844, 412×915 Chromium 인라인 미리보기: JS 오류 0, 세 크기 모두 주요 메뉴 한 화면, 원본방 주소 설정 및 연결 진단 버튼 확인.
- V8 스테이징 Android 버전Code 803, 버전Name `8.0.3-V7-HOME-RETIRED-V3-RECEIVER`; CI 빌드 작업 [40718570056](https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40718570056) 시작. **완료 판정은 CI 결과 확인 후**.

## 미완료·차단
1. **V3 자동송신은 아직 미구현**: V3 R60 동일 인증서 개인키/네이티브 Android 소스 확보, WorkManager 및 Keystore 단말별 인증, native outbox, HTTP upload→V8 receive→GET receipt→local SENT_VERIFIED 구현 후 실제 선장 휴대전화 시험 필요. 기존 R60 APK 삭제·초기화 금지.
2. **V7 전체 삭제 미완료**: V8 하위 누적DB·7방 점수·보고서 및 기존 V7 원본 링크/코드 의존성 제거와 6방 계약 재검증 필요. 이번 단계는 **V8 홈 노출 제거**만 완료. V7 Edge Function/DB/Storage 삭제·토큰 폐기 0건.
3. V7 서버 데이터와 Storage에 대한 외부 백업 및 복구 리허설 미완료. 같은 Supabase 프로젝트에 V3/V4/V8 공유 자료가 있어 프로젝트 전체 삭제 금지.
4. `public.v7_jotasu_transfer_queue` RLS disabled 보안경고 유지. 공유 API와 RLS 정책 의존성 점검 후 안전 조치 필요.
5. V8.0.3 스테이징 APK는 아직 선장 휴대전화 설치·실제 데이터 연동 검증 전이므로 공식 배포로 승격 금지.

## 다음 1순위
- V8.0.3 CI APK 빌드 결과 확인.
- V3 동일서명 업데이트 구현을 위한 Android 원본/서명 접근 확인과 단말 등록.
- V7 데이터 외부 백업·공유 의존성 분리 후 실제 V7 전용 삭제 단계.

## 2026-10-29 후속 검증 및 차단 해소
- V3 R60 APK 실제 `assets/engines/nav_v3_r39_official_draw_readonly.js`에서 `BASE=https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/v7-confirmed-draw-readonly` **하드코딩 확인**. 따라서 V7 Edge Function을 즉시 삭제하면 V3 결과후 자동 채점도 끊김. V3 동일서명 APK에서 V8 URL로 교체해야 함.
- V7 등록 당첨번호 17건(1~16, 1247)을 **V8 독립 `v8_draw_references` 테이블로 읽기전용 이관**. 원본 `v7_official_draws` 변경·삭제 0건, 독립 검증된 사실로 오인하지 않도록 `independently_source_verified=false`.
- 신규 `v8-confirmed-draw-readonly` Edge Function은 Supabase 프로젝트 함수 수 제한으로 배포 거부. 기존 `v8-v3-resultbefore-receive` v2의 공개 읽기 전용 `GET /draw?round_no=N`으로 통합하여 함수 추가 없이 해결.
- [V8 수신기 v2 /draw·401 검증 CI](https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40719708584) **성공**: 제1회 레거시 참조 6번호 조회, 1246회 미등록 404, 무인증 POST·영수증 GET 401.
- V8.0.3 Android 스테이징 빌드: 신규 브랜치 push 실행에서는 GitHub Actions 서명 캐시가 브랜치 범위 때문에 복원 실패(보안키 대체 생성하지 않음). 원본 서명 캐시가 접근 가능한 **초안 PR #4** 경로로 빌드하여 [GitHub Actions run 40718913986](https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40718913986) **성공**. 동일 서명 SHA-256 인증서 검증.
- 스테이징 APK 6,591,000 bytes, SHA-256 `02b1b5eda86d72e623b1cd4fa6951fb094f351a954b2f152e9f7d338415cf197`. APK에서 추출한 HTML/CSS/JS로 Chromium 360/390/412px 화면 및 기본 메뉴 이동 재검증: JS 오류 0, 한 화면 배치 PASS.
- **주의:** V8.0.3은 V7 **홈화면 카드와 바로가기만** 제거한 단계. 누적DB/성적표·보고서 내 V7 레거시 참조 및 V7 서버 기능은 아직 남아 있음. 따라서 V7 전체 삭제 완료, V3 실기기 자동연동 완료, 공식 최종 APK 배포 완료를 주장하지 않음.
- 원본 GitHub 브랜치 `feature/crew-meeting-standalone-apk-20261025` 변경·병합 없음. 초안 PR [#4](https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/pull/4)는 **미병합**.

## V3 R60 클라이언트 소스 패치 준비 (APK 미설치)
- 기존 `nav_v3_r30_fresh20_central_outbox.js`를 수정하지 않는 별도 `v3_r60_v8_transfer_adapter.js` 제작: 대기 보고서 JSON 바이트·SHA-256 추출, 네이티브 인증된 서버 GET 영수증 대조 후 `centralOutbox` 상태만 `SENT_VERIFIED` 전환. 최초 분석·잠금 불변.
- Node.js 모의 IndexedDB 테스트 PASS: 잘못된 해시 거부, 20조합 최초잠금, 정상 형식 ACK 후 상태 전환, 중복 ACK 멱등성, 원본 보존, 중복 잠금 거부. **실제 단말·서버 업로드 시험 아님**.
- 로컬 배포 산출물 `/mnt/data/V3_R60_V8_연동_개발패키지_20261029.zip`, SHA-256 `312a93bc35547248e9ae30d68d149dcbe8e5929ea307b97cd6f63052b42e95ee`. 서명키나 토큰 포함 없음. 동일서명 V3 APK는 아직 생성하지 못함.
