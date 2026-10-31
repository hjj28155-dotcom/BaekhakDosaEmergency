# ⚓ V13 R8.28 원문 6문장 한 화면 — 화면 수정 소스 인계

기준 소스: **V13 R8.27 (31335)**  
수정 목표: **V13 R8.28 (31336)**, 패키지 `com.baekhak.dosa.v13r3preview`.

## 선장 승인 지시 (2026-10-31)

- 원문 분석 화면 하단의 **“4개 방 결과전 현황”** 카드와 네 개 상태 타일을 삭제하여 화면을 짧게 한다.
- **원문 분석** 자체와 좌측 메뉴 및 독립 4방 엔진 데이터, DB-1은 유지한다.
- `− / 회차 / +`를 30~32px 높이, `회차 적용 / 원문 분석 실행`을 31~34px 높이로 축소한다.
- 한 화면에서 보관된 과거 **원문 6문장**이 모두 보이도록 글자 간격·행 간격을 다듬는다.
- 기존 V1 공지·수동 APK 다운로드 업데이트 동작은 유지한다.

## 실제 코드 변경

1. `app/src/main/assets/index.html`의 `view===0` 원문 분석 템플릿에서 `${fourRoomPredrawOverview(round)}` 출력만 제거. `function fourRoomPredrawOverview` 및 `fourRoomStatusDetails` 자체와 결과전 데이터 코드는 지우지 않음.
2. 원문 화면에만 `rawSixCompact`, `rawActionBar`, `rawBrief`, `rawFootActions` CSS 클래스를 적용. 다른 메뉴 크기는 변경하지 않음.
3. 원문 화면 하단의 긴 엔진 안내 문구는 숨겨 화면 공간을 확보. `다음 회차` 및 분석/회차 조작 버튼 유지.
4. `app/build.gradle`은 versionCode **31336**, versionName **V13 R8.28**. `MainActivity.java`의 오류 시 버전 기본값만 **31336**으로 갱신.

## 확인된 검사

- 기준 index.html SHA-256: `1a0776ebbe13ca56d4b6d9cf67f8b72f54e2e6a2dd3e29f715ed85d64bffa371`
- 수정 index.html SHA-256: `07126717a8734b887a070d7f008c8d97aac50c260ba8b4ba5631f70d61874668`
- 전체 AndroidIDE ZIP SHA-256: `034c568e1a599bf439ae6e7d8ba0bd09468ac6b93534303b8b53c62177d10f39`
- ZIP CRC 통과, 원본 파일 143개 유지 (수정 관리 파일 1개 추가)
- Chromium 정적 브라우저 시험: 393×854, 320×700, 768×880에서 회차 1의 원문 6문장 모두 표시; 4방 결과전 현황 카드 DOM 0개; JavaScript 예외 0개
- 외부 화면과 엔진의 휴대전화 동작, 실제 Android 빌드/서명/설치 시험: **미완료**

## 배포 금지 조건 및 다음 단계

이 문서는 **실제 수정 소스 이력**이며 **설치용 APK가 아닙니다.** 현재 설치된 R8.27은 HTML 패치 링크를 자동으로 화면에 반영하도록 배포된 상태가 아닙니다. 따라서 새 APK를 만들지 않고 GitHub에 이 문서만 올려도 설치 앱의 화면은 바뀌지 않습니다.

R8.28 앱 설치를 위해서는 기존 R8.27과 **같은 서명 인증서**를 쓰는 AndroidIDE 빌드와 서명 검증이 필요하며, APK가 준비되고 Drive 업로드된 뒤에만 `releases/v13-release.json`을 31336으로 업데이트해야 합니다. 앱 배포 R8.27(31335)은 그 전까지 유지합니다.

기존 APK, DB, 서명키, V3/V7 독립 앱 원본 삭제 없음.
