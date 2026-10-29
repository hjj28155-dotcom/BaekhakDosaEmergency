# 항행의자유 중앙통제실 — 새 대화방 작업 인계서

기준일: 2026-10-29 KST. 사용자 호칭: 선장님. 담당 역할: 항해사.

## 새 대화방 시작 문장

> 항해사, 이사 완료. 이 인계서를 읽고 최신 작업의 다음 단계부터 진행하십시오. V8.0.6은 통제실장 방에 원문분석, 항해의자유 방에 배포판 실행을 연결한 시험판입니다. 기존 DB·분석·최초잠금을 보존하고 V3 실제 자동연동 미완료 항목을 이어가십시오.

## 선장 확정 지시와 이번 방 완료 내용

- V3와 V8, 일반 사용자용 항해의자유 배포판은 서로 다른 앱이다.
- V7 전체 폐기 지시는 유지하되 공유 서버 자료와 의존성을 분리해야 한다. 이번 방에서 서버 원본을 삭제한 건은 없다.
- V8 홈에 별도의 통제실장 방 추가.
- 각 방의 불필요한 중복 안내·주소설정·서버진단·과거 V7 조회 등은 접는 상세 메뉴로 이동. 기존 기능과 기록은 보존.
- 통제실장 방에 원문분석 버튼 추가. 기존 내장 선장R46/통제실장R1 분석 화면과 PNG 보고서 기능 재사용. 분석 화면에서 돌아가면 통제실장 방으로 복귀.
- PNG 보고서 표시 제목은 ‘통제실장 · 원문분석 보고서’. 엔진 알고리즘·기존 분석 기록은 변경하지 않았다.
- 홈의 항해의자유 방은 연구분석 대신 사용자 배포판 화면으로 연결. 설치된 배포판 실행과 공식 Drive 설치 경로 제공. V3나 V8을 잘못 실행하도록 지정하지 않음.
- 기존 APK·DB·분석기록·최초잠금·서명키는 삭제/초기화/대체하지 않는다. 시험판을 실기기 확인 없이 정식으로 승격하지 않는다.

## 최신 V8 시험판 — 실제 빌드/검증 완료, 휴대전화 미검증

- 버전: V8.0.6 / versionCode 806 / versionName 8.0.6-DIRECTOR-ANALYSIS-DISTRIBUTION.
- 패키지: kr.hanghae.crewmeeting.compact.
- 원본 V8 동일 서명 인증서 SHA-256: e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be. CI 고정 검증 통과.
- 파일: HANGHAE_CREW_MEETING_V8_8.0.6_DIRECTOR_ANALYSIS_DISTRIBUTION.apk.
- 크기: 6,596,200 bytes.
- APK SHA-256: 225de7c3aabdd45a1f91248ad37a4808976c76fa020714b75fd94c29e062dfa6.
- 다운로드: https://drive.google.com/file/d/1rpQKIVuSWo92dL0QCY6dGL64JlRpJ0Ty/view
- 최종 기능 커밋: d98690cc3243bb81a6cc60a062b22b856472b38c.
- APK CI 성공: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40732820098 .
- Android library assemble/lint 및 Chromium 검사 성공: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions/runs/40732813824 .
- 360×740 / 390×844 / 412×915px에서 통제실장 원문분석 진입/복귀, 배포판 화면 분리, 설치/미설치 안내, 실행요청, Drive href, 간소화 메뉴 및 기존 화면 회귀 통과. JS 오류 0.
- 렌더 이미지 직접 확인. 최종 APK 내 HTML/CSS/PNG renderer는 수정 소스와 바이트 일치. classes.dex + classes2.dex에서 배포판 native bridge 및 고정 패키지 문자열 확인.
- 설치/미설치·실행 요청 브라우저 시험은 가상 native bridge를 사용한다. 실제 선장 휴대전화 설치·다른 앱 실행·승인/기록 보존·원문분석 실행은 아직 검증하지 않았다.

## 개발 원본 및 변경 파일

- 저장소: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency .
- V8 작업 브랜치: feature/v8-v7-retirement-v3-link-20261029.
- 초안 PR #4: https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/pull/4 . 미병합. 최신 브랜치 HEAD를 읽고 진행할 것; 최종 기능 커밋 뒤 인계/검증 문서만 추가될 수 있다.
- 화면: crew-meeting-android/app/src/main/assets/captain_home_v114.html, captain_v8.css, v4_png_report.js.
- Native: crew-meeting-android/app/src/main/java/kr/hanghae/crewmeeting/MainActivity.java.
- Manifest/버전: crew-meeting-android/app/src/main/AndroidManifest.xml, app/build.gradle.
- CI: .github/workflows/build-crew-meeting-apk.yml, verify-v3-r60-v8-client.yml.
- 화면 검사: integrations/v3-r60-v8-client/tests/director-room.spec.cjs.
- 이번 수정 기록: worklogs/2026-10-29-v8-806-director-analysis-distribution.md.
- 초기 V3/V8 연결 기록: worklogs/2026-10-29-v7-retirement-v3-to-v8-server-implementation.md.
- 프로젝트에 부착된 2026-10-24/25 V7 문서보다 이 인계와 최신 저장소 기록이 최신 기준이다. 오래된 V7 버전으로 작업을 되돌리지 않는다.

## 일반 사용자용 항해의자유 배포판 — 별도 앱

- 실행 대상 패키지: com.navigator.freedom.v3final. V8 native bridge가 고정된 이 앱의 상태/버전을 확인하고 실행 요청한다. Android queries에 이 패키지 추가.
- 공식 보존 메타데이터: main/freedom-release-latest.json. 이번 확인 시 version40041 / 4.0.41.
- 공식 보존 Drive 파일: https://drive.google.com/file/d/1ULoKMeNFSOWDebMfY4tFvgyiK4EDxaJ-/view . 기존 공식 공급 주소는 바꾸지 않았다.
- 이번 방에서 별도 확인한 시험판은 V4.0.51 /40051: https://drive.google.com/file/d/1PpZeDrzyQK77zzy8qxVDpGcL_KuUSSwI/view . 공식 승격 전이며 ‘설치·업데이트 안내’ 안에 시험판으로 구분.
- 더 높은 버전 설치본을 공식 보존판40041로 내리지 않도록 안내한다. 배포판의 별도 개발방에는 후속 승인/홈버튼/음성 수정 작업이 있을 수 있으므로 새로운 배포판 변경 전 해당 최신 브랜치를 대조한다.
- 확인한 배포판 브랜치: fix/freedom-v4051-drive-readable-bottom-home-20261029 @4eeff6e72999eab41ab3c00c017271baf63e82af.
- 이번 V8 작업에서 배포판 APK나 승인·DB·원문·최초잠금은 수정하지 않았다.

## V3 R60 ↔ V8 실제 자동연동 — 계속 남은 최우선 개발

- 선장 보고 기준 휴대전화 V3 R60 설치, package com.baekhak.dosa.f6, versionCode30193. 기존 DB·분석·최초잠금 보호.
- 원본 R60 APK Drive: 1VvRoYFiZ9WORbTGp2MNyuOTvtsN7am3S.
- R60 자원 소스ZIP Drive: 1yO8Hp5vAT3CtZOTwS6TjabRDrW0CuEb7. 이것은 전체 native Android 프로젝트가 아니다.
- R60 APK SHA-256: 87ccc77af2a1bd2e5a3b19f31ef84e5e21eccffda717daf2e183c726f9f58026.
- R60 서명 인증서: eedcfd3730f650e55defee1b0c971b77117b241211d73eb02a8fd9982245a48c.
- IndexedDB shipmate_v3_r53_fresh20_epoch1, epoch V3_RESTART_R53_20261028. analyses/roundLocks/centralOutbox 원본 보존.
- V8 수신기 소스 integrations/v8-v3-resultbefore-receive/index.ts. 기존 독립 수신기 준비 기록 있음. 서버 현재 운영상태는 재개 시 다시 확인.
- 이번 방 추가 모듈 integrations/v3-r60-v8-client: 정확한 outbox JSON 바이트/해시, 별도 SQLite 복제, WorkManager 재시도, Keystore 토큰, V6 공용 원본 upload→V8 POST→GET 영수증 대조, 성공한 경우에만 기존 outbox SENT_VERIFIED.
- 실제 Chromium IndexedDB+가상 native ACK 보존/거부/멱등성 검사27건 및 Android 라이브러리 compile/lint 통과. 이 AAR/소스가 현재 설치 R60에 포함된 것은 아니다.
- 미완료: R60 전체 native 프로젝트/기존 서명 해제 경로 확보, host 통합, 동일서명 V3 APK, 전용 단말 인증, 실제 휴대전화→서버 원본/ACK 검증. 실제 자동전송 완료나 접수 건수 증가로 보고하면 안 된다.

## 새 방 다음 단계

1. 이 인계 및 저장소 최신 HEAD/작업대장 확인. 끝난 UI 작업을 처음부터 반복하지 않는다.
2. 선장 휴대전화 V8.0.6 설치/통제실장 원문분석/항해의자유 배포판 실행의 실제 결과가 있는지 확인하고 필요한 수정만 진행.
3. V3 실제 연동의 위 미완료 항목을 순서대로 해결. 승인·키·원장 정보를 추정/재발급하지 않는다.
4. V7 서버 원본 삭제는 공유 의존성·외부백업·복구 검증이 끝나지 않았으므로 완료로 표시하지 않는다.
5. 실제 수정/빌드/배포/설치를 확인한 범위만 보고하고 버전·커밋·검증·남은 작업을 대장에 기록한다.
