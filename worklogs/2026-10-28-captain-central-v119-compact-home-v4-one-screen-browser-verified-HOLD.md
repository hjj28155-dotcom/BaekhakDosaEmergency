# ⚓ 중앙통제실 V1.0.19 — 홈 한 화면 + V4 독립 5조합×2 화면 검증 (2026-10-28)

## 선장 지시와 범위
- 실제 휴대전화 V1.0.17 사진에서 홈이 한 화면에 들어오지 않고 V4 분석방이 홈 아래로 펼쳐지는 문제 확인.
- V3/V7과 같은 카드 버튼 체계로 V4 `앱 열기` → 전용 `회차 분석` → 제1·제2 원문 5조합씩 한 화면 표시, 분석 후 `누적DB` 이동.
- 불필요한 긴 설명/반복 문구는 홈에서 숨김. 일반 V4 배포판, 기존 고정 Drive 링크, V3/V7, 승인 기록과 누적DB 원본 불변.

## 변경 소스
- 브랜치: `feature/crew-meeting-standalone-apk-20261025`.
- `crew-meeting-android/app/src/main/assets/captain_home_v114.html`
  - 홈: 누적 연구 기준 긴 문구, 중복 설명, 카드 설명, 하단 홈 개요 숨김. 3앱 카드 2버튼 균등 배치. 화면 높이 600px 이하에서는 하단 회차조회만 숨기고 상단 보고서 메뉴는 유지.
  - V4: 홈 아래 확장 방식 삭제. 독립 `homeView('v4')` 모드에 V4 헤더·홈으로·회차 분석·5개×2 결과 자리·상태·1차/2차 점검·기록·누적DB 이동 배치.
  - 기존 main.roomMode CSS 고특이도 :not 선택자가 V4방까지 display:none 하던 결함 수정.
  - 기존 homeView/materialsSelect가 원본 IIFE 내부에 있어 외부 V4 스크립트에서 호출 불가능했던 결함 수정: 허용된 V4/home/materials 화면과 freedom/db만 전달하는 좁은 브리지 `captainV4Route`, `captainV4Select` 사용.
  - V4 카드의 중복 승인관리 버튼 제거, 일반 승인관리 공통 메뉴 유지. 제거한 버튼에 대한 남은 이벤트 리스너도 삭제.
  - **중요: 실제 V4 5조합×2 엔진/DB-1 원문 읽기 연결은 미완료.** 회차 분석은 연결 대기 상태를 정확히 표시하며 임의 번호 생성 없음. 누적DB 버튼은 기존 등록 메타 조회 화면으로 회차를 전달하는 기능만 검증됨.
- 관련 커밋: `aa073d92a39caa332947d542d70aba78059ae3ed`, `fd2a6e54e206fb77403146c976588db5a9fb6cd0`, `517ca9e15d430346d8db6b85ef2034cd3d8f8d2e`.
- 버전: `versionCode 119`, `versionName 1.0.19-CAPTAIN-V4-ROUTE-VERIFIED`; Android 동일 패키지 `kr.hanghae.crewmeeting.compact`.
- 빌드 head `6d9c6350fc1e7008cd4806da855c024b3b43e79d`.

## 검증
- GitHub Actions https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40640773629 — **completed success**.
- 원본 서명 SHA256 `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be`와 동일, 패키지/versionCode CI 확인.
- Artifact ID `12795261900`; ZIP **64,833 bytes**, CRC 정상.
- APK **68,005 bytes**, SHA256 `cee8208e426035e8f11db3ada86e04ae0f04df2908c1600e2bc5e3d270aa5fb0`; .sha256 파일과 일치, APK 내부 ZIP CRC 정상, 번들 HTML 추출 확인.
- **실제 빌드 APK에서 추출한 HTML**을 Chromium/Playwright로 320×600, 360×700, 390×844, 412×915 테스트. 홈/독립 V4 각각 clientHeight=scrollHeight, V4 display=flex, 클릭 후 상태 정확, 브라우저 JS 오류 없음.
- 390×844에서 V4 앱 열기 → 회차 분석 → 누적DB 이동 시 선택 회차 1 유지, 자료실 표시 → V4 복귀 → 홈으로 동작 확인.
- **휴대전화 실기기 검증은 선장님 설치 후 대기**.
- Google Drive ZIP: https://drive.google.com/file/d/126WPVu1-EGzml5rhkrfcPcz_Azrc6vMW/view?usp=drivesdk (업로드 성공, 재조회 64,833 bytes).
- 대화 직접 APK: `/mnt/data/항행의자유_중앙통제실_선장전용_V1.0.19_홈V4_한화면_검증.apk`.
- 기존 고정 Drive APK와 이전 V1.0.16/17 시험본 변경 없음.

## HOLD
1. V4 **실제** 원문 DB-1 읽기 및 원본 엔진 제1분석 5조합 + 독립 제2분석 5조합, 중복 검사/출처 검증/결과전 확정 연동 미완료.
2. 누적DB 버튼은 기존 메타자료 조회 이동만 확인. 원문 DB-1 전체와 실제 엔진 연결은 별도.
3. V3/V7/V4 실제 결과전 자동송신 및 결과후 점수 연동 미완료.
4. 선장 실제 휴대전화에서 V1.0.19 설치, 홈, V4 앱 열기, 회차 분석, 누적DB, V3/V7, 승인관리 확인 필요.
