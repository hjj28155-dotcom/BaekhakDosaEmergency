# ⚓ V13 R8.29 원격 화면 복구 APK 배포 검증 기록

- 기준일: 2026-10-31 (KST)
- 대상: 선장 전용 V13 중앙통제실 (V3·V7 별도 보존)
- 선장 직접 AndroidIDE 빌드 후 제공한 APK: `V13_R8_29_REMOTE_UI_BUILD_THIS-debug.apk`
- package: `com.baekhak.dosa.v13r3preview`, versionCode **31337**, versionName **V13 R8.29** (AndroidManifest binary XML 검증)
- 파일 크기: **15,032,713 bytes**
- SHA256: `6ccd9440ff79efb12ab8415de7922179f9a0ff71e8e64befa1ec3d7e3f1c4f11`
- v2 서명 인증서 SHA256: `3fe57967c15f81ee39ea2b9b88dd96a7506c1b5b9032606ecbea04bbdb15ee3c`
- 비교 R8.27(31335) 인증서 일치, APK v2 서명 데이터 검증 통과. 실제 단말 설치 성공은 선장 확인 전.
- ZIP CRC 검사 정상.
- `assets/index.html` SHA256: `07126717a8734b887a070d7f008c8d97aac50c260ba8b4ba5631f70d61874668`; 4방 결과전 현황 화면 숨김, rawSixCompact 탑재, 원문 6문장 화면 보존.
- DEX `classes2.dex`: `uiUpdate`, `checkForUiUpdates`, `appliedRevision`, `pendingRevision`, `remotePoll`, `v13_verified_remote_ui.html` 문자열 포함. Java 소스의 네이티브 원격 검사 및 60초 간격 활성 중 재확인 흐름 확인. 실제 휴대전화 통신·다이얼로그 동작은 미검증.

## Drive 배포
- 선장 V13 전용 Drive 폴더 `1KKF32iMa7l5ulap_NKjmtmZv9AfqnKGi`
- 새 파일 ID `1zBa-KXjceUxj7gbwUp_RTHTCadVoTm0S`
- 파일 URL https://drive.google.com/file/d/1zBa-KXjceUxj7gbwUp_RTHTCadVoTm0S/view?usp=drivesdk
- 업로드 후 metadata 크기 15,032,713 bytes, 원본 다운로드 재검증 SHA256 일치.
- 소유자 전용 Drive 접근: 선장 전용 앱이므로 공개 권한으로 변경하지 않음.

## GitHub 배포
- manifest https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/blob/main/releases/v13-release.json
- manifest commit `43cd88015bbd1901bcbffd35decad0d2b8db0444`.
- published R8.29 / 31337, APK downloadUrl 위 Drive 링크.
- 원격 화면 리비전 **6**, minVersionCode **31337**, 파일 `releases/ui/v13-ui-r6.html`, sha256 `07126717a8734b887a070d7f008c8d97aac50c260ba8b4ba5631f70d61874668`.
- GitHub HTML blob SHA1 `a569cab6d8d0f6ce458a6e096429ee5c2d7e4d32`는 빌드 APK의 내장 화면과 동일. 6회차는 원격 전송·적용 경로 검증용으로, 이미 설치된 R8.29의 내장 화면과 시각적으로 동일할 수 있음.

## 확인 미완료 / 다음 작업
1. 선장 단말에서 R8.27 위에 R8.29 덮어 설치해 데이터 유지 확인.
2. 앱 실행/포그라운드 복귀 또는 약 60초 이내 `원격 화면 수정 6회차` → `적용` 표시 여부 확인. 없으면 원인 분석하며 작동 완료 선언 금지.
3. 원격 적용 토스트 및 개정 회차 6 표시 후, **다음 실제 버튼/화면 수정**을 7회차 파일로 올려 새 APK 빌드 없이 시각 변경이 가능한지 검증.
4. DB-1 1249 파일 선택기 문제는 선장 요청대로 보류. 기존 DB·4방 엔진 무단 삭제 없음.
