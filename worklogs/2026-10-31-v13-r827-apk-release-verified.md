# V13 R8.27 — 실제 APK 수신·검증·배포 등록

작업일: 2026-10-31 (Asia/Seoul) / 대상: V13 중앙통제실 (V3 항해사앱·V7과 별개)

## 선장 지시 및 기준
- V1 방식: 기존 앱이 GitHub 배포 정보 조회, 업데이트 발견 시 팝업, 사용자가 다운로드 후 직접 APK 덮어 설치.
- 홈/우측 기능창 보존, 이미 삭제 지시한 안내 문구 제거, 중복 업데이트 확인 버튼 하나로, 수동 조회 시 업데이트 없으면 '최신 버전입니다.' 유지.
- 기존 DB/서명/앱 삭제 금지.

## APK 실물 검증
- 선장이 업로드한 파일: `V13_R8_27_V1_POPUP_BUILD_THIS-debug.apk`
- 파일 크기: 15,027,333 bytes
- SHA-256: `07d26ad31e31befd8f61e84cd25ea06ede7ba424c02d9d367462f93731d3eabb`
- APK ZIP 오류 없음, AndroidManifest binary XML 직접 파싱:
  - package `com.baekhak.dosa.v13r3preview`
  - versionCode=31335
  - versionName=`V13 R8.27`
- APK Signature Scheme v2 서명 데이터 인증서 SHA-256:
  `3fe57967c15f81ee39ea2b9b88dd96a7506c1b5b9032606ecbea04bbdb15ee3c`.
  R8.25(31333)·R8.26(31334) 인증서 동일, 서명 데이터 cryptography 검증 통과.
- `assets/index.html`: SHA-256 `1a0776ebbe13ca56d4b6d9cf67f8b72f54e2e6a2dd3e29f715ed85d64bffa371`.
  `동일 패키지·동일 서명` 0회, `새로운 APK가 등록되지 않았습니다` 0회, `최신 버전입니다.` 1회.
- 실제 선장 휴대전화 덮어 설치 및 최신 버전 조회 결과는 아직 미검증.

## Drive 실물 업로드
- Drive V13 전용 폴더 `1KKF32iMa7l5ulap_NKjmtmZv9AfqnKGi`
- 파일명 `V13_R8_27_V1_업데이트_31335_20261031.apk`
- ID `1GS0IVF0AjAR-Egg1LHqRTVVDtnyd7lhe`
- URL https://drive.google.com/file/d/1GS0IVF0AjAR-Egg1LHqRTVVDtnyd7lhe/view?usp=drivesdk
- 재조회 크기 15,027,333 bytes, Drive에서 내려받은 실제 바이트 SHA-256 원본과 동일.
- **중요:** Google Drive metadata `shared=false`, owner-only 권한. 기존 사용자 외부 다운로드는 아직 검증되지 않았으며, 공개 배포에는 Drive '링크가 있는 모든 사용자 > 뷰어' 권한 필요. 연결된 Drive 공유 도구는 개별 사용자/도메인 공유만 가능하여 공용 링크 권한 설정은 미실시.

## GitHub 게시
- https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/blob/main/releases/v13-release.json
- `latestVersionCode` 31335, `versionName` `V13 R8.27`
- `downloadUrl` 위 Drive APK 링크
- `deploymentMode` `manual_apk_v1`, `status` `published`, `uiUpdate` 없음, `announcement` null.
- 갱신 커밋: `48dd9665382a5865b2cde9be16bfa22fd4281a43`.
- GitHub 파일 재조회로 값 확인 완료.

## 다음 단계
1. 선장 기기 R8.26에서 앱 종료 없이 홈으로 나갔다 복귀; 60초 제한 후 R8.27 업데이트 팝업 확인.
2. APK 다운로드, 기존 앱 덮어쓰기 설치 성공 및 DB 보존 확인.
3. Drive 공유 상태를 점검하여 일반 설치자도 다운로드 가능한지 확인(필요 시 '링크가 있는 모든 사용자' 읽기).
4. 설치 후 '업데이트 확인' 버튼에서 '최신 버전입니다.' 표시/중복버튼 없음/안내 문구 삭제 확인.
