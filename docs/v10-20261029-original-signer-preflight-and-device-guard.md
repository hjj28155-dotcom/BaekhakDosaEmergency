> **2026-10-29 정정:** 본 문서의 이전 V10 서명 전 APK(해시 `75e45eec...`)에는 홈 로고·배너 두 파일이 빠져 있습니다. **서명 및 설치에 사용하지 마십시오.** 새 수정본은 [`v10-30194-missing-assets-repaired-unsigned-20261029.md`](./v10-30194-missing-assets-repaired-unsigned-20261029.md)에 기록했으며, 그 수정본 역시 **서명 전·설치 불가** 상태입니다.

# V10 중앙통제실 — 안전 서명 사전검증·실기기 호환성 보호 (2026-10-29)

## 선장 지시 / 기준 화면
- 최신 지시 “계속 진행하세요”: 선장 확정 가로형 네이비 V10 중앙통제실 홈. 기존 R60 세로형 홈 배경은 사용하지 않음.
- V10 표시명·아이콘·선장방·항해사방·도사방·통제실방·자동5조합방·누적DB방·항행의자유 배포폰방·한 회차 전체보기·이전·종료 반영.
- V3 엔진·결과전 원본·누적DB·최초 잠금·V4 별도 앱·V7 별도 중앙통제실 삭제·초기화·임의 통합 금지.

## 정확한 물리적 파일 증거
- 기존 원본 R60 signed APK SHA256 `87ccc77af2a1bd2e5a3b19f31ef84e5e21eccffda717daf2e183c726f9f58026`, `com.baekhak.dosa.f6`, versionCode 30193, 기존 인증서 SHA256 `eedcfd3730f650e55defee1b0c971b77117b241211d73eb02a8fd9982245a48c`.
- 실제 V10 unsigned Android candidate SHA256 `75e45eec435e8393cb8c26af51b6465000ff60f48e6c0afab58768ee4b97e9a5`, 패키지 동일, versionCode 30194, Android label `항행의자유 중앙통제실 V10`, V10 launcher icon.
- **새로운 독립 AXML parser로** root manifest의 `package`·`versionCode`·`versionName`, `application` label/icon resource IDs 및 `resources.arsc`의 실제 app label 문자열을 읽어 검증 PASS.
- ZIP 71항목 signed R60 → 68항목 unsigned (기존 서명 metadata 3개 삭제), V10 변경 원래 엔트리 **정확히 4개**(`assets/index.html`,`AndroidManifest.xml`,`res/Fk.png`,`resources.arsc`), 기존 원본 **64개 완전 바이트 일치**, 인라인 기존 스크립트 **16개 완전 보존** 및 CRC PASS.
- 독립 검사 결과 JSON `V10_30194_UNSIGN_APK_INDEPENDENT_PREFLIGHT.json`.
- 이 검사만으로 Android 실제 런타임·앱 설치·원본 잠금 DB 보존을 보장할 수는 없음.

## 보안 서명/실기기 무변경 점검 도구 개발 (실제로 실행검사)
- `verify_v10_unsigned.py`: 두 APK 해시 확인 + 이진 manifest 버전/package + 리소스 앱 명칭/아이콘 포인터 + 64항목 불변 + 기존 script 16개 확인. **실제 V10 미서명 파일 대상 PASS**.
- `sign_v10_on_owner_machine.sh`: 소유자만 접근하는 보안 작업기에서 보호된 원본 PKCS12를 암호 노출 없이 apksigner 명령으로 사용하도록 안내. 해시/패키지/versionCode/앱명 검사 후 서명 시도, v1/v2 및 원본 인증서 SHA256 불일치 시 결과 폐기. Android `aapt`와 `apksigner` 필요. **이 환경엔 SDK가 없고 보호 키 암호도 없어 실제 서명은 수행하지 않았음.** 서명 완료 오인 금지.
- `check_phone_compatibility.sh`: `adb`에서 실기기 `com.baekhak.dosa.f6` 앱이 있는지 확인, versionCode 30194 이상이면 설치 중단. **가짜 adb 응답 실험:** 30193 → 허용, 30194/30195 → 차단, f6 없으면 차단 모두 통과. 실제 사용자 전화에서 실행하지 않음.
- 사용자 휴대전화 실제 설치 항해사앱 package/version은 **현재 미확인**. V33 계보 `.n3`, `.f2`, `.f6` 패키지는 서로 다르며 **f6 APK로 n3/f2 앱의 데이터 보존 업데이트가 불가능**함. 현 기기 앱을 삭제하거나 '업데이트'로 속여 신규 설치시키지 말 것.
- R33(f2)와 R60(f6)의 기존 공개 인증서 SHA256은 같으나 **패키지명이 다르다**. 공유 인증서만으로 데이터를 이관할 수 있다고 가정하면 안 됨.
- 본 안전 도구 ZIP에 개인키·키 암호·개인기기 자료는 포함하지 않음.

## Google Drive 검증 (원본 앱 변경 없이 별도 V10 폴더)
- V10 unsigned APK/소스 작업자 인계 원본 (설치 불가): https://drive.google.com/file/d/1ClSDUmy-A0pgGuozR0VwzVyzhOyKhdYM/view
- **신규 업로드한 안전 서명·기기호환성 도구 ZIP:** https://drive.google.com/file/d/1WxdU1f60ajX3AKbMz7So1pMvtMQ1LTqq/view
- Drive 폴더: https://drive.google.com/drive/folders/1Lu2zzHW6qdPr5PPuvpJPyc0ThKZtjfA4
- ZIP 이름 `V10_30194_OWNER_SECURE_SIGNING_PREFLIGHT_20261029.zip`, **8,311바이트**, SHA256 `120db365dc4e1b93607af9976bfec76a746e89f36a8f6fdb4534b38d62b5ce80`. 실제 ZIP CRC PASS, 5개 안전도구 포함. Drive에서 파일ID·크기·폴더 재조회 완료.
- 기존 f6 R60/V4/V7 APK/원장/Drive 파일은 변경하지 않음.

## 현재 장애물 / 다음 순서
1. 보안 Android build-tools 및 원본 비밀번호를 가진 **소유자 서명 환경**에서만 원본 PKCS12로 서명. 암호를 공개채팅/문서/GitHub에 올리면 안 됨.
2. `apksigner verify --verbose --print-certs` v1/v2 통과 + signer SHA256 원본과 완전 동일 확인, `aapt dump badging` V10 package/version/label 검사.
3. **실제 휴대전화 설치 패키지명과 버전**을 확인: `f6` + 30194 미만인 경우에만 보존 업데이트 후보, 그 외는 별도 이관 설계 필요.
4. 실제 Android 실기기 테스트로 기존 20조합·누적DB·최초잠금 유지, V10 가로형 홈·이전·종료·성적표/배포폰 링크 확인.
5. 이 과정을 통과한 **서명된 설치용 V10 APK만** Google Drive 별도 신규 설치/업데이트 파일로 등록. 선장 승인 전에 기존 Drive APK 자동 치환 금지.

## 수행상태
- unsigned Android 후보 제작 및 독립구조 검사 **완료**.
- 안전 서명/기기호환성 스크립트 **제작·문법·차단 모의검사 완료**.
- Drive 원본/검증 도구 업로드 및 확인 **완료**.
- 원본키로 signed APK 제작, 실제 휴대전화 실사용 확인, 설치용 Drive V10 APK 업로드 **미완료**.
