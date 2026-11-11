# N2 승인 홈 화면 진행 기록 — 2026-11-11

- 대상: 별도 N2 앱 `com.hanghae.fold8n2app`, 운영 manifest R6.21.46.
- 기준 GitHub main: `7917c36024feb2fa98e870941ff921b88cf1678d` (작업 시작 당시 조회한 최신).
- 선장 승인 화면: 파란 바다/황금빛 석양/범선/조타륜, 금빛 UI, 이전회차 | 0000 | 다음회차, 분석방 기본 전체방, 기존 6개 메뉴, 종료.
- 분리 작업 브랜치: `work/n2-approved-home-controls-20261111`.
- 추가 파일:
  - `r6-n2-standalone/work/approved-home/approved-home-controls.mjs`
  - `r6-n2-standalone/work/approved-home/approved-home-controls.test.mjs`
- GitHub 최종 파일 추가 커밋: `285fd8ec8ab5c136993bb05b92b5319a6b2eefcb`.
- 실제 수행한 검증: 동등한 로컬 제어 로직 Node 테스트 6/6 통과; 새 GitHub 파일 2개 재조회 성공.
- 보수적 규칙: 0000은 입력 자리표시자일 뿐 유효 회차가 아님; 숫자 1~9999만 유효; 저장 회차는 기존 API 결과만 선택; 전체방은 프레젠테이션 요청만 구성, DB 변경 없음.
- 변경하지 않은 것: 운영 main, update.json, APK, 설치, 개인 서명키, 데이터베이스, 기존 발표 엔진.
- 미완료: 6.21.46 APK 속 `assets/index.html`의 정확한 현재 코드를 안전하게 확보하여 승인 이미지 레이아웃과 기존 이벤트 브리지를 실제 연결, 원본 인증서 유지 재서명, 기기 저장 보존 시험.
- 근거: 종전 작업대장에 따르면 N2 6.21.45 수정은 APK 내부 `assets/index.html` 과 `AndroidManifest.xml` 두 파일 변경. 현 GitHub 배포 저장소에는 현재의 편집 가능한 UI 원본을 찾지 못함.
- 다음 1순위: 운영 APK/보관된 원본 프로젝트의 정확한 UI 소스를 확보한 뒤 작업 브랜치에서 연결·테스트. 소스 확보 전 운영 업데이트 채널 변경 금지.
