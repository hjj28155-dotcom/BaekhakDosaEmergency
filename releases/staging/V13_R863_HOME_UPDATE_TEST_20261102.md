# V13 R8.63 홈 업데이트 시험 채널 (운영 미적용)

- 시험 기준: V13 R8.62 (31370)→R8.63 (31371), 동일 APK 인증서 지문 `3fe57967c15f81ee39ea2b9b88dd96a7506c1b5b9032606ecbea04bbdb15ee3c` 확인.
- 시험용 APK SHA256 `039586224edb9442113cfb26a0edc778544533f4273c673527cf1cca5056a24f` (사용자 첨부 R8.63 debug APK 바이트 그대로; 임의 변경 없음).
- 시험 APK 다운로드: https://drive.google.com/file/d/1-Mn-gV6tEZVZg-ZTl93ju3WY1SU189fu/view
- 운영 `main/releases/v13-release.json` 변경 금지. 현재 설치 앱은 `main`을 참조하므로 이 분리 JSON을 자동으로 읽지 않습니다.
- `test_only_not_production`: **브라우저·모의 네이티브 브리지 연동 검증**. 실제 홈 버튼에서 해당 시험 배포를 받으려면 앱 자체의 테스트 채널 전환이나 운영 전환이 필요합니다.
- 기존 V13 R8.63 앱에 적용된 업데이트 공지 브리지 보완은 새 소스 ZIP 후보이며, 시험 APK에는 아직 포함되었다고 간주하지 않습니다.
- 실기기 업데이트 후 기록 보존, 앱서명 및 원격 설치 검증 미완료; 운영 배포로 전환 금지.
