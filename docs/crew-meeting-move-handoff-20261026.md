# ⚓ 공동회의 — 2026-10-26 새 대화방 이사 인계

## 마지막 선장 지시
- 홈 상단: 항해사 / 통제관 / 중앙연결관. 원본방이라는 이름을 홈 버튼에 표시하지 않음.
- 두번째 줄: 실시간 공유 / 보고서 / 업데이트 / 더보기.
- 상기 **7개 버튼의 높이는 전부 70px**.
- **V3 항해사앱 실행과 V7 중앙통제실 실행 버튼은 홈에 배치**; 두 **연결 변경** 버튼은 화면에서 제거. 기존 Native Launcher 연결 기록은 보존.
- APK 최신 파일은 **구글드라이브 고정 주소**에 올려 선장이 폴더를 찾지 않고 덮어 설치.
- 3개 원본 ChatGPT 대화방의 실제 업무 메시지 송수신이 최우선. 새 모의 AI 응답을 원본방 답변이라고 주장하지 말 것.

## 작업 현황 — 실제 확인
- 공동회의 독립 Android APK **1.0.7-HOME70**, versionCode **107**, package `kr.hanghae.crewmeeting.compact`.
- 빌드 GitHub Actions run **#40257490441 completed/success**. 기존 서명 SHA256 `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be` 검증.
- 고정 Drive 파일 **기존 ID 유지**: `1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL`. 파일 이름 `항행의자유_공동회의_최신업데이트.apk`. 연결된 Drive 실제 파일을 다운로드해 확인: **15,674바이트**, SHA256 `e5e338c28fdf18c8a42d1790634723c4916c782e8ad964247cb94637c0225cec`.
- 직접 업데이트 링크: https://drive.google.com/file/d/1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL/view?usp=drivesdk
- 홈 소스 `crew-meeting.html` GitHub main commit **8577b51c56b0907a370507cbc8dca9671bbf7198**, Pages run **#40258994140 completed/success**.
- 수정 홈 소스에서 `installedAppBar`가 실제 `homeQuickActions` 다음에 있으며 V3/V7 홈버튼 존재, 두 변경 버튼 DOM 제거, 기존 `requestInstalledLaunch('v3',false)` 및 `('v7',false)` 동작 보존.
- 새 APK에 해당하는 Android WebView 주소는 `?app_version=107`으로 최신 HTML을 읽음.
- **선장폰 실제 1.0.7 설치/홈 확인은 미검증**. 이전 선장 캡처(39111.jpg)는 수정 요구 증거이지 수정 후 설치 검증은 아님.

## 최우선 미완료
1. 선장폰 기존 공동회의 앱에 1.0.7 덮어 업데이트, 홈 7개 70px 및 V3/V7 버튼 실행, 연결 변경 버튼 제거, 기존 연결 보존 확인.
2. 세 기존 ChatGPT 원본방의 실제 업무 통신: `crew_meeting_bridge_events` DB와 `crew-three-room-bridge` Edge Function ACTIVE. 현재 항해사 대화에서 원본 소통 서버에 INSERT→SELECT 검증 메시지 ID `9a007367-89c6-4719-a46b-63ce5d788844`. 통제관·중앙연결관 방에서 같은 서버 메시지를 SELECT하고 각자의 답변 INSERT해 공동회의 화면에 표시되는지 **아직 검증 전**.
3. 별도 원본 ChatGPT 방이 스스로 깨어나 무인으로 계속 답하는 자동 실시간 대화는 **미구현/HOLD**. 공유판의 앱 내 4초 폴링과 혼동 금지.
4. 선장은 중앙연결관이 이미 **서버 등록됨**을 확인. 특정 원본 채팅 주소 테이블의 URL 누락을 서버 전체 미등록으로 단정하지 말 것.

## 원본·보존 및 다음 작업 규칙
- **V3 항해사앱과 V7 중앙통제실은 각각 별도 앱**, 공동회의 앱과 병합하지 않음.
- 기존 기기 등록, 서명, 입장키, 분석 원문, 보고서, 누적DB, 원본 대화방, 연결 기록 삭제/초기화 금지.
- 완료는 실제 검증된 작업만 보고하고, 새 대화방에서는 중복 질문이나 처음부터 재개발하지 않음.
- 상시 상세 작업대장: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/blob/feature/crew-meeting-standalone-apk-20261025/docs/crew-meeting-1.0.3-continuity-20261025.md
