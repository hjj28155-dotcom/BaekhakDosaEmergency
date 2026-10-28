# ⚓ 항행의자유 중앙통제실 V8 — 홈화면·통제실장 명칭 작업대장

- 날짜: 2026-10-28 KST
- 소스 브랜치: `feature/crew-meeting-standalone-apk-20261025`
- 최종 소스 커밋(본 작업대장 작성 직전): `9c7a6f0584b2b52986440d0fe1c027b8e5c95c1a`
- 기존 standalone: `1.0.30-CAPTAIN-NAVAL-RESEARCH-SCORES`, versionCode 130
- 신규 목표 standalone: **`8.0.0-CAPTAIN-CONTROL-DIRECTOR`, versionCode 800**, package `kr.hanghae.crewmeeting.compact` 유지
- 원본 V3 R60 APK: `com.baekhak.dosa.f6`, code 30193, **수정 없음**
- 기존 V7 엔진·원본·DB·접수증: **수정 없음**

## 선장 지시와 구현
1. 중앙통제실 표시 명칭을 **V8**로 변경: HTML 제목·홈 헤더·Android 런처 레이블·APK 버전.
2. **연락관/중앙연결관 → 통제실장**: V8 HTML, 누적 채점 라벨, V4 PNG 보고서 표시 문자열을 수정. 단, 과거 자료·연구 엔진 파일명 `liaison-blind-engine.mjs`, DB key `v4-2`, 원본 `LIAISON_R1_INDEPENDENT_ARCHIVE`, `linkofficer` 내부 역할 ID는 호환성 때문에 그대로 보존.
3. 선장님이 제공한 V3 고급 항해 홈화면을 참고하여 V8 전용 SVG 범선·등대·바다 일러스트 `captain_v8_hero.svg`, 금색 테두리·청/녹/적/보라 4색 대형 관리 카드 `captain_v8.css` 추가. 기존 V3 앱 UI/파일은 수정하지 않음.
4. V8 제4 카드에서 기존 통제실장 원본 대화방 및 실시간 소통방의 기존 핸들러로만 이동. 새로운 서버 데이터 쓰기 없음.
5. Android WebView의 CSS/SVG 로컬 에셋 인터셉트 추가. 기존 원본 APK 서명키 검증 CI 규칙 유지.

## 검증 상태
- GitHub 파일 생성·수정 및 소스 재조회 가능. 위 변경 사항은 브랜치에 커밋됨.
- GitHub Actions V8 최종 검증 빌드: **`40705230989` SUCCESS**. APK 서명 SHA-256 고정값 `e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be` CI 검증 통과.
- GitHub artifact ID `12821934650`, name `HANGHAE-CREW-MEETING-V8-8.0.0-CAPTAIN-CONTROL-APK`.
- 실제 추출 APK: 6,521,818 bytes, SHA-256 `1081160086cbe5692fab2a67aa8bb4334188ccbcb959486b35bc908dc06a2a88`.
- APK ZIP 내부의 V8 HTML·CSS·SVG 존재, `AndroidManifest.xml`의 UTF-16LE 버전명 `8.0.0-CAPTAIN-CONTROL-DIRECTOR` 및 런처 레이블 `항행의자유 중앙통제실 V8` 확인.
- 정적 브라우저 화면 미리보기: 360×800, 390×844, 412×915; V8 범선 배경 표시, 관리 카드 4개, 구명칭 미노출, 가로 넘침 없음, JS 예외 없음. 390×844에서 `통제실장` 카드와 V8 실시간 소통 버튼을 클릭해 `roomMode` 및 홈 영웅이미지 숨김·소통방 노출 확인.
- 브라우저 테스트는 APK 에셋을 인라인한 Chromium 시뮬레이션이며 **Android 실기기 설치·서버 인증 E2E 검증을 대체하지 않음**.
- 초기 V8 CI 실행 `40705006356`은 이전 명칭 `연락관엔진`을 검사하는 오래된 assertion으로 실패. 이를 `통제실장엔진`으로 수정 후 재빌드.
- 최종 APK **CI 빌드 및 브라우저 미리보기 검증 완료**. 다만 실제 휴대전화 설치, 기기별 화면, V3 자동업로드, Google Drive 최신 파일 교체는 **미확인**. 확인 없이 완료 주장 금지.

## 보존·주의·남은 작업
- V3 R60→V7 항해사방 `extra_navigator/result_before`의 신규 인증 수신기·Android 자동전송·실제 ACK는 **별도 미완료 과제**. 인계서의 서버 배포 승인 조건 준수.
- 기존 V7 1~16 RESULT_AFTER 파일, DB-1, V4 R46 영수증, 최초잠금·원본·V3는 삭제·초기화·덮어쓰기 금지.
- 기존 고정 Google Drive 링크 `1NtFkXk2HPqUOPsp2K-as29r9SHgKJmYc`가 **V8 APK로 교체되었다는 검증은 없음**. 이전 대화의 V1.0.30 Drive 교체 완료 표현도 증빙되지 않음.
- 다음 단계: 선장 실기기에서 V8 화면·버튼·동일 서명 업데이트 설치 확인 → 선장 승인된 방식으로 Google Drive 최신 APK 교체 여부 결정 → 별도 V3 R60 결과전 인증 수신기 구현 검토.
