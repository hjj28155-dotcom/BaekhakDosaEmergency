# 2026-10-29 KST — V8.0.4 통제실장 전용 방 / V3 전송 모듈

기준: feature/v8-v7-retirement-v3-link-20261029 @ 7da823061d268e696332725d5684a4b95a6993b1.

선장 지시: V3↔V8 실제 자동연동을 이어가며 홈화면에 별도의 통제실장 방을 만든다.

## 변경
- V8 홈에 독립 통제실장 방 입구, 내부 전용 화면, 원본 대화 열기/주소설정, 보고서/서버점검/소통/진단 및 홈 복귀 추가. 기존 linkofficer 원본방 저장 키/주소 재사용. 기존 방·DB 삭제 없음.
- V8 versionCode 804, versionName 8.0.4-DIRECTOR-ROOM. 동일 V8 인증서 고정 빌드 workflow 유지. 시험 산출물명 V8_8.0.4_DIRECTOR_ROOM. 휴대전화 설치는 별도 검증 전.
- V3 R60 APK와 source ZIP을 Drive에서 복구. APK SHA-256 87ccc77af2a1bd2e5a3b19f31ef84e5e21eccffda717daf2e183c726f9f58026, 공개 서명인증서 eedcfd3730f650e55defee1b0c971b77117b241211d73eb02a8fd9982245a48c 직접 확인.
- integrations/v3-r60-v8-client: 기존 R60 IndexedDB 원본 보존 JS adapter, 별도 SQLite/WorkManager 송신 모듈, Keystore 토큰 보관, 업로드 해시/POST→GET 영수증 대조, 오류 재시도/충돌 보류 구현. R60 Android 전체 빌드 프로젝트와 서명키 해제 수단은 확보되지 않음. 이 모듈은 설치 APK가 아님.
- prepare_assets.py는 정확한 R60 APK만 받아 별도 override 생성. 기존 PENDING_UNSENT 전용 시간표 조건이 SENT_VERIFIED도 허용하도록 수정; 빠른 ACK가 하루 분석 카운트 중단을 일으키는 문제 방지. 후속 당첨조회 V8 URL 연결 준비. 엔진 원본/기존 DB 무변경.

## 검증
- 로컬: APK SHA-256 및 v1 공개 인증서 확인, asset override 생성 성공, Python 구문검사 및 JS 구문검사 통과.
- 로컬 브라우저 런타임은 Chromium 없음, 브라우저 다운로드 실패로 UI/IndexedDB 동작검증 미실행. CI에 Android library compile/lint, 실제 Chromium IndexedDB + 가상 native ACK 검사, 360/390/412px 통제실장 방 이동 검사와 스크린샷을 추가. CI 결과 후 별도 기록.
- 실제 단말 인증/업로드 성공/동일서명 V3 업데이트 설치/ V7 전체 삭제는 미완료. V7 서버 데이터 삭제 0건.

## 다음
CI 결과 확인 및 오류 수정 → V8 동일서명 시험 APK 확인 → 통제실장 화면 최종 렌더 확인. V3는 R60 전체 native 프로젝트 및 원본 서명 해제 경로 확보 후 앱 통합/인증/폰에서 실제 전송 검증 필요.

## 후속 검증 결과
- V8.0.4 동일서명 APK CI 40726470348 성공. APK 6,592,692 bytes, SHA-256 7e5b736daa153bd678e2550923deb609cc0a897cba7411917b3a27e2603be0a0. v2 공개 인증서 SHA-256 e24e9573e5e98567ea2e5abddac427126c43d8f1e46b37e4099dabc911cf70be. APK 내 HTML/CSS가 수정 원본과 바이트 일치.
- 첫 검증 CI는 폐기된 SDK tools 패키지 요청으로 실패. 현재 SDK 패키지 명시 + Android/browser 독립 job으로 수정.
- CI 40727008078 성공: Android library assembleDebug/lintDebug, Chromium 실제 IndexedDB 보존/ACK 거부 27건, 360×740/390×844/412×915px 통제실장 방 진입·주소 설정·보고서·홈 복귀, JavaScript 오류 0.
- 세 화면 렌더 이미지 직접 확인. 미등록 원본방 버튼의 마지막 글자 줄바꿈을 피하도록 원본방 등록으로 줄임. 최종 APK 재빌드 결과를 이어서 기록.
- 아직 실제 V3 native host 통합/동일서명 V3 APK/단말 인증/폰 자동전송 검증은 미완료. 라이브러리 빌드 성공을 해당 작업의 완료로 간주하지 않음.

- 360px 렌더에서 홈 상단 원본방 버튼과 진단표시의 경계가 겹치는 부분 확인. 작은 화면 hero 최소 높이를 205px로 확보하고 겹침 방지 브라우저 검사를 추가.
