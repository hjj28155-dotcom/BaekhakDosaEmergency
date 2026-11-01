# ⚓ V13 R22 — 4방 결과전 화면 상시 표시 문제 수정 (2026-11-01 KST)

## 선장 보고
- 선장: "각방에 왜 결과전 표시 화면이 안 나타나죠."
- 대상: V13 중앙통제실 R8.29 원격 UI. V3/V7/V12 독립 프로젝트 원본은 수정하지 않음.

## 코드에서 발견한 직접 원인
1. 공개 manifest `releases/v13-release.json`은 원격 revision **19** / versionCode 31337, V13 R8.29를 가리킨다. R21까지 모두 staging-only였음.
2. 선장/도사/항해사 방(view 7/8/10)은 `acceptedRoomPredraw(room,round)` 수신 시에만 PNG를 그리며, 미수신이면 간단한 `roomPngMissing` 안내만 보였다. 항상 표시되는 5조합 목록 슬롯이 없었다.
3. 항행의자유방(view 9)은 `pick(9)`에서 `v13R19ShowFreedomFullscreen()`을 자동 호출. z-index 100100 전체화면 V12 iframe이 V13 내장 결과전 패널을 덮어 사용자가 보지 못했다.
4. 실제 정식 엔진 4방 5+5+15+5 조합과 최초등록 잠금 검증은 아직 완료되지 않았음. 화면 숨김 문제와 엔진 미수신은 별개의 과제.

## R22 안전 수정 (공개 미배포)
- 소스: `releases/staging/v13-ui-r22-predraw-boards-visible.html`.
- 브랜치: `staging/v13-r22-predraw-boards-visible-20261101`.
- R21에서 수정한 1249 원문 비파괴 조회 및 기존 로컬 오버레이 충돌 방지 보존.
- 신규 `renderRoomBeforeBoard(roomKey,roundNo)`: 결과 수신 전에도 선장 5 / 도사 5 / 항행의자유 15 / 항해사 5개 행을 **항상** 표시, 미수신 표기. 수신한 실제 유효 형식의 report만 번호 표시.
- 항행의자유 15개 행을 원문분석 5 / 자동 A 5 / 자동 B 5로 시각적으로 구분.
- 기존 항행의자유 자동 V12 전체화면 iframe을 **보존**하면서 상단에 접기·펼치기 가능한 V13 결과전 표시판을 자동 표시. 접으면 V12 원본 전체화면 확인 가능.
- `V13_REMOTE_UI_DOCUMENT_REVISION=22` (staging label only).
- 정식 PNG, 30조합 합본, 원본 엔진 호출, 공식 등록·잠금·결과후 채점의 게이트 **변경 없음**.
- 연구 조합을 정식 결과전으로 표시하지 않으며 임의 번호·가짜 PNG 생성 없음.

## 검증 실측
- GitHub R22 HTML blob `f09d8e16e684d96c194fc0ab752119dc911fd19b` 재조회.
- V8 Function() inline JS 2개 문법 검사 PASS.
- UI renderer isolated mock 실행 13/13 PASS: 각 방 미수신 슬롯 수(5/5/15/5), 5개 수신 시 잔여 미수신 행, freedom 3그룹 구분, V12 전체화면 iframe 및 패널 유지, 1249 source 보존, actual accepted-report-only gate, JS 구문 확인.
- 테스트의 수신 조합은 UI 배열 가상 입력이며 엔진 출력/실제 결과전/공식잠금 증명이 아님.
- Android/WebView 단말 화면, 터치·스크롤, V12 iframe 안쪽의 실제 분석 출력, 1248/1249 정식 15/30조합, 최초등록 잠금은 미검증.

## 안전 상태 및 다음 단계
- GitHub main의 공개 배포용 `releases/ui/v13-ui-r19.html`, `releases/v13-release.json`, APK, DB 및 각방 과거 보관 기록 변경 없음.
- 다음 1순위: 폰에서 결과전 표시판 가시성·전체화면 동작 검증(예: 전체화면과 상단 결과전 보드 펼침), 실제 V12 정본/독립 원본 엔진 어댑터 확보 및 1249 회차 형식 검증.
- 충분한 단말 검증과 배포판 승격 확인 전 R22를 '설치 적용됨'으로 보고하지 않음.
