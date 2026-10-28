# ⚓ 항행의자유 중앙통제실 — 2026-10-28 V1.0.30 개발·서버·구글드라이브 작업대장

기준: 선장 지시 "완료하고 말하세요". 기존 V3/V4/V7 원본, DB-1, 최초 잠금, 공식 성적·배포 이력은 삭제·초기화하지 않는다.

## 이번 작업 완료 (직접 검증한 범위)

### 1. 서버 결과전 / 결과후

- Supabase 프로젝트 `jyqkohpeowkhuaohlezc`.
- `crew-six-room-before-feed` **v5 ACTIVE**: 기존 V4 R46 실제 수신증을 `crew_engine_result_receipts`에서 읽어 중앙 결과전 메타로 연결. v4 수정 중 동일 함수 내 중복 `let q` 선언을 v5에서 수정. 서버 DB-1 원문 SHA-256, 5조합 유일성, 수신증 canonical SHA-256 검증 후 `RETROSPECTIVE_R46_DB1_RECEIPT_VERIFIED`로 표시. 공식 결과전 승격 금지.
- `crew-seven-room-research-scores` **v2 ACTIVE**: 신규 읽기 전용 서버. 인증된 선장 키 요구, 서버 DB-1 SHA·수신증 SHA·공식 당첨번호 검증, 원본 미수신은 0점 아닌 미집계. 첫 번째 검증 수신증 우선으로 중복 연구 집계 방지. 회차별 5조합 사후 대조 및 7방 누적 연구 표본의 8등급(0/6~6/6) 합산. 최대 3,000 수신증 페이지 조회, 실제 수신증의 회차만 DB-1 원문 조회. 과거 연구만 다루며 공식 구매 잠금·미래 당첨 확률과 별도.
- 두 Edge Function 배포 후 버전/상태/소스 재조회 성공. 데이터 INSERT/UPDATE/DELETE 없음.
- **실제 제1회차 확인**: V4 R46 5조합 1건. DB-1 원문 SHA 일치, 5조합 확인. 공식 당첨번호 10,23,29,33,37,40 / 보너스16과 SQL 독립 대조 결과 1/6 두 조합, 2/6 세 조합(총 5개 연구 조합). **실전 추첨 전 예측 성적 아님**.
- V4 연락관 R1 독립 사전원본은 0건. V3 4방 및 V7 통제관방의 검증된 신규 자동송신은 미확인. 원본을 임의 생성·복제·다른 방으로 전용하지 않음.
- V7 `captain` 보관본 1~16회차 결과전/결과후는 별도 원본으로 보존; `controller` 5조합으로 재분류하지 않음.

### 2. 앱 UI 및 코드

- GitHub 저장소 `hjj28155-dotcom/BaekhakDosaEmergency`, 브랜치 `feature/crew-meeting-standalone-apk-20261025`.
- APK `versionCode=130`, `versionName=1.0.30-CAPTAIN-NAVAL-RESEARCH-SCORES`, package `kr.hanghae.crewmeeting.compact`.
- 중앙통제실 홈에 범선·바다·등대 SVG 기반 항해 테마, 금색 테두리, V3/V7/V4 독립 색상. 기존 V3 앱 UI는 변경하지 않음. **원본 참고 이미지 40225.png를 현재 세션에서 확보하지 못해 설명에 기반한 독립 디자인이며 원본 이미지와 픽셀 동일성 미검증.**
- 결과전 피드에서 R46 검증된 과거 연구 수신증을 명시적으로 구분.
- 결과후에서 서버가 검증한 조합·당첨번호만 표시하고 5+보너스 별도 구분. V7 captain 과거 보관 원본은 참고 섹션에 별도.
- 총 누적 성적은 7방 중 실제 검증 연구 원본이 있는 방만 연구 표본으로 계산, 나머지는 미집계(0점 아님). 성공 기준 변경 시 비율 재계산.
- APK 내부 "업데이트" 버튼은 Google Drive 기존 고정 파일 ID `1NtFkXk2HPqUOPsp2K-as29r9SHgKJmYc`로 수정.

### 3. 빌드·검증

- GitHub Actions 빌드 **40701614184 SUCCESS** (2026-10-28 UTC), artifact ID **12822725263**. 기존 서명키 지문을 검증하고 APK debug 빌드 및 `apksigner verify`, package/versionCode 검사, 원문 DB-1 1247회 SHA 검사, 기존 모듈·JS 검사, artifact SHA 검사 통과.
- APK **6,515,075 bytes**, SHA-256 `65ee00eceec28dc5df1a09af2ca5604ec922818d479ff0b03188cc7f8678f634`.
- 실제 APK ZIP에서 manifest versionName, HTML 결과전·결과후 점검, JS 인라인 4개 및 외부 JS `node --check` 통과.
- Chromium 브라우저 390×844 홈 배경 렌더 확인(범선/등대/파도, JS pageerror 없음). 320×600 / 390×844 / 412×915 연구 성적 UI 모의 응답 검증: 5조합, 1/6 2개, 2/6 3개, V3 미집계, 3/6 이상 0.00% 모두 통과. **모의 응답 기반 UI 테스트이며 선장 인증된 실제 서버 HTTP GET 또는 Android 실기기 테스트가 아님.**
- 실제 Android 단말 설치/갤러리 PNG 저장/배경 표시/선장 인증 로그인은 미검증. 앱을 자동 설치했다고 주장하지 않음.

### 4. Google Drive 동일 파일 업데이트 완료

- 기존 고정 URL `https://drive.google.com/file/d/1NtFkXk2HPqUOPsp2K-as29r9SHgKJmYc/view?usp=drivesdk`.
- 기존 Drive 파일 ID에 `files.update` 방식으로 새 APK 바이트 **직접 덮어쓰기 성공**, 파일 ID·URL·상위 폴더 유지.
- 현재 Drive 파일 메타데이터 **6,515,075 bytes**, 수정시각 **2026-10-28 14:04:26 UTC**, revision 3개(이전 revision 보존).
- Drive에서 새로 다운로드한 실제 APK와 CI 산출물 바이트 단위 동일 및 SHA-256 동일. manifest 1.0.30, 항해 테마, 결과후 점수 API 연결, 업데이트 링크 자체 참조 검증.
- 별도 스테이징 Drive 파일 `1KX1o6hHtop0SMXQgjYGwW7-Ydyis7IWf`도 보존. 기존 파일이나 다른 앱 삭제 없음.

### 5. 기존 DB·보호대상 최종 확인

- `v6_round_sources=1247` (DB-1 유지), `v7_official_draws=17`.
- `crew_engine_result_receipts=1`, `v7_first_reports=16`, `v7_grading_receipts=16`, `v7_cumulative_verification=0`, `v7_library_items=0`.
- 실제 V3·V7 통제관·R1 원본이 없는 상태에서 전체 35조합 결과전/결과후·누적 실적을 완성했다고 주장하지 않는다.

## 남은 외부 의존 작업 (미완료로 명시)

1. 선장 기기 내 V3 4방 / V7 통제관방 / V4 R1 독립 분석의 실제 원본·잠금·전송 경로 확인 및 인증된 수신. 원본 없는 조합은 생성하지 않는다.
2. 선장 인증된 실제 서버 GET 응답 및 Android 단말 앱 설치/화면/PNG 갤러리 저장 종단간 테스트.
3. 제공됐던 `40225.png` 원본과 화면 픽셀 비교. 이번 항해 테마는 설명 기반 디자인임.
4. 독립 원본이 실제 수신된 후 7방 35조합의 공식·연구 분리 채점과 누적 실적 완성.

**최종 상태:** V1.0.30 빌드·서명·Drive 동일 링크 업데이트, 서버 읽기 전용 R46 결과전/결과후 연구 점수 연결 및 브라우저 모의 테스트까지 완료. 누락된 다른 방 원본·실기기 검증은 완료 아님.
