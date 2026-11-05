# V13 선장 전용 원격 UI R25 게시 기록 — 2026-11-05 (KST)

## 대상과 선장 지시
- V13은 **선장 전용 앱**이며 일반 배포판이 아니다.
- V3 항해사앱, V7 중앙통제실, V30 작업과 구분한다.
- 승인 디자인: 황금 범선 배경 전체 폭, 메뉴 버튼으로 좌측 메뉴 열기/닫기, 왼쪽 중복 원문분석 제거, 중앙 원문분석 유지.
- 보존할 메뉴 9개: 선장방, 신서방(구 도사방), 항해사방, 항행의자유방, 성적 관리, 누적 DB, 원문 채팅방, 작업 기록, 설정.
- 기존 원문/DB/서명 APK/분석 기록과 처음 등록 잠금을 변경하거나 초기화하지 않는다.

## 변경 내용과 근거
- `main/releases/ui/v13-ui-r25.html` — 원본 R24 파일을 기준으로 R25 UI 전체를 추가.
- Git blob: `019909ec3afec01f988a1e941276df95bb51b82f`.
- SHA-256: `e1e50cbeb7c0efc2eb5b9197e1d2aefbdd97770b67b9c083970e17cde92d5a0e` (GitHub Actions 42035951772).
- `main/releases/v13-release.json` — `uiUpdate.revision` 24→25, R25 URL/해시 및 선장 전용 안내문만 수정. `latestVersionCode=31380`, 기존 APK 다운로드 URL/서명 해시/기존 공지, `status=captain_test_only`, `releaseScope=captain_only` 보존.
- 적용 커밋: `bf5b68aaaca176cb2dd6a10ec8ae20888c3924cf`.
- R24 기존 파일은 변경하지 않음. 별도 스테이징 소스/검증 기록 보존.

## 시험과 게시 후 확인
- R25 전체 HTML GitHub Chromium 자동시험: 360×780 및 920×1400 각 9개, 총 18/18 PASS (Actions run 42035408922).
- R24 SHA-256 `b81448290fea62823fc2472b4e54b888f95ac90350d865b9cf196c88f2f7f428`은 기존 manifest와 일치.
- R25 SHA-256 게이트 PASS (Actions run 42035951772).
- main 재조회: 파일 blob과 manifest R25 SHA 일치, main HEAD는 적용 커밋.
- **미검증**: 선장 실제 휴대전화의 원격 UI 25 수신/화면 전환, 모든 실제 기기 버튼 터치, 개인 데이터 보존 및 설치 상태. GitHub 등록을 휴대전화 적용 완료로 간주하지 않음.
- **APK 신규 빌드/설치 없음**, V13 자체 앱 버전Code는 31380 그대로. DB/서버 쓰기 없음.

## 다음 1순위
- 선장 휴대전화의 기존 V13 앱에서 원격 UI 업데이트 확인 후 화면에 R25가 실제 적용되는지 확인.
- ☰ 메뉴/9개 버튼, 중앙 원문 분석, 기존 분석 기록·누적DB를 실기기에서 점검하고 이상이 있으면 R24 URL/해시로 복귀할 수 있도록 이전 정본 보존.
