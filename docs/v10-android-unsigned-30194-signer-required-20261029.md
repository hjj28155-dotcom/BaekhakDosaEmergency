# ⚓ V10 중앙통제실 Android 설치 패키지화 — 서명 전 완료, 정식 설치 금지

날짜: 2026-10-29 · 선장 지시: “진행하십시오.”

## 소스/버전 경계
- 선택한 가로형 네이비 중앙통제실 홈 `40921.jpg`을 유지. 원본 R60 16개 분석 스크립트와 엔진/원장 바이트를 변경하지 않음.
- 실제 설치 원본: `항행의자유_V3_R60_설치정보복구_U10연구보완_30193_동일서명.apk`, SHA-256 `87ccc77af2a1bd2e5a3b19f31ef84e5e21eccffda717daf2e183c726f9f58026`, `com.baekhak.dosa.f6`, versionCode `30193`.
- R60 원본 공개 인증서 SHA-256 `eedcfd3730f650e55defee1b0c971b77117b241211d73eb02a8fd9982245a48c`. V3_R33_f2 서명된 APK의 지문과 동일하며 V4 공개키와 다름. **다른 인증서로 서명하거나 R60 앱을 삭제하지 않을 것.**
- 개인 PKCS12 서명키는 보호 폴더에서 확인했으나 **암호화되어 현재 환경에서 해독할 수 없음**. 비밀번호 확인/노출/무단 대체 없이 작업 중단. 개인키·암호는 여기에 복제/저장하지 않음.

## 실제 작성한 Android 서명 전 바이너리
- **이것은 설치할 수 없는 미서명 파일임.**
- 파일 `HANGHAE_CENTRAL_V10_30194_SAME_SIGNER_REQUIRED.apk.unsigned`; 버전코드 **30194**, 버전명 `V10.0-CENTRALCONTROL-F6`; 패키지 `com.baekhak.dosa.f6` 유지.
- Android manifest 원본 바이너리 XML 리소스를 유효한 StringPool 방식으로 재구성한 후 versionCode(30193→30194) 및 versionName을 설정.
- Android `resources.arsc` string `app_name`를 **`항행의자유 중앙통제실 V10`**으로 변경(원래 `항해사앱 33`).
- APK 실제 실행 아이콘 `res/Fk.png` (1024×1024) 교체; 다른 `res/Xg.png` 등 원본 리소스는 보존.
- `assets/index.html`만 검증된 V10 가로형 홈+기존 R60 16개 script 바이트 보존으로 변경. 그 외 분석엔진/원문/누적DB/Android 클래스 등 **64개 항목은 원본 R60 APK와 바이트 동일**.
- 이전 APK의 기존 v1 서명 메타데이터 3개는 제거; 기존 v2 APK Signing Block은 새 ZIP 작성으로 제거. **v1/v2 서명이 없기 때문에 기기 설치/업데이트 불가**임을 명시.
- ZIP CRC 테스트, 압축 해제, 필수 HTML 및 16개 원본 script 보존, stringpool parse/리소스 참조 및 versionCode 교체, ZIP_STORED 리소스의 4바이트 정렬 테스트 PASS. 독립 Android `aapt`/실기기 설치 테스트는 미완료.
- 서명 전 바이너리: **15,531,550 bytes**, SHA256 **`75e45eec435e8393cb8c26af51b6465000ff60f48e6c0afab58768ee4b97e9a5`**.

## 보존 패키지/Drive 검증
- 검증 메타데이터: `V10_ANDROID_UNSIGNED_30194_BUILD_VERIFICATION.json`.
- 보안 서명 작업자 인계 ZIP `V10_30194_UNSIGNED_SIGNER_HANDOFF_DO_NOT_INSTALL_20261029.zip`: 미서명 Android 파일 `.apk.unsigned` + 검증 JSON + 빌드 스크립트 + 설치 금지 안내. **암호화 PKCS12 개인키·비밀번호 포함되지 않음**.
- ZIP 크기 **15,439,550 bytes**, SHA256 **`ec2d94289552e672dc90681a6120e365722c756b67898711ab39fdc2854075ea`**; CRC PASS.
- 동일한 V10 격리 드라이브 시험자료 폴더 업로드 후 메타데이터 및 목록 재조회 PASS:
  https://drive.google.com/file/d/1ClSDUmy-A0pgGuozR0VwzVyzhOyKhdYM/view
- V10 화면 실소스 가로 미리보기: https://drive.google.com/file/d/11PEZy5N2z4r4rXMJ_di2Pg2U8kihJr4H/view
- **정식 V10 Android signed APK / 신규설치·업데이트 드라이브 APK 링크는 미완료.** 기존 V3·V4·V7 앱/Drive 설치 파일·데이터 변경 0건.

## 다음 출항 게이트
1. 원본 PKCS12를 소유자가 관리하는 **비공개 안전한 Android 서명환경**에 배치. 비밀키 암호는 채팅/공개 GitHub/소스파일에 입력하지 않음.
2. Android build-tools `apksigner`를 사용해 원본 키로 서명 후 `apksigner verify --verbose --print-certs`: v1/v2 서명 정상, SHA256 인증서 **eedcfd...45a48c** 정확히 일치, `aapt dump badging` app label·versionCode·package 확인.
3. 현재 선장 휴대전화에 설치된 항해사앱 versionCode가 **30194 미만인지** 확인 (같거나 더 높으면 별도 추가 상위 버전 필요), 서명 일치 확인.
4. 원문·누적DB·최초잠금 삭제 없이 실기기 덮어쓰기 설치 확인, 7개 방·한 회차 전체보기·각 방 이전·종료·아이콘·Android 화면 회전/가독성 시험.
5. 선장 실기기 검증과 명시 승인 후에만 기존 V3와 구분된 **V10 신규/업데이트 APK를 구글드라이브에 게시**. 기존 V3 R60 링크/파일 덮어쓰기 금지.

## 진실성 기록
- 실제 UI 소스 브라우저 동작 검사 PASS; Android 바이너리 구조, 앱 라벨/버전/아이콘 반영 PASS; **서명과 Android 실기기 설치 미완료**.
- V4 성능개선 연구는 별도 브랜치에서만 유지, V7 중앙통제실과 섞지 않음.
