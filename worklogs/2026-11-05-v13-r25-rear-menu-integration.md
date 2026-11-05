# V13 R25 승인 메뉴 통합 및 정적 검증 — 2026-11-05 KST

## 작업 정체성과 기준
- V13 중앙통제실만 변경 대상으로 삼음. V3 독립 항해사앱, V7 E100, 별도 V30 시안 혼동 금지.
- 원본 GitHub main: `releases/ui/v13-ui-r24.html` (blob `1df7a4b9ca0d338058f9cab67c30a846d0a9b0b9`; 1,239,178자), V13 R8.72 APK versionCode 31380, remote revision 24.
- 작업 브랜치: `staging/v13-approved-rear-menu-20261105` (운영 main 아님).
- 선장 승인: 좌측 메뉴 홈 뒤 숨김, 상단 메뉴 클릭하면 표시, 중복 원문 분석·1~300 연구실·자동 A·B 방 제거(화면 항목만), 9개 메뉴 보존, 중앙 원문 분석 유지.

## 소스 변경 내용
- 새 통합 HTML: `releases/ui/v13-ui-r25.html` (blob `10cc12b796fe3098b702f68c4536f036e09449fd`; 1,240,659자).
- 생성 커밋 `09f5314248375c84768f7d3603e527aa1621aa92`.
- 기존 가로 2열 `#workspace.open` 메뉴 → 홈을 축소하지 않는 절대위치 오버레이 `#rail`.
- 화면 메뉴 인덱스 `[7,8,10,9,6,3,11,12,13]` (9개만): 선장방, 신서방(구 도사방), 항해사방, 항행의자유방, 성적 관리, 누적 DB, 원문 채팅방, 작업 기록, 설정.
- `menu[8]`의 **표시명만** 신서방으로 바꿈. 도사방 기존 실제 핸들러/데이터 인덱스 8 유지.
- 중앙 홈 버튼 `#homeRawAction data-goto=0` 신규 배치. 기존 `stage` 이벤트 위임의 `pick(0)`을 그대로 이용하므로 기존 원문 분석 화면으로 연결.
- 우측 기능 패널 닫을 때 홈 배경으로 복귀하며 메뉴를 숨기도록 `closeRightPanel()` 수정.
- 문서 revision 상수 24 → 25. APK 버전과 공식 릴리스 JSON은 변경하지 않음.
- 이전 별도 `integration/v13/approved-rear-menu.js`는 시험 모듈로 보존하되 새 HTML에는 삽입하지 않음.

## 실행한 검사 / 결과
- GitHub 새 HTML 재조회 성공: blob `10cc12b796fe3098b702f68c4536f036e09449fd`.
- 9개 메뉴 순서와 원문 분석 좌측 중복 제거 정적 검사 PASS.
- 중앙 원문 분석 버튼/기존 data-goto 이벤트 경로 정적 검사 PASS.
- 배경 축소 방지 CSS, 상단 MENU 이벤트, 원본 DB 조회 기능, 업데이트 호출 존재 검사 PASS.
- HTML 내부 `<script>` 3개 중 2개 JS 스크립트 `new Function` 구문검사 PASS. 나머지 1개는 JSON 스크립트로 JS 문법검사 대상 아님 (JSON 자체 별도 파싱 미실시).
- main 과 staging 의 `releases/v13-release.json` blob SHA 동일 `e52ef22a14fdf2e293a237d9d2bd2c72533748c8`. 현장 배포 revision 여전히 24, APK versionCode 31380.
- Android 기기 클릭, WebView 표시, 브라우저 이미지 레이아웃, 중앙버튼 위치, 폴드 펼침/접힘, APK 빌드 및 설치는 **미검증**. 기능 완성/운영 업데이트 완료라고 주장하지 않음.

## 보존/제약
- DB, 기존 여섯 문장, 조합, 결과전 최초등록 잠금, 채점/성적, 승인 기록, 서명키, 설치 APK, main 배포 설정 보존.
- R25는 실제 GitHub HTML로 통합되었으나 **시험 브랜치 파일**이므로 현재 설치 앱에 자동 적용되지 않음.
- 다음 단계 우선: 선장폰 WebView 호환성/실제 클릭 검증, 사용 환경 폴드 한·두 화면 QA → 오류 수정 → 선장 전용 remote revision 25 시험 배포 시 공식 manifest 해시 갱신을 동일 변경으로 수행. APK 교체는 별도 문제.
