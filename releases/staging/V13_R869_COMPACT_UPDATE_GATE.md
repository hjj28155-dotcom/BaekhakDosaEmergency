# V13 R8.69 홈 업데이트 준비 — 운영 미적용

- 기준: V13 R8.68 -> R8.69, versionCode 31376 -> 31377
- 소스 전체 ZIP 구글드라이브: https://drive.google.com/file/d/1a66wAH0b-hnAMon0bOG5dc7Dt9ERCNic/view
- 검증 결과: https://drive.google.com/file/d/1s2SURuS8kkTVWibO7cX3dx0Qbt5P938C/view
- 화면: https://drive.google.com/file/d/1rX6Ez31sS-zy4LzQfQpQrxR-rN3TfKBL/view
- 변화: CSS 카드 상하폭 축소 / 앱 버전 변경만. DB/엔진/Java/업데이트 버튼 및 기존 6개 방 기록 보존.
- CSS 이전/이후 영역 높이: 393px 화면 약 26.0% 축소, 768px 약 27.5% 축소. 모의 브라우저 및 QA 시험 성공.
- APK: **없음**, Android SDK 컴파일/서명/실기기 테스트 미실시. 소스 ZIP을 APK 다운로드 URL로 지정하지 말 것.
- 운영 메타데이터 main/releases/v13-release.json 현재 R8.29(31337) — 기존 설치자용이므로 변경하지 않음.
- 설치자의 동일 서명으로 빌드된 V13 R8.69(31377) APK 실파일을 Drive에 업로드하고 SHA256, 서명 인증서 일치, 덮어쓰기 설치·기존 원장 보존을 검증한 뒤에만 운영 메타데이터 게시.
- 기존 앱을 삭제하거나 원장을 초기화하지 않는다. V14/V7 변경 없음.
