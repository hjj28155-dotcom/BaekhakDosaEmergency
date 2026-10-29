# ⚓ V10 30194 — 실제 APK 홈 이미지 누락 수정 / 서명 전 최종 후보

작성일 2026-10-29 KST. 선장 "계속 진행하십시오" 지시.

## 기존 후보에서 검출된 실제 결함
- 이전 V10 서명 전 후보 `HANGHAE_CENTRAL_V10_30194_SAME_SIGNER_REQUIRED.apk.unsigned`, SHA-256 `75e45eec435e8393cb8c26af51b6465000ff60f48e6c0afab58768ee4b97e9a5`의 `assets/index.html`은 홈 로고 `v10_icon_draft.png`와 범선/노을 배너 `v10_reference_banner.jpg`를 참조.
- 그런데 해당 후보 APK의 `assets/`에는 **위 두 파일이 실제로 들어 있지 않았음**. 이전의 standalone 브라우저 목업은 이미지가 있었기 때문에 이 오류를 검출하지 못함.
- **이전 구버전 서명 전 후보는 서명/설치에 사용 금지.** 기존 Drive 파일은 삭제하지 않고 별도 신규 검증 후보로 대체.

## 이번 수정 — 분석원장 불변
- 새 파일 `HANGHAE_CENTRAL_V10_30194_ASSETS_FIXED_UNSIGNED_DO_NOT_INSTALL.apk.unsigned`; Android 패키지 `com.baekhak.dosa.f6`, versionCode **30194**, 표시명 `항행의자유 중앙통제실 V10` 그대로.
- 실제 APK에 `assets/v10_icon_draft.png`(319,376바이트) 및 `assets/v10_reference_banner.jpg`(60,335바이트) 두 항목 **추가**. 두 파일 모두 소스 ZIP과 SHA256 및 이미지 유효성 일치 검증.
- PNG SHA-256 `f78d422ff3209ba7942f4d25aa0b01b16dc075efdffbb8818af199416c12cdbc`; JPEG SHA-256 `4299ac2241096b6124ee64bc0b141e4183ebec8013266291ab00ee2d8c295dd8`.
- **이전 미서명 후보의 모든 68항목을 디코드된 파일 바이트 그대로 유지.** 원래 R60 분석/누적원장 및 리소스 항목 **64개 무변경**. Android manifest·V10 앱 아이콘 리소스·package/version·기존 원본 script 16개도 일치.
- APK 파일 SHA-256 **`4101a642f70676a37b68fb94d8df4704c80a3b937180f7ffa7797054eedb1856`**, 15,911,365바이트, 70항목.
- ZIP CRC/기존 파일 비교/Android ZIP_STORED 4바이트 정렬/PNG·JPEG decode 검사 PASS. 기존 서명 없음, 설치 불가.

## 실제 수정 APK 자산으로 새 브라우저 재현검사
- 실제 수정 APK에서 `assets/index.html`, 외부 `engines/*.js`, `v10_icon_draft.png`, `v10_reference_banner.jpg`를 전부 추출해 입력. 실행환경의 file/HTTP 접근 제한으로 로컬 JS는 HTML에 inline하고 이미지는 원본 APK 이미지 바이트로 data URL 변환해 렌더했으며 **임의 대체 이미지를 사용하지 않음**.
- Chromium 실측 1024×768, 393×852, 320×700 모두:
  - V10 대시보드 visible, 로고 이미지 자연너비 **512**, 메인 JPEG 배경 표시 확인.
  - 홈 방 카드 8개(7개 방 + 한 회차 전체보기), 상단 홈 포함 네비 버튼 8개.
  - 가로 오버플로 **0** / 브라우저 JS pageerror **0**.
- 원본/기존 진입 기능 10개 및 이전·종료 동작은 앞선 `V10_서명대기_기능검증결과.json`에서 통과. 이번 수정은 두 이미지의 APK 내 추가에 한정. 실제 Android 설치/이미지 WebView 현장 시험 아님.

## 서명용 신규 인계 패키지 (설치 파일 아님)
- ZIP `V10_30194_ASSET_FIXED_OWNER_SIGNER_HANDOFF_20261029.zip`: **15,823,744바이트**, SHA-256 `0dc362dfdaa0f82a0f7a80eea2d549b6403d69a21a7ed434c45f241c12512ddf`.
- 포함: 수정 APK `.apk.unsigned`, PNG/JPEG 보존 검증 JSON, `verify_v10_assets_fixed_unsigned.py`, `sign_v10_on_owner_machine.sh`(**수정 APK 해시 4101... 확인**), `check_phone_compatibility.sh`, 결함 수정 재현 스크립트, 명확한 설치 금지 안내.
- 독립 unsigned 검사 PASS: 변경 4항목 + 신규 이미지 2항목, 원본 R60 64항목 보존, version 30194, R60 소스 16 script.
- **Google Drive 업로드 및 재조회 성공:** https://drive.google.com/file/d/18JwXzL6tRMMCBULUhzlCTbxQV6NDgOP1/view ; 폴더 https://drive.google.com/drive/folders/1Lu2zzHW6qdPr5PPuvpJPyc0ThKZtjfA4
- 이전 미서명 ZIP/기존 R60 signed APK/기존 V4 배포 링크/V7 자료 삭제·교체하지 않음.

## 아직 달성하지 못한 사항
1. 기존 R60와 같은 SHA-256 인증서 `eedcfd3730f650e55defee1b0c971b77117b241211d73eb02a8fd9982245a48c`로 v1/v2 APK 서명할 보안 Android 도구·암호를 사용하지 못함. 공개 GitHub/채팅에 키나 암호 반입 금지.
2. 실기기 기존 설치 패키지가 `com.baekhak.dosa.f6`이고 versionCode가 30194보다 낮은지 확인 전. n3/f2는 f6로 데이터 보존 업데이트 불가능.
3. Android 설치·기존 DB/최초잠금 보존·V7 ACK/실제 음성/홈 실기기 검증 미완료. **signed V10 Android APK는 제작/Drive 게시 안 함.**
4. V3/V4 성능개선 엔진 연구는 별개로 남고, 이번 작업에서 엔진을 교체하지 않음.
