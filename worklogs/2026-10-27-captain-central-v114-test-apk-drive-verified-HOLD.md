# ⚓ 선장 전용 중앙통제실 1.0.14 — 동일서명 시험 APK / Drive 검증 완료 (2026-10-27 KST)

## 선장 지시·작업 경계
- 선장 요청: 홈/통합조회/결과후 시안에 이어 선장 전용 중앙통제실 APK를 제작하여 Google Drive에서 내려받게 할 것.
- 수정대상: `kr.hanghae.crewmeeting.compact` **독립 선장 전용 중앙통제실 APK만**.
- 일반 배포판 V4.0.41/V4.0.43 후보, 독립 V3, 독립 V7, 기존 승인관리 DB 및 구매 잠금 기록은 전혀 변경하지 않음.
- 기존 설치 APK v1.0.13, 공식 고정 Drive APK ID `1Ddg-fGo2q3JfowTjRTNUxmXPQ46CkUZL` 보존. 고정 링크 덮어쓰기/정식 교체 미실시.
- 이미지 생성된 대시보드 미리보기는 디자인 시안이며 Android 앱에 실제 포함한 HTML과 픽셀 단위 동일하지 않음.

## V1.0.14 수정 및 소스
- 서명 보존용 빌드 브랜치 `feature/crew-meeting-standalone-apk-20261025`.
- 앱 Gradle: `versionCode=114`, `versionName=1.0.14-CAPTAIN-RESEARCH`; 기존 package 및 debug signing config 유지.
- Android `MainActivity.java`: 기존 웹 Pages main URL을 그대로 읽어 신형 UI가 소실되는 현상을 방지하기 위해 APK 내 `captain_home_v114.html`을 `loadDataWithBaseURL`로 표시. 기존 원본방 라우팅·승인관리방 링크·V3/V7 설치 앱 직접 실행 브리지 보존. WebView 안의 기존 승인정보 초기화나 새 앱 설치는 하지 않음.
- HTML 원본: GitHub 개발 브랜치 `design/captain-three-app-command-home-20261027`의 `crew-meeting.html` (원본 Git blob `3acbf6f13ab5954811866f28139fe0f1b23d0f6e`).
- Android 내 포함된 6개 방: V3 4개 방(원문 총15+자동 총5), V7 통제관방(원문 5), V4 항해의자유방(원문10). 결과전·결과후 조회 인터페이스와 기존 승인관리 버튼 포함.
- 한계: 원본 3앱 자동 결과전 송신·결과후 자동 연계·실기기 UI는 아직 미완료. 서버 원본 읽기용 `crew-six-room-before-feed` v2 ACTIVE와 별도 검증해야 함.
- 변경 커밋: HTML 내장 `ff1d21eb1438af09aa769da4f882bfba05093884`, Native 브리지 `aa7796556bf305b76e3542d8695e4c6d7932744b`, 버전 `16f6b26d80920b487bd779dff9d72cd43f8aa0ec`, workflow 갱신 `a6f4e1aaad9b23bd5b67fe354b9b82bf7355aacf`.

## 빌드·서명·패키지 실검증
- GitHub Actions `40505754345`: `completed success`.
- 빌드 로그 단계: 기존 고정 debug signing key 복구, 인증서 pin 검사, Android compile, `apksigner verify`, 패키지/버전 검사, HTML 필수 DOM ID 검사, 결과파일 SHA-256, 아티팩트 업로드, 내부 sha256sum --check 전부 성공.
- 원본 서명 인증서 SHA-256 pin: `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be` (기존 1.0.13 동일).
- 패키지명 `kr.hanghae.crewmeeting.compact`, 버전코드 `114`.
- GitHub Actions 아티팩트 ID `12735295825`, `HANGHAE-CREW-MEETING-1.0.14-TEST-APK`.
- APK 실제 크기 `64089` bytes.
- APK SHA-256 `14299f7e0f7d46c2fa669b3207eed8141c875e9a0c58aef5066d5a81018d43dc`.
- ZIP 검사: `zipfile.testzip() is None` / APK 내부 `assets/captain_home_v114.html` 및 AndroidManifest 존재. APK 포함 UI의 결과전/결과후/4개 V3방·V7·V4 문자열 확인.
- Drive 재다운로드 실제 APK 64089 bytes, SHA-256 완전 일치, **CI 산출물과 바이트까지 동일**.

## 다운로드 / 보관
- APK 단독 Drive 업로드/재다운로드 검증: `https://drive.google.com/file/d/1U2KR-ML4Kpq4jDJj6MpSeD3GT9oaQ2qY/view`.
- 검증용 APK+SHA256 ZIP Drive 별도 업로드: `https://drive.google.com/file/d/1zv4yH-s1MQC9NdvwX0FAv1vYPNpU8P9S/view`.
- 이전 V1.0.13 고정 업데이트 링크는 절대 변경하지 않음. 개인 선장 전용 새 파일을 별도 신규 업로드했으며 공유권한을 확대하지 않음.
- 위 두 파일의 Google Drive 업로드는 소스 변경 없이 각각 보관. 구글 드라이브 링크가 소유자 이외에 열리지 않을 수 있음(비공개 정책 유지).

## 남은 실제 작업
1. 선장 Android 기기에서 기존 앱 삭제 없이 1.0.13 → 1.0.14 업데이트 성공 확인. 앱 홈·원본 대화방 연결·승인관리 진입·파일 조회·배경·버튼 가독성 실제 확인.
2. 결과전 6방의 자동 송신 연결 (V3 4개 방 원본 이벤트/서명·권한 확인; V7/V4 독립 출처 확인), 회차중복 및 추첨전/사후 재분석 구분.
3. 결과후 공식 당첨번호 자동 채점·통합표/원본 분리, 조합이 없는 회차는 0점 오인 금지.
4. 선장 실기기 승인 전에는 고정 Drive 링크 교체, 정식 업데이트 배포, 광고/배포판 APK 수정 금지.
