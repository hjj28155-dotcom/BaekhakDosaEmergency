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

## 2026-11-05 22:33 KST — 선장폰에 업데이트 정보가 없다는 보고 / 정정
- 선장 보고: V13 앱에서 R25 업데이트 정보가 표시되지 않음. 이를 앱 설치/업데이트 완료로 간주하지 않음.
- 이전 보고 중 'R25 GitHub 원격 HTML 게시만 하면 기존 V13 R8.72 앱에서 UI 업데이트가 수신된다'는 내용은 호환성 검증 부족으로 잘못됐음.
- 검증 근거: `worklogs/2026-11-04-v13-r913-round-navigation-staging-and-r872-compatibility.md`에서 실제 R8.72 APK SHA256 `e636d0c27ef232d6a4887ee8ca58ecca59709fa8a72187a000e3a280a3e0a933`의 classes2.dex 검사. `checkForUpdates`와 `checkRemoteUiPatch`는 존재하지만, 전체 HTML용 `checkForUiUpdates`와 `getUiRevision`은 존재하지 않음. 제한된 CSS 선택자 변경은 가능하지만 새 메뉴 DOM/핸들러 교체는 불가.
- `releases/v13-release.json`의 uiUpdate R25 URL/해시는 실제 게시됐고 브라우저 18/18 통과했지만 **R8.72 단말 UI 업데이트 설치 증거가 아니다**. 기존 APK versionCode=31380 그대로이므로 APK 업데이트 알림도 R25로 발생하지 않음.
- V13 선장 전용 앱. 일반 배포판과 혼동 금지. V3, V7, V30 독립 관리.
- 이전 R9.13 AndroidIDE 프로젝트 소스(31421) 스테이징 및 Drive 보관 사실은 위 2026-11-04 기록에서 확인되나, 동일서명 APK 빌드/설치 및 기존 데이터 보존 실기기 검증은 미완료.
- **필요한 실제 후속 작업**: 선장폰에 설치된 APK versionCode/서명확인 → 현재 R8.72이면 R9.13+R25 메뉴 병합 Android 소스에 원격 HTML 로더를 구현하거나 APK 내 assets 화면 교체 → 동일 서명 인증서로 APK 빌드 → 기존 설치본 삭제 없이 업그레이드 및 기록 보존 시험.
- 본 정정 기록은 운영 파일/DB/APK를 변경하지 않음. 원격 R25 파일은 보존하되 설치 완료로 표현 금지.
